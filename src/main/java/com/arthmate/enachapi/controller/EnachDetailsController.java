package com.arthmate.enachapi.controller;

import com.arthmate.enachapi.dto.*;
import com.arthmate.enachapi.exception.XmlSignatureException;
import com.arthmate.enachapi.model.EnachDetail;
import com.arthmate.enachapi.model.EnachDetailHistory;
import com.arthmate.enachapi.model.LiveBankStatusResponseBody;
import com.arthmate.enachapi.service.*;
import com.arthmate.enachapi.utils.ResponseHandler;
import com.arthmate.enachapi.utils.security.JwtTokenUtil;
import com.arthmate.enachapi.utils.validator.ActionPermissionMatrix;
import com.arthmate.enachapi.utils.validator.ActionPermissionMatrixByAuth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.StringWriter;
import java.net.URI;
import java.util.*;

import static com.arthmate.enachapi.utils.ApplicationConstants.*;
import static com.arthmate.enachapi.utils.CommonUtils.*;
import static reactor.netty.Metrics.SUCCESS;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@CrossOrigin
public class EnachDetailsController {

    private final EnachDetailsService enachDetailsService;
    private final EnachDetailsHistoryService enachDetailsHistoryService;
    private final BankLiveStatusService bankLiveStatusService;
    private final CacheManager cacheManager;
    private final LogToS3Service logToS3Service;
    private final EnachRequestStatusService enachRequestStatusService;
    private final ModelMapper enachRequestMapper;

    @Value("${npci.livebankstatus.url}")
    private String liveBankStatusUri;

    @Value("${npci.mandate.request.submit.url}")
    private String npciMandateRequestSubmitUrl;
    @Value("${npci.mandate.request.amend.url}")
    private String npciMandateRequestAmendUrl;

    @Value("${npci.mandate.request.cancel.url}")
    private String npciMandateRequestCancelUrl;

    @Value("${enach.redirect.url}")
    private String redirectWebUrl;

    @Value("${enach.redirect.callback.status.url}")
    private String redirectCallbackStatusUrl;

    @Value("${npci.merchant.id}")
    private String merchantID;

    @Value("${npci.env}")
    private String env;

    @Value("${npci.uat.bank}")
    private String uatBank;

    @Value("#{${npci.auth.mode.map}}")
    Map<String, String> authMap;

    private final JwtTokenUtil jwtTokenUtil;
    private final ModelMapper modelMapper;

    @Cacheable("live-banks")
    @GetMapping("/npci-live-bank-status")
    public ResponseEntity<Object> getLiveBankStatus() {
        Map<String, LiveBankStatusResponseBody> response;
        response = bankLiveStatusService.getNPCIBankStatus(liveBankStatusUri);
        return ResponseHandler.responseBuilder(SUCCESS, HttpStatus.OK,response);
    }

    @PostMapping("/npci-request")
    @PreAuthorize("hasAuthority('nach-ext-write')")
    public ResponseEntity<Object> addEnachDetails(@Valid @RequestBody EnachDtlRqstBdy enachRequest, Authentication authentication) {
        var companyId = jwtTokenUtil.getCompanyIdByAuthenticationObj(authentication);
        var userId = jwtTokenUtil.getUserIdByAuthenticationObj(authentication);
        if(StringUtils.isNotBlank(companyId)){
            enachRequest.setCompanyId(companyId);
        }
        if(StringUtils.isNotBlank(userId)){
            enachRequest.setCreatedBy(userId);
        }
        log.info("Add E-nach details request body {}",enachRequest);
        var response = new HashMap<String,Object>();

        EnachDetail enachDetail = enachDetailsService.addNewEnachDetail(enachRequestMapper.map(enachRequest, EnachDetail.class));
        response.put(REQUEST_ID,enachDetail.getRequestId());
        return ResponseHandler.responseBuilder("Created Successfully", HttpStatus.OK,response);
    }

    @PutMapping("/npci-request/{requestId}")
    @PreAuthorize("hasAuthority('nach-ext-write')")
    public ResponseEntity<Object> updateEnachDetails(
            @PathVariable String requestId ,
            @Valid @RequestBody EnachDtlRqstBdy enachRequest, Authentication authentication) {
        var userId = jwtTokenUtil.getUserIdByAuthenticationObj(authentication);
        if(StringUtils.isNotBlank(userId)){
            enachRequest.setUpdatedBy(userId);
        }
        log.info("Update E-nach details request body {}",enachRequest);
        var response = new HashMap<String,Object>();
        EnachDetail enachDetail = enachDetailsService.updateEnachDetail(modelMapper.map(enachRequest, EnachDetail.class), requestId);
        response.put(REQUEST_ID,enachDetail.getRequestId());
        return ResponseHandler.responseBuilder("Updated Successfully", HttpStatus.OK,response);
    }

