package com.arthmate.enachapi.controller;

import com.arthmate.enachapi.dto.EnachDtlRqstBdy;
import com.arthmate.enachapi.dto.EnachTxnRespBdy;
import com.arthmate.enachapi.dto.EnachTxnRqstBdy;
import com.arthmate.enachapi.dto.EnachTxnSearchRqstBdy;
import com.arthmate.enachapi.exception.NachTransactionNotFoundException;
import com.arthmate.enachapi.model.EnachDetail;
import com.arthmate.enachapi.model.NachTransactions;
import com.arthmate.enachapi.service.EnachDetailsService;
import com.arthmate.enachapi.service.NachTransactionsService;
import com.arthmate.enachapi.utils.ResponseHandler;
import com.arthmate.enachapi.utils.security.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import static com.arthmate.enachapi.utils.ApplicationConstants.*;

import javax.validation.Valid;
import java.util.HashMap;

import static com.arthmate.enachapi.utils.ApplicationConstants.SEQUENCE_NUMBER;
import static com.arthmate.enachapi.utils.ApplicationConstants.TRANSACTION_ID;
import static reactor.netty.Metrics.SUCCESS;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@CrossOrigin
public class NachTransactionsController {

    private final NachTransactionsService nachTransactionsService;
    private final EnachDetailsService enachDetailsService;
    private final ModelMapper modelMapper;
    private final JwtTokenUtil jwtTokenUtil;
    private final ModelMapper enachTransactionMapper;


    @PostMapping("/nach-transaction")
    @PreAuthorize("hasAuthority('nach-ext-write')")
    public ResponseEntity<Object> addNachTransaction(@Valid @RequestBody EnachTxnRqstBdy enachTxnRqst
            , Authentication authentication) {
        log.info("Add Nach Transaction request body {}",enachTxnRqst);
        var companyId = jwtTokenUtil.getCompanyIdByAuthenticationObj(authentication);
        var userId = jwtTokenUtil.getUserIdByAuthenticationObj(authentication);
        if(StringUtils.isNotBlank(companyId)){
            enachTxnRqst.setCompanyId(companyId);
        }
        if(StringUtils.isNotBlank(userId)){
            enachTxnRqst.setCreatedBy(userId);
        }
        var response = new HashMap<String,Object>();

        if(StringUtils.isNoneEmpty(enachTxnRqst.getOldPresentmentTxnId())){
            NachTransactions nachTransaction = nachTransactionsService.getNachTxnByRequest(enachTxnRqst.getOldPresentmentTxnId());
            if( nachTransaction != null && !nachTransaction.isRetry()){
                nachTransaction.setRetry(true);
                nachTransactionsService.updateNachTransaction(nachTransaction, enachTxnRqst.getOldPresentmentTxnId());
            }
        }

        NachTransactions nachTransaction = nachTransactionsService.addNewNachTxn(enachTransactionMapper.map(enachTxnRqst, NachTransactions.class));
        response.put(TRANSACTION_ID,nachTransaction.getPresentmentTxnId());
        if(StringUtils.isNoneEmpty(enachTxnRqst.getSequenceNumber()))
            response.put(SEQUENCE_NUMBER,enachTxnRqst.getSequenceNumber());
        return ResponseHandler.responseBuilder("Created Successfully", HttpStatus.OK,response);
    }

    @PutMapping("/nach-transaction/{txnId}")
    @PreAuthorize("hasAuthority('nach-ext-write')")
    public ResponseEntity<Object> updateNachTransaction(
            @PathVariable String txnId ,
            @Valid @RequestBody EnachTxnRqstBdy enachTxnRqst, Authentication authentication ) {
        var userId = jwtTokenUtil.getUserIdByAuthenticationObj(authentication);
        if(StringUtils.isNotBlank(userId)){
            enachTxnRqst.setUpdatedBy(userId);
        }
        log.info("Update Nach Transaction request body {}",enachTxnRqst);
        var response = new HashMap<String,Object>();
        NachTransactions nachTransaction = nachTransactionsService.updateNachTransaction(modelMapper.map(enachTxnRqst, NachTransactions.class), txnId);
        response.put(TRANSACTION_ID,nachTransaction.getPresentmentTxnId());
        return ResponseHandler.responseBuilder("Updated Successfully", HttpStatus.OK,response);
    }

    @GetMapping("/nach-transaction/{txnId}")
    @PreAuthorize("hasAuthority('nach-ext-read')")
    public ResponseEntity<Object> getNachTransactionTxnId(@PathVariable String txnId) throws NachTransactionNotFoundException {
        log.info("Get Nach Transaction by txn_id: {}",txnId);
        var response = new HashMap<String,Object>();
        NachTransactions nachTransaction = nachTransactionsService.getNachTxnByRequest(txnId);
        if(nachTransaction == null){
            throw new NachTransactionNotFoundException("Record not found with transaction_id: " + txnId);
        }
        String transactionRqstId = nachTransaction.getSubscriptionId();
        EnachDetail enachDetails = enachDetailsService.getEnachDetailByRequest(transactionRqstId);
        if(enachDetails == null) enachDetails = new EnachDetail();
        response.put("customer_name", enachDetails.getCustomerName());
        response.put("customer_email_id", enachDetails.getCustomerEmailId());
        response.put("customer_mobile_no", enachDetails.getCustomerMobileNo());
        response.put("external_ref_num", enachDetails.getExternalRefNum());
        response.put("transaction_details",modelMapper.map(nachTransaction, EnachTxnRespBdy.class));
        return ResponseHandler.responseBuilder(SUCCESS, HttpStatus.OK,response);
    }

    @PostMapping("/npci-transaction-request-search")
    @PreAuthorize("hasAuthority('nach-ext-read')")
    public ResponseEntity<Object> getAllEnachTransactionsDetails(@Valid @RequestBody EnachTxnSearchRqstBdy enachTxnSearchRqstBdy) {
        log.info("GET-ALL E-nach transaction Search request body {}", enachTxnSearchRqstBdy);
        var response = new HashMap<String, Object>();
        response = (HashMap<String, Object>) nachTransactionsService.searchEnachDetailsByCriteria(enachTxnSearchRqstBdy);
        return ResponseHandler.responseBuilder(RESP_SUCCESS, HttpStatus.OK, response);
    }
}
