package com.arthmate.enachapi.service;

import com.arthmate.enachapi.client.EnachRequestStatusClient;
import com.arthmate.enachapi.dto.*;
import com.arthmate.enachapi.model.EnachDetail;
import com.arthmate.enachapi.model.EnachReqResLog;
import com.arthmate.enachapi.model.ThirdPartyServiceCode;
import com.arthmate.enachapi.repo.EnachDetailsRepo;
import com.arthmate.enachapi.repo.EnachReqResLogRepo;
import com.arthmate.enachapi.repo.ThirdPartyServiceCodeRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.arthmate.enachapi.utils.ApplicationConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnachRequestStatusService {

    private final EnachRequestStatusClient npciRequestStatusClient;
    private final ThirdPartyServiceCodeRepo thirdPartyServiceCodeRepo;
    private final LogToS3Service logToS3Service;
    private final EnachDetailsService enachDetailsService;
    private final EnachDetailsRepo enachDetailsRepo;
    private final EnachReqResLogRepo enachReqResLogRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String service = "enach";
    private StringBuilder reqResLog = new StringBuilder();
    private List<EnachReqResLog> logRecords = new ArrayList<>();

    @Value("${npci.merchant.id}")
    private String merchantId;
    @Value("${aws.s3.log.file.path.format}")
    private String s3LogFilePath;

    public EnachStatusResponseDto checkEnachRequestStatus(EnachDetail enachDetail, String calledBy) {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            // call API to get transaction status for Merchant
            enachDetail = checkTxnStatusForMerchant(enachDetail);

            // store the req-res file to s3
            enachDetail = uploadReqResLogToS3(enachDetail, calledBy);

        } catch (Exception e) {
            log.error("Exception occurred while checking status for request id: {}", enachDetail.getRequestId(), e);
        }
        return EnachStatusResponseDto.builder()
                .enachDetail(enachDetail)
                .enachReqResLog(logRecords)
                .build();
    }

    private EnachDetail checkTxnStatusForMerchant(EnachDetail enachDetail) {
        try {
            TxnStatusForMerchantReqBdy txnStatusForMerchantReq = buildTxnStatusForMerchantReqBdy(enachDetail);
            String txnStatusForMerchantReqStr = objectMapper.writeValueAsString(txnStatusForMerchantReq);
            logToS3Service.writeLogToString(reqResLog, txnStatusForMerchantReqStr, TXN_STATUS_FOR_MERCHANT_API.concat(" Request"));

            String txnStatusForMerchantResStr = npciRequestStatusClient.callTxnStatusForMerchantApi(txnStatusForMerchantReqStr);
            logToS3Service.writeLogToString(reqResLog, txnStatusForMerchantResStr, TXN_STATUS_FOR_MERCHANT_API.concat(" Response"));

            TxnStatusForMerchantResBdy txnStatusForMerchantRes = objectMapper
                    .readValue(txnStatusForMerchantResStr, TxnStatusForMerchantResBdy.class);

            enachDetail = setNpciResponse(enachDetail, txnStatusForMerchantRes.getTranStatus().get(0));
            enachDetail = setEnachStatus(enachDetail, txnStatusForMerchantRes.getTranStatus().get(0));

            // if status is fetched successfully call response posted to Merchant api to get Xml response
            // even if this api fails the status should received previously should be captured properly
            enachDetail = checkResPostedToMerchant(enachDetail);

        } catch (Exception e) {
            log.error("Exception occurred in txn status for merchant api for requestId: {}", enachDetail.getRequestId(), e);
        }

        return enachDetail;
    }

    private EnachDetail setEnachStatus(EnachDetail enachDetail, TxnStatusForMerchantResBdy.TranStatus tranStatus) {
        String codeCategory = "";

        // check error codes, rejection codes
        // for successful request to npci error_code will be "000"
        if (tranStatus.getErrorCode() == null || "000".equals(tranStatus.getErrorCode())) {
            // set status for success resposne
            enachDetail.setStatus(NPCI_CALLBACK_SUCCESS_STATUS.get(enachDetail.getStatus()));
            codeCategory = "enach_reject_code";
        } else {
            // set status for fail request
            enachDetail.setStatus(NPCI_CALLBACK_FAIL_STATUS.get(enachDetail.getStatus()));
            codeCategory = "enach_error_code";
        }

        // set our unique status code, desc based on service code
        Optional<ThirdPartyServiceCode> thirdPartyServiceCode = thirdPartyServiceCodeRepo
                .findByServiceServiceCodeAndCategory(service, codeCategory, tranStatus.getReasonCode());
        if (thirdPartyServiceCode.isPresent()) {
            enachDetail.setStatusCode(thirdPartyServiceCode.get().getCodeId());
            enachDetail.setStatusDesc(thirdPartyServiceCode.get().getCodeDesc());
        } else {
            log.info("Unique code not found against the service code id for request id: {}", enachDetail.getRequestId());
            enachDetail.setStatusDesc(tranStatus.getErrorDesc());
        }

        return enachDetail;
    }

    private EnachDetail checkResPostedToMerchant(EnachDetail enachDetail) {
        try {
            ResPostedToMerchantReqBdy resPostedToMerchantReq = buildResPostedToMerchantReqBdy(enachDetail);
            String resPostedToMerchantReqStr = objectMapper.writeValueAsString(resPostedToMerchantReq);
            logToS3Service.writeLogToString(reqResLog, resPostedToMerchantReqStr, RES_POSTED_TO_MERCHANT_API.concat(" Request"));

            String resPostedToMerchantResStr = npciRequestStatusClient.callResPostedToMerchantApi(resPostedToMerchantReqStr);
            logToS3Service.writeLogToString(reqResLog, resPostedToMerchantResStr, RES_POSTED_TO_MERCHANT_API.concat(" Response"));
            ResPostedToMerchantResBdy resPostedToMerchantRes = objectMapper
                    .readValue(resPostedToMerchantResStr, ResPostedToMerchantResBdy.class);

            // decrypt the RespXml/ErrorXml and store it to s3
            String mandateRespDoc = StringEscapeUtils.unescapeHtml4(resPostedToMerchantRes.getResponseDtl().get(0).getMandateRespDoc());
            String checkSumVal = resPostedToMerchantRes.getResponseDtl().get(0).getCheckSumVal();
            String respType = resPostedToMerchantRes.getResponseDtl().get(0).getRespType();

            Map<String, String> request = Map.of(
                    "MandateRespDoc", mandateRespDoc,
                    "checkSumVal", checkSumVal,
                    "respType", respType,
                    "requestId", enachDetail.getRequestId()
            );

            Map logMap = enachDetailsService.parseRespNlog2S3(request, RES_POSTED_TO_MERCHANT_API);
            enachDetail.setNpciXmlRespS3Url((String)logMap.get("npciXmlRespS3Url"));
            logRecords.add(LogToS3Service.buildEnachReqResLog(enachDetail.getRequestId(),
                    (String)logMap.get("npciXmlRespS3Url"), NCPI_REQUEST_STATUS_BATCH));

        } catch (Exception e) {
            log.error("Exception occurred in response posted to merchant api for requestId: {}", enachDetail.getRequestId(), e);
        }

        return enachDetail;
    }

    private EnachDetail uploadReqResLogToS3(EnachDetail enachDetail, String createdBy) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("apiName", createdBy);
            params.put("requestId", enachDetail.getRequestId());
            params.put("timestamp", System.currentTimeMillis());
            params.put("exe", "txt");
            String fileName = StringSubstitutor.replace(s3LogFilePath, params, "%{", "}");
            String reqResS3Url = logToS3Service.uploadLogsToS3(fileName, reqResLog.toString());
            log.info("Enach status req res log uploaded to s3 for request id: {} at: {}", enachDetail.getRequestId(), reqResS3Url);
            enachDetail.setStatusReqResS3Url(reqResS3Url);
            logRecords.add(LogToS3Service.buildEnachReqResLog(enachDetail.getRequestId(), reqResS3Url, createdBy));

        } catch (Exception e) {
            log.error("Exception occurred in saving req-res file to s3 for request id: {} :: ",
                    enachDetail.getRequestId(), e.getMessage());
        }

        return enachDetail;
    }

    private EnachDetail setNpciResponse(EnachDetail enachDetail, TxnStatusForMerchantResBdy.TranStatus tranStatus) {
        if (ObjectUtils.isNotEmpty(tranStatus)) {
            enachDetail.setNpciRefMsgId(tranStatus.getNpciRefMsgId());
            enachDetail.setAccptd(Boolean.parseBoolean(tranStatus.getAccptd()));
            enachDetail.setAccptRefNo(tranStatus.getAccptRefNo());
            enachDetail.setReasonCode(tranStatus.getReasonCode());
            enachDetail.setReasonDesc(tranStatus.getReasonDesc());
            enachDetail.setRejectBy(tranStatus.getRejectBy());
            enachDetail.setErrorCode(tranStatus.getErrorCode());
            enachDetail.setErrorDesc(tranStatus.getErrorDesc());
        }
        return enachDetail;
    }

    private TxnStatusForMerchantReqBdy buildTxnStatusForMerchantReqBdy(EnachDetail enachDetail) {
        return TxnStatusForMerchantReqBdy.builder()
                .mandateReqIdList(List.of(buildMandateReqId(enachDetail)))
                .build();
    }

    private TxnStatusForMerchantReqBdy.MandateReqId buildMandateReqId(EnachDetail enachDetail) {
        return TxnStatusForMerchantReqBdy.MandateReqId.builder()
                .merchantId(merchantId)
                .mndtReqId(enachDetail.getRequestId())
                .reqInitDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .build();
    }

    private ResPostedToMerchantReqBdy buildResPostedToMerchantReqBdy(EnachDetail enachDetail) {
        return ResPostedToMerchantReqBdy.builder()
                .getRespForNpciRefId(List.of(buildMandateReqIdNpci(enachDetail)))
                .build();
    }

    private ResPostedToMerchantReqBdy.MandateReqIdNpci buildMandateReqIdNpci(EnachDetail enachDetail) {
        return ResPostedToMerchantReqBdy.MandateReqIdNpci.builder()
                .merchantId(merchantId)
                .mndtReqId(enachDetail.getRequestId())
                .reqInitDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .npciRefMsgId(enachDetail.getNpciRefMsgId())
                .build();
    }

    public void updateEnachStatusDetails(List<? extends EnachStatusResponseDto> items, String updatedBy) {
        int result1 = enachDetailsRepo.updateByQueryUpdatePairs(items.stream()
                .filter(obj -> Objects.nonNull(obj) && Objects.nonNull(obj.getEnachDetail()))
                .map(obj -> getEnachStatusUpdateQueryPair(obj.getEnachDetail(), updatedBy))
                .toList());
        log.info("No. of enach details updated with status: {}", result1);

        Collection<EnachReqResLog> result2 = enachReqResLogRepo.insertAll(items.stream()
                .filter(obj -> CollectionUtils.isNotEmpty(obj.getEnachReqResLog()))
                .map(EnachStatusResponseDto::getEnachReqResLog)
                .flatMap(List::stream)
                .filter(obj -> ObjectUtils.isNotEmpty(obj))
                .toList());
        log.info("No. of enach status req-res logs inserted: {}", result2.size());
    }

    private Pair<Query, Update> getEnachStatusUpdateQueryPair(EnachDetail enachDetail, String updatedBy) {
        return Pair.of(
                Query.query(Criteria.where("request_id").is(enachDetail.getRequestId())),
                Update.update("accptd", enachDetail.isAccptd())
                        .set("accpt_ref_no", enachDetail.getAccptRefNo())
                        .set("reason_code", enachDetail.getReasonCode())
                        .set("reason_desc", enachDetail.getReasonDesc())
                        .set("reject_by", enachDetail.getRejectBy())
                        .set("error_code", enachDetail.getErrorCode())
                        .set("error_desc", enachDetail.getErrorDesc())
                        .set("status", enachDetail.getStatus())
                        .set("status_notified",0)
                        .set("status_code", enachDetail.getStatusCode())
                        .set("status_desc", enachDetail.getStatusDesc())
                        .set("status_req_res_s3_url", enachDetail.getStatusReqResS3Url())
                        .set("npci_xml_resp_s3_url", enachDetail.getNpciXmlRespS3Url())
                        .set("updated_by", updatedBy)
                        .set("updated_at", LocalDateTime.now())
        );
    }

    public ResponseEntity<Map> processEnachStatusApi(String requestId) {
        try {
            Optional<EnachDetail> enachDetailOpt = enachDetailsRepo.getEnachDetailsByRequestId(requestId);

            if (enachDetailOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(statusApiResponseBody(false, "RequestId doesn't exist"));
            }
            EnachDetail enachDetail = enachDetailOpt.get();

            // fetch status if enach details status in mandate_initiated otherwise skip
            if (REQ_FLG_INIT.equalsIgnoreCase(enachDetail.getStatus())) {
                EnachStatusResponseDto statusResponse = checkEnachRequestStatus(enachDetailOpt.get(), REQUEST_STATUS_API);
                enachDetail = statusResponse.getEnachDetail();
                updateEnachStatusDetails(List.of(statusResponse), REQUEST_STATUS_API);
            }

            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "message", "Status fetched successfully",
                    "data", enachDetail
            ));

        } catch (Exception e) {
            log.error("Exception occurred in enach status api for request id: {}", requestId, e);
            return ResponseEntity.internalServerError().body(statusApiResponseBody(false, "Error occurred while fetching status"));
        }
    }

    private Map<String, Object> statusApiResponseBody(boolean success, String message) {
        return Map.of(
                "success", success,
                "message", message
        );
    }
}