    @PatchMapping("/npci-request/{requestId}")
    @PreAuthorize("hasAuthority('nach-ext-write')")
    public ResponseEntity<Object> patchEnachDetails(
            @PathVariable @ActionPermissionMatrix(cnd = ACT_AMEND, act = ACT_AMEND_NM, val = ACT_DENIED)
            String requestId , @RequestBody EnachDtlAmendRqstBdy enachRequest) {
        log.info("Update E-nach details request body {}",enachRequest);
        var response = new HashMap<String,Object>();
        EnachDetail enachDetailCln = enachDetailsService.getEnachDetailByRequest(requestId);
        enachDetailsHistoryService.saveEnachDetailHistory(enachDetailCln);
        enachDetailsService.amendEnachDetail(enachRequest, enachDetailCln);
        response.put(REQUEST_ID,enachDetailCln.getRequestId());
        response.put("npci_request_id",enachDetailCln.getNpciRequestId());

        log.info("A new audit trail with new npci_request_id :{} created Successfully", enachDetailCln.getNpciRequestId());

        return ResponseHandler.responseBuilder("Request Amend initiated Successfully", HttpStatus.OK,response);
    }

    @PatchMapping("/npci-request")
    @PreAuthorize("hasAuthority('nach-int-readwrite')")
    public ResponseEntity<Object> patchMandateDetails(
            @ActionPermissionMatrixByAuth(cnd = ACT_PATCH_REG, act = ACT_PATCH_REG_NM, val = ACT_DENIED) Authentication authentication,
            @Valid @RequestBody EnachMandateDetails enachRequest) {
        log.info("Patch E-nach mandate details request body {}", enachRequest);
        var requestId = jwtTokenUtil.getRequestIdByAuthenticationObj(authentication);
        var response = new HashMap<String, Object>();
        EnachDetail enachDetail = enachDetailsService.patchEnachMandateDetails(enachRequest, requestId);
        response.put(REQUEST_ID, requestId);
        response.put("enach_details", modelMapper.map(enachDetail, EnachDtlRespBdy.class));
        return ResponseHandler.responseBuilder(RESP_SUCCESS, HttpStatus.OK, response);
    }


    @GetMapping("/npci-request")
    @PreAuthorize("hasAuthority('nach-int-readwrite')")
    public ResponseEntity<Object> getEnachDetailsByRequestId(Authentication authentication) {
        var requestId = jwtTokenUtil.getRequestIdByAuthenticationObj(authentication);
        log.info("Get E-nach details by request_id: {}",requestId);
        var response = new HashMap<String,Object>();
        EnachDetail enachDetail = enachDetailsService.getEnachDetailByRequest(requestId);
        response.put("enach_details",modelMapper.map(enachDetail, EnachDtlRespBdy.class));
        return ResponseHandler.responseBuilder(SUCCESS, HttpStatus.OK,response);
    }

    @DeleteMapping("/npci-request/{requestId}")
    @PreAuthorize("hasAuthority('nach-ext-write')")
    public ResponseEntity<Object> cancelEnachRequest(@PathVariable
                @ActionPermissionMatrix(cnd = ACT_CANCEL, act = ACT_CANCEL_NM, val = ACT_DENIED)
                String requestId ) {
        log.info("Cancel E-nach details request Id {}",requestId);
        var response = new HashMap<String,Object>();
        EnachDetail enachDetailCln = enachDetailsService.getEnachDetailByRequest(requestId);
        enachDetailsHistoryService.saveEnachDetailHistory(enachDetailCln);
        enachDetailsService.cancelEnachDetail(requestId, REQ_FLG_CANCEL, REQ_REASON_CANCEL);
        response.put(REQUEST_ID,enachDetailCln.getRequestId());
        response.put("npci_request_id",enachDetailCln.getNpciRequestId());

        log.info("A new audit trail with new npci_request_id :{} canceled Successfully", enachDetailCln.getNpciRequestId());
        return ResponseHandler.responseBuilder("Request Cancel initiated Successfully", HttpStatus.OK,response);
    }

    @GetMapping("/npci-request-suspend/{requestId}")
    @PreAuthorize("hasAuthority('nach-ext-write')")
    public ResponseEntity<Object> suspendEnachRequest(@PathVariable
                @ActionPermissionMatrix(cnd = ACT_SUSPEND, act = ACT_SUSPEND_NM, val = ACT_DENIED)
                String requestId) {
        log.info("Suspend E-nach details request Id {}",requestId);
        var response = new HashMap<String,Object>();
        EnachDetail enachDetailCln = enachDetailsService.getEnachDetailByRequest(requestId);
        enachDetailsHistoryService.saveEnachDetailHistory(enachDetailCln);
        enachDetailsService.suspendEnachDetail(requestId, REQ_SUSPEND_FLG, REQ_REASON_SUSPEND);
        response.put(REQUEST_ID,enachDetailCln.getRequestId());
        response.put("npci_request_id",enachDetailCln.getNpciRequestId());

        log.info("A new audit trail with new npci_request_id :{} suspended Successfully", enachDetailCln.getNpciRequestId());
        return ResponseHandler.responseBuilder("Request Suspended Successfully", HttpStatus.OK,response);
    }

