package com.arthmate.enachapi.service;

import com.arthmate.enachapi.dto.EnachDtlAmendRqstBdy;
import com.arthmate.enachapi.dto.EnachDtlSearchRqstBdy;
import com.arthmate.enachapi.dto.EnachMandateDetails;
import com.arthmate.enachapi.dto.npci.*;
import com.arthmate.enachapi.dto.npci.callback.MandateRejRespDc;
import com.arthmate.enachapi.dto.npci.callback.MndtAccptRespDc;
import com.arthmate.enachapi.model.EnachDetail;
import com.arthmate.enachapi.model.ThirdPartyServiceCode;
import com.arthmate.enachapi.repo.EnachDetailsRepo;
import com.arthmate.enachapi.repo.ThirdPartyServiceCodeRepo;
import com.arthmate.enachapi.utils.security.JwtEnachDetailsTokenService;
import com.arthmate.enachapi.utils.security.JwtTokenDetails;
import com.arthmate.enachapi.utils.security.JwtTokenUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.StringSubstitutor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.xml.bind.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.arthmate.enachapi.utils.ApplicationConstants.*;
import static com.arthmate.enachapi.utils.CommonUtils.decryptString;
import static com.arthmate.enachapi.utils.CommonUtils.encryptString;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnachDetailsService {

    private final EnachDetailsRepo enachDetailsRepo;
    private final Map<String, String> enachMandatePurposeMap;

    private final ModelMapper amendRequstEnachMapper;

    private static final String DFLT_DT_PTRN = "yyyy-MM-dd";
    private static final String DFLT_DTTM_PTRN = "yyyy-MM-dd+05:30";
    private final LogToS3Service logToS3Service;
    private ObjectMapper objectMapper = new ObjectMapper();
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtEnachDetailsTokenService jwtEnachDetailsTokenService;
    private final ThirdPartyServiceCodeRepo thirdPartyServiceCodeRepo;

    @Value("${npci.utility.code}")
    private String npciUtilityCode;
    @Value("${npci.corporate.name}")
    private String corpName;

    @Value("${npci.corporate.sponsor.bank}")
    private String corpSponBank;
    @Value("${npci.mandate.request.payload.mndtType}")
    private String mndtType;
    @Value("${npci.mandate.request.payload.ccy}")
    private String ccy;
    @Value("${npci.mandate.request.payload.seqTp}")
    private String seqTp;
    @Value("${npci.crAccDtl.name}")
    private String crAccDtlNm;
    @Value("${npci.crAccDtl.accNo}")
    private String crAccDtlAccNo;
    @Value("${npci.crAccDtl.ifsc.mmbId}")
    private String crAccDtlMmbId;

    @Value("${npci.amend.reason.code}")
    private String amendRCode;
    @Value("${npci.cancel.reason.code}")
    private String cancelRCode;
    @Value("${aws.s3.log.file.path.format}")
    private String s3LogFilePath;

   // @Value("${npci.xml.response.namespaced}")
    private String xmlNmSpc = " xmlns=\"http://npci.org/onmags/schema\"";



    public EnachDetail addNewEnachDetail(EnachDetail enachDetail) {
        enachDetail.setRequestId(getEnachRequestId());
        enachDetail.setNpciRequestId(enachDetail.getRequestId());
        enachDetail.setStatus(REQ_FLG_OPEN);
        enachDetail.setStatusNotified((byte)0);
        enachDetail.setCreatedAt(LocalDateTime.now());
        enachDetail.setUpdatedDate(LocalDateTime.now());
        enachDetailsRepo.save(enachDetail);
        return enachDetail;
    }

    public EnachDetail updateEnachDetail(EnachDetail enachDetail, String requestId) {
        enachDetail.setRequestId(requestId);
        if(StringUtils.isNoneEmpty(enachDetail.getNpciRequestId()))
            enachDetail.setNpciRequestId(enachDetail.getNpciRequestId());
        enachDetail.setUpdatedDate(LocalDateTime.now());

        enachDetailsRepo.update(enachDetail);
        return enachDetail;
    }

    public EnachDetail amendEnachDetail(EnachDtlAmendRqstBdy enachRequest, EnachDetail enachDetail){
        enachRequest.setStatus(REQ_AMND_FLG_REQ);
        enachRequest.setEnachReason(REQ_REASON_AMND);
        enachRequest.setStatusNotified((byte)0);
        return updateEnachDetail(amendRequstEnachMapper.map(enachRequest, EnachDetail.class), enachDetail.getRequestId());
    }

    public UpdateResult cancelEnachDetail(String requestId, String status, String reason){
        return updateEnachDetailStatus(requestId, status, reason);
    }

    public UpdateResult suspendEnachDetail(String requestId, String status, String reason){
        return updateEnachDetailStatus(requestId, status, reason);
    }

    public UpdateResult revokeSuspendEnachDetail(String requestId, String status, String reason){
        return updateEnachDetailStatus(requestId, status, reason);
    }

    public UpdateResult updateEnachDetailStatus(String requestId, String status, String reason) {
        return enachDetailsRepo.update(EnachDetail.builder()
                .requestId(requestId)
                .status(status)
                .statusNotified((byte)0)
                .enachReason(reason)
                .updatedDate(LocalDateTime.now()).build());
    }

    public UpdateResult updateEnachDetailStatus(String requestId, String status, String reason, String reasonCd, String npciRefId,
                                                String mndtId, boolean accptd, String msgId, String npciXmlRespS3Url, String respType) {
        String codeCategory = "";
        String statusCode = "";
        String statusDesc = "";
        final String service = "enach";
        if ("ErrorXML".equalsIgnoreCase(respType)) {
            codeCategory =  "enach_error_code";
        } else if ("RespXML".equalsIgnoreCase(respType)) {
            codeCategory =  "enach_reject_code";
        }

        // set our unique status code, desc based on service code
        Optional<ThirdPartyServiceCode> thirdPartyServiceCode = thirdPartyServiceCodeRepo
                .findByServiceServiceCodeAndCategory(service, codeCategory, reasonCd);
        if (thirdPartyServiceCode.isPresent()) {
            statusCode = thirdPartyServiceCode.get().getCodeId();
            statusDesc = thirdPartyServiceCode.get().getCodeDesc();
        } else {
            log.info("Unique code not found against the service code id for request id: {}", requestId);
        }

        return enachDetailsRepo.update(EnachDetail.builder()
                .requestId(requestId)
                .accptd(accptd)
                .status(status)
                .statusNotified((byte)0)
                .rejectReason(reason)
                .statusCode(statusCode)
                .statusDesc(StringUtils.isNotBlank(statusDesc) ? statusDesc : reason)
                .referenceNumber(npciRefId)
                .mndtId(mndtId)
                .msgId(msgId)
                //.npciRequestId(shuffle(requestId))
                .npciXmlRespS3Url(npciXmlRespS3Url)
                .updatedDate(LocalDateTime.now()).build());
    }

    public EnachDetail patchEnachMandateDetails(EnachMandateDetails enachRequest, String requestId) {

        enachDetailsRepo.patchEnachDetails(enachRequest, requestId);

        return enachDetailsRepo.getEnachDetailsByRequestId(requestId).orElse(null);
    }


    public boolean isRequestIdExists(String requestId){
        return enachDetailsRepo.isRequestIdExists(requestId);
    }

    public boolean isNPCIRequestIdExists(String requestId){
        return enachDetailsRepo.isNPCIRequestIdExists(requestId);
    }

    public EnachDetail getEnachDetailByRequest(String requestId){
        EnachDetail enachReq = null;
        Optional<EnachDetail> enachOpt =  enachDetailsRepo.getEnachDetailsByRequestId(requestId);
        if(enachOpt.isPresent())
            enachReq = enachOpt.get();
        else
            log.info("EnachDetail not found for request_id {}",requestId);
        return enachReq;
    }

    public long getEnachCountByExtRefNum(String extRefNum, List<String> stats){
        return enachDetailsRepo.getEnachCountByExtRefNum(extRefNum, stats);
    }

    public Map<String,Object> searchEnachDetailsByCriteria(EnachDtlSearchRqstBdy requestBdy){
        Map<String,Object> resp = new HashMap<>();
        resp =  enachDetailsRepo.searchEnachDetailsByCriteria(requestBdy);
        log.info("EnachDetail not found for request_body {}",requestBdy);
        return resp;
    }

    private String getEnachRequestId(){
        return UUID.randomUUID().toString().replace("-", "");

    }

    public String prepareNPCIPayloadChecksum(EnachDetail enachDetail){
        return String.format("%s|%s|%s|%s|%s", Objects.toString(enachDetail.getAccountNo(),""),
                Objects.toString(enachDetail.getStartDate().format(DateTimeFormatter.ofPattern(DFLT_DTTM_PTRN)),""),
                (enachDetail.getEndDate() != null ? Objects.toString(enachDetail.getEndDate().format(DateTimeFormatter.ofPattern(DFLT_DTTM_PTRN)),""):""),
                Objects.toString(enachDetail.getAmount().toString(),""),"");
    }


    public String generateHashString(String hashSrc) {
        // LOGGER.info("Value Before hashing :" + MMS_Constants.LOG_DELIMETER + hashSrc);
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(hashSrc.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        }

        byte[] byteData = md.digest();

        //convert the byte to hex format method 1
        StringBuilder sb = new StringBuilder();
        for (byte byteDatum : byteData) {
            sb.append(Integer.toString((byteDatum & 0xff) + 0x100, 16).substring(1));
        }


        //convert the byte to hex format method 2
        StringBuilder hexString = new StringBuilder();
        for (byte byteDatum : byteData) {
            String hex = Integer.toHexString(0xff & byteDatum);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return sb.toString();
    }

    public StringWriter prepareNPCIPayload(EnachDetail enachDetail)  {
        StringWriter stringWriter = new StringWriter();
        String MndtId =  enachDetail.getNpciRequestId();
        try{
            MndtAuthReq mndtAuthReq = MndtAuthReq.builder().grpHdr(
                    GrpHdr.builder()
                            .msgId(MndtId)
                            .creDtTm(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))
                            .reqInitPty(ReqInitPty.builder()
                                    .info(Info.builder()
                                            .id(Objects.toString(enachDetail.getUtilityNumber(), npciUtilityCode))
                                            .utilCode(Objects.toString(enachDetail.getUtilityNumber(), npciUtilityCode))
                                            .catCode(Objects.toString(enachDetail.getPurposeOfMandate(), ""))
                                            .catDesc(getCatDescriptionByCode(enachDetail.getPurposeOfMandate()))
                                            .name(Objects.toString(enachDetail.getCorporateName(), corpName))
                                            .spnBnk(corpSponBank)
                                            .build())
                                    .build())
                            .build()
            ).mandate(
                    Mandate.builder()
                            .mndtReqId(MndtId)
                            .mndtType(mndtType)
                            .colltnAmt(ColltnAmt.builder()
                                    .ccy(ccy)
                                    .value(encryptString(enachDetail.getAmount().toString()))
                                    .build())
                            .ocrncs(Ocrncs.builder()
                                    .seqTp(seqTp)
                                    .frqcy(enachDetail.getEmiFrequency())
                                    .frstColltnDt(
                                            encryptString(
                                                    enachDetail.getStartDate().format(DateTimeFormatter.ofPattern(DFLT_DTTM_PTRN))))
                                    .fnlColltnDt(enachDetail.getEndDate() != null ?
                                            encryptString(enachDetail.getEndDate().format(DateTimeFormatter.ofPattern(DFLT_DTTM_PTRN)))
                                            : "")
                                    .build())
                            .dbtr(Dbtr.builder()
                                    .nm(enachDetail.getCustomerName())
                                    .accNo(encryptString(enachDetail.getAccountNo()))
                                    .acctType(enachDetail.getAccountType().name())
                                     // .mobile("+91-"+enachDetail.getCustomerMobileNo())
                                     // .email(enachDetail.getCustomerEmailId())
                                     // .pan(enachDetail.getCustomerPAN())
                                    .build())
                            .crAccDtl(CrAccDtl.builder()
                                    .nm(corpSponBank)
                                    .mmbId(crAccDtlMmbId)
                                    .accNo(crAccDtlAccNo)
                                    .build())
                            .build()
            ).build();

            MandateReqDc mandateReqDc = new MandateReqDc();
            if (!StringUtils.isBlank(enachDetail.getMndtId())) {
                mndtAuthReq.getMandate().setMndtId(encryptString(enachDetail.getMndtId()));
                mndtAuthReq.getMandate().setReason(amendRCode);
            }
            mandateReqDc.setMndtAuthReq(mndtAuthReq);

            JAXBContext jaxbContext = JAXBContext.newInstance(MandateReqDc.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, false);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

            marshaller.marshal(mandateReqDc, stringWriter);

        }catch (Exception e){
            log.error("Exception: {} while prepare NPCI mandate request payload , Request_id: {}", e.getMessage(),enachDetail.getRequestId());
            e.printStackTrace();
        }
        return stringWriter;
    }

    public StringWriter prepareNPCICancelPayload(EnachDetail enachDetail)  {
        StringWriter stringWriter = new StringWriter();
        String MndtId =  enachDetail.getNpciRequestId();
        try{
            MndtAuthReq mndtAuthReq = MndtAuthReq.builder().grpHdr(
                    GrpHdr.builder()
                            .msgId(MndtId)
                            .creDtTm(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))
                            .reqInitPty(ReqInitPty.builder()
                                    .info(Info.builder()
                                            .id(Objects.toString(enachDetail.getUtilityNumber(), npciUtilityCode))
                                            .utilCode(Objects.toString(enachDetail.getUtilityNumber(), npciUtilityCode))
                                            .name(Objects.toString(enachDetail.getCorporateName(), corpName))
                                            .spnBnk(corpSponBank)
                                            .build())
                                    .build())
                            .build()
            ).mandate(
                    Mandate.builder()
                            .mndtReqId(MndtId)
                            .mndtId(encryptString(enachDetail.getMndtId()))
                            .reason(cancelRCode)
                            .crAccDtl(CrAccDtl.builder()
                                    .nm(corpSponBank)
                                    .mmbId(crAccDtlMmbId)
                                    .accNo(crAccDtlAccNo)
                                    .build())
                            .build()
            ).build();

            MandateReqDc mandateReqDc = new MandateReqDc();
            mandateReqDc.setMndtAuthReq(mndtAuthReq);

            JAXBContext jaxbContext = JAXBContext.newInstance(MandateReqDc.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, false);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

            marshaller.marshal(mandateReqDc, stringWriter);

        }catch (Exception e){
            log.error("Exception: {} while prepare NPCI mandate request payload , Request_id: {} NpciRequest_id: {}", e.getMessage(),enachDetail.getRequestId(),enachDetail.getNpciRequestId());
            e.printStackTrace();
        }
        return stringWriter;
    }

    public String getCatDescriptionByCode(String cd){
        Optional<Map.Entry<String, String>> optCatMap = enachMandatePurposeMap.entrySet().stream().filter(stringStringEntry -> stringStringEntry.getKey().equals(cd)).findFirst();
        return optCatMap.isPresent() ? optCatMap.get().getValue(): "";
    }

    public Map<String, String> parseRespNlog2S3(Map<String, String> resp, String createdBy){
        log.info("Resp:{}",resp);
        String mandateRespDoc = StringEscapeUtils.unescapeHtml4(resp.get("MandateRespDoc"));
        String checkSumVal = (String) resp.get("checkSumVal");
        String respType = (String) resp.get("respType");
        String requestID = resp.get("requestId");
        Map<String, String> logMap = new HashMap<>();
        logMap.put("MandateRespDoc", mandateRespDoc);
        logMap.put("CheckSumVal", checkSumVal);
        logMap.put("Status", respType);
        Optional<String> reqIdOpt = Optional.empty();
        try {
            if (respType.equals("ErrorXML")) {
                if (StringUtils.isBlank(requestID)) {
                    reqIdOpt = getRequestIdFromNPCIRejRespXml(mandateRespDoc);
                    if (reqIdOpt.isPresent()) requestID = reqIdOpt.get();
                }
                MandateRejRespDc mndtRes = getMndtRejectRespDc(mandateRespDoc);
                if (StringUtils.isNotBlank(requestID)) {
                    requestID = reqIdOpt.get();
                    Optional<EnachDetail> enachDetailOpt = enachDetailsRepo.getEnachDetailsByRequestId(requestID);
                    log.info("Reject Case : Callback RespType \"ErrorXML\" received for Request Id: {}", requestID);
                    logMap.put("RequestId", requestID);
                    logMap.put("Accptd", "false");
                    if (enachDetailOpt.isPresent()) {
                        logMap.put("Status", NPCI_CALLBACK_FAIL_STATUS.get(enachDetailOpt.get().getStatus()));
                    }
                    logMap.put("ReasonCd", mndtRes.getRjctRsnCd());
                    logMap.put("Reason", mndtRes.getRjctRsn());
                    logMap.put("ReferenceId", mndtRes.getRefrncID());
                    logMap.put("MsgId", mndtRes.getMsgID());
                }
            } else if (respType.equals("RespXML")) {
                if (StringUtils.isBlank(requestID)) {
                    reqIdOpt = getRequestIdFromNPCIAcpRespXml(mandateRespDoc);
                    if (reqIdOpt.isPresent()) requestID = reqIdOpt.get();
                }
                MndtAccptRespDc mndtRes = getMndtAccptRespDc(mandateRespDoc);
                if (reqIdOpt.isPresent()) {
                    requestID = reqIdOpt.get();
                    Optional<EnachDetail> enachDetailOpt = enachDetailsRepo.getEnachDetailsByRequestId(requestID);
                    logMap.put("RequestId", requestID);
                    logMap.put("Accptd", decryptString(mndtRes.getaccptncRslt()));
                    if (enachDetailOpt.isPresent()) {
                        logMap.put("Status", decryptString(mndtRes.getaccptncRslt()).equals("false") ?
                                NPCI_CALLBACK_FAIL_STATUS.get(enachDetailOpt.get().getStatus()) :
                                NPCI_CALLBACK_SUCCESS_STATUS.get(enachDetailOpt.get().getStatus()));
                    }
                    logMap.put("ReasonCd", decryptString(mndtRes.getRjctRsnCd()));
                    logMap.put("Reason", decryptString(mndtRes.getRjctRsn()));
                    logMap.put("ReferenceId", mndtRes.getRefrncID());
                    logMap.put("MndtId", mndtRes.getMndtID());
                    logMap.put("MsgId", mndtRes.getMsgID());
                    log.info("{} Case : {} RespType \"RespXML\" received for Request Id: {}", logMap.get("Status"), createdBy, requestID);
                }
            } else {
                log.info("Invalid 'respType' for {}:: {}", createdBy, resp.toString());
            }
            String logData = logMap.toString();
            try {
                logData = objectMapper.writeValueAsString(logMap);
            } catch (JsonProcessingException e) {
                log.info("Unable to transform in json Response, {}", resp.toString());
            }
            logMap.put("npciXmlRespS3Url", logNPCIRes2S3(requestID, logData, createdBy));
        }catch (Exception e){
            log.error("Callback response proprties value decryption problem for Request Id: {}",requestID);
        }
        return logMap;
    }

    private Optional<String> getRequestIdFromNPCIRejRespXml(String xmlString){
        InputStream mandateRespDocInpStrm = new ByteArrayInputStream(xmlString.getBytes());
        try{
            JAXBContext jaxbContext = JAXBContext.newInstance(MandateRejRespDc.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            MandateRejRespDc respObj = (MandateRejRespDc) unmarshaller.unmarshal(mandateRespDocInpStrm);
            return Optional.ofNullable(respObj.getRespMsgId());
        }catch (Exception e){
            log.info("Unable to Parse ErrorXML Response, {}", e.getMessage());
        }
        return Optional.of("");
    }
    private Optional<String> getRequestIdFromNPCIAcpRespXml(String xmlString){
        String xmlString1 = xmlString.replace(xmlNmSpc, "");
        InputStream mandateRespDocInpStrm = new ByteArrayInputStream(xmlString1.getBytes());
        try{
            JAXBContext jaxbContext = JAXBContext.newInstance(MndtAccptRespDc.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            MndtAccptRespDc respObj = (MndtAccptRespDc) unmarshaller.unmarshal(mandateRespDocInpStrm);
            return Optional.ofNullable(respObj.getRespMsgId());
        }catch (JAXBException e){
            log.info("Unable to Parse RespXML Response, {}", e.getMessage());
        }
        return Optional.of("");
    }

    public MndtAccptRespDc getMndtAccptRespDc(String xmlString){
        String xmlString1 = xmlString.replace(xmlNmSpc, "");
        InputStream mandateRespDocInpStrm = new ByteArrayInputStream(xmlString1.getBytes());
        try{
            JAXBContext jaxbContext = JAXBContext.newInstance(MndtAccptRespDc.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (MndtAccptRespDc) unmarshaller.unmarshal(mandateRespDocInpStrm);
        }catch (JAXBException e){
            log.info("Unable to Parse RespXML Response, {}", e.getMessage());
        }
        return null;
    }

    public MandateRejRespDc getMndtRejectRespDc(String xmlString){
        String xmlString1 = xmlString.replace(xmlNmSpc, "");
        InputStream mandateRespDocInpStrm = new ByteArrayInputStream(xmlString1.getBytes());
        try{
            JAXBContext jaxbContext = JAXBContext.newInstance(MandateRejRespDc.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (MandateRejRespDc) unmarshaller.unmarshal(mandateRespDocInpStrm);
        }catch (JAXBException e){
            log.info("Unable to Parse ErrorXML Response, {}", e.getMessage());
        }
        return null;
    }

     public String logNPCIRes2S3(String reqId, String logContlent, String createdBy){
        if (NPCI_CALLBACK_API.equalsIgnoreCase(createdBy)) createdBy = createdBy.concat(" Request");
        else if (RES_POSTED_TO_MERCHANT_API.equalsIgnoreCase(createdBy)) createdBy = createdBy.concat(" Response");

         StringBuilder npciCallbackLogs = LogToS3Service.writeLogToString(null, logContlent, createdBy);
         Map<String, Object> params = new HashMap<>();
         params.put("apiName", createdBy);
         params.put("requestId", reqId);
         params.put("timestamp", System.currentTimeMillis());
         params.put("exe", "txt");
         String logFileName = StringSubstitutor.replace(s3LogFilePath, params, "%{", "}");

         return logToS3Service.uploadLogsToS3(logFileName, npciCallbackLogs.toString());
     }

    public String generateToken( String requestId){
       JwtTokenDetails jwtTokenDetails = jwtEnachDetailsTokenService.loadEnachDetailsByRequestId(requestId);
        return jwtTokenUtil.generateTokenForEnachDetails(jwtTokenDetails);
    }

}