    @GetMapping("/npci-request-revoke-suspend/{requestId}")
    @PreAuthorize("hasAuthority('nach-ext-write')")
    public ResponseEntity<Object> revokeSuspendEnachRequest(@PathVariable
            @ActionPermissionMatrix(cnd = ACT_REVOKE_SUSPEND, act = ACT_REVOKE_SUSPEND_NM, val = ACT_DENIED)
            String requestId) {
        log.info("Revoke Suspend E-nach details request Id {}",requestId);
        var response = new HashMap<String,Object>();
        EnachDetailHistory enachDetailHistory = enachDetailsHistoryService.getStatusBeforeSuspendByRequestId(requestId);
        EnachDetail enachDetailCln = enachDetailsService.getEnachDetailByRequest(requestId);
        enachDetailsHistoryService.saveEnachDetailHistory(enachDetailCln);
        enachDetailsService.revokeSuspendEnachDetail(requestId, enachDetailHistory.getStatus(), REQ_REASON_REVOKE_SUSPEND);
        response.put(REQUEST_ID,enachDetailCln.getRequestId());
        response.put("npci_request_id",enachDetailCln.getNpciRequestId());

        log.info("A new audit trail with new npci_request_id :{} revoke suspended Successfully", enachDetailCln.getNpciRequestId());
        return ResponseHandler.responseBuilder("Request Revoke Suspended Successfully", HttpStatus.OK,response);
    }



    @GetMapping(value = "/npci/payload")
    @PreAuthorize("hasAuthority('nach-int-readwrite')")
    public ResponseEntity<Object> getNPCIPayloadByRequestId(@ActionPermissionMatrixByAuth(cnd = ACT_PAYLOAD, act = ACT_PAYLOAD_NM, val = ACT_DENIED) Authentication authentication) {
        var requestId = jwtTokenUtil.getRequestIdByAuthenticationObj(authentication);
        log.info("Get NPCI payload for enach mandate by request_id: {}", requestId);
        var response = new HashMap<String, Object>();
        EnachDetail enachDetail = enachDetailsService.getEnachDetailByRequest(requestId);
        String checkSum = enachDetailsService.prepareNPCIPayloadChecksum(enachDetail);
        StringWriter stringWriter = null;
        if(enachDetail.getStatus().equals(REQ_CANCEL_FLG_REQ))
            stringWriter = enachDetailsService.prepareNPCICancelPayload(enachDetail);
        else
             stringWriter = enachDetailsService.prepareNPCIPayload(enachDetail);
        try {
            response.put("MandateReqDoc", encodeXmlData(generateXMLDigitalSignature(stringWriter.toString())));
            boolean isValid = isXmlDigitalSignatureValid(decodeXmlData((String) response.get("MandateReqDoc")));
            log.info("XML Digital Signature validity Check for request_id: {} npci_request_id: {} validityStatus : {}", requestId, enachDetail.getNpciRequestId(), isValid);
            if(!enachDetail.getStatus().equals(REQ_CANCEL_FLG_REQ))
                response.put("CheckSumVal", encryptString(generateHashString(checkSum)));
        } catch (XmlSignatureException e) {
            log.error("Error in signing xml content for requestId {} npci_request_id: {}", requestId, enachDetail.getNpciRequestId(), e);
        }catch (Exception e) {
            log.error("Error in generating check sum value for requestId {} npci_request_id: {}", requestId, enachDetail.getNpciRequestId(), e);
        }
        enachDetailsService.updateEnachDetailStatus(requestId, StringUtils.isBlank(enachDetail.getMndtId()) ? REQ_FLG_INIT : REQ_AMND_FLG_INIT,"");
        response.put("MerchantID", merchantID);
        response.put("BankID", env.equals(PRODUCTION) ? enachDetail.getBank() : uatBank);
        response.put("AuthMode", authMap.get(enachDetail.getAuthenticationMode()));
        response.put("NPCIRequestPostUrl", StringUtils.isBlank(enachDetail.getMndtId()) ?  npciMandateRequestSubmitUrl : npciMandateRequestAmendUrl);
        if(enachDetail.getStatus().equals(REQ_CANCEL_FLG_REQ))
            response.put("NPCIRequestPostUrl", npciMandateRequestCancelUrl);

        return ResponseHandler.responseBuilder(SUCCESS, HttpStatus.OK, response);
    }

    @PostMapping(value = "/npci/callback")
    public ResponseEntity<Void> sumitForm(@RequestParam Map<String, String> request) {
        log.info("NPCI Callback Response {}", request);
        String mandateRespDoc = StringEscapeUtils.unescapeHtml4(request.get("MandateRespDoc"));
        String checkSumVal = request.get("checkSumVal");
        String respType = request.get("respType");
        log.info("NPCI Callback MandateRespDoc {}, Checksum: {}, RespType: {}", mandateRespDoc, checkSumVal, respType);

        Map<String,String> resMap = enachDetailsService.parseRespNlog2S3(request, NPCI_CALLBACK_API);
        String requestId = resMap.get("RequestId");
        if(enachDetailsService.isNPCIRequestIdExists(resMap.get("RequestId"))){
             boolean accptd = Boolean.parseBoolean(resMap.get("Accptd").toLowerCase());
             String status = resMap.get("Status").toLowerCase();
             String reason = resMap.get("Reason");
             String reasonCd = resMap.get("ReasonCd");
             String npciRefIf = resMap.get("ReferenceId");
             String mndtId = (resMap.containsKey("MndtId")) ? resMap.get("MndtId") : "";
             String msgId = (resMap.containsKey("MsgId")) ? resMap.get("MsgId") : "";
            String npciXmlRespS3Url = resMap.get("s3Url");
             enachDetailsService.updateEnachDetailStatus(requestId,status,reason, reasonCd, npciRefIf,mndtId, accptd, msgId,npciXmlRespS3Url, respType);
             logToS3Service.saveLogDetails(requestId, npciXmlRespS3Url, NPCI_CALLBACK_API);
            String token = enachDetailsService.generateToken(requestId);
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectCallbackStatusUrl+token)).build();
        }
        log.error("NPCI Callback logging Failure as no NPCI Rquest Id {} found in collection,",requestId);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectCallbackStatusUrl)).build();
    }

    @CacheEvict({"live-banks"})
    @GetMapping("/clearCache")
    public ResponseEntity<Object> clearCache(){
        List<String> cachedAPIs =  cacheManager.getCacheNames().stream()
                .map(cacheName -> {
                    cacheManager.getCache(cacheName).clear();
                    return cacheName;
                }).toList();
        return ResponseHandler.responseBuilder("Successful cache reset", HttpStatus.OK,cachedAPIs);
    }

    @GetMapping("/enach-status")
    @PreAuthorize("hasAuthority('nach-int-readwrite')")
    public ResponseEntity<Map> getEnachStatus(Authentication authentication) {
        var requestId = jwtTokenUtil.getRequestIdByAuthenticationObj(authentication);
        log.info("Enach status api called for request id: {}", requestId);
        ResponseEntity<Map> response = enachRequestStatusService.processEnachStatusApi(requestId);
        log.info("Enach status api response for request id: {} ::\n{}", requestId, response);
        return response;
    }

    @PostMapping("/nach-search-advance")
    @PreAuthorize("hasAuthority('nach-ext-read')")
    public ResponseEntity<Object> searchAdvanceEnachDetails(@Valid @RequestBody EnachDtlSearchRqstBdy enachDtlSearchRqstBdy) {
        log.info("GET-ALL E-nach mandate Search request body {}", enachDtlSearchRqstBdy);
        var response = new HashMap<String, Object>();
        response = (HashMap<String, Object>) enachDetailsService.searchEnachDetailsByCriteria(enachDtlSearchRqstBdy);
       // response.put(REQUEST_ID, requestId);
       // response.put("enach_details", modelMapper.map(enachDetail, EnachDtlRespBdy.class));
        return ResponseHandler.responseBuilder(RESP_SUCCESS, HttpStatus.OK, response);
    }

    @PostMapping("/nach-search-basic")
    @PreAuthorize("hasAuthority('nach-ext-read')")
    public ResponseEntity<Object> searchBasicEnachDetails(@Validated(DirectSearch.class) @RequestBody EnachDtlSearchRqstBdy enachDtlSearchRqstBdy) {
        log.info("GET-ALL E-nach mandate Search request body {}", enachDtlSearchRqstBdy);
        var response = new HashMap<String, Object>();
        response = (HashMap<String, Object>) enachDetailsService.searchEnachDetailsByCriteria(enachDtlSearchRqstBdy);
        // response.put(REQUEST_ID, requestId);
        // response.put("enach_details", modelMapper.map(enachDetail, EnachDtlRespBdy.class));
        return ResponseHandler.responseBuilder(RESP_SUCCESS, HttpStatus.OK, response);
    }

}
