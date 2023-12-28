package com.arthmate.enachapi.service;

import com.arthmate.enachapi.client.UtilApiClient;
import com.arthmate.enachapi.dto.ScheduleTransactionResponse;

import com.arthmate.enachapi.dto.EnachTxnSearchRqstBdy;

import com.arthmate.enachapi.dto.SmsRequestBody;
import com.arthmate.enachapi.dto.SmsResponseBody;
import com.arthmate.enachapi.model.EnachDetail;
import com.arthmate.enachapi.model.NachTransactions;
import com.arthmate.enachapi.repo.EnachDetailsRepo;
import com.arthmate.enachapi.repo.NachTransactionsRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Objects;
import java.util.HashMap;
import java.util.Map;

import java.util.Optional;
import java.util.stream.Collectors;

import static com.arthmate.enachapi.model.enums.NachTransactionBatchStatus.NEW;

@Slf4j
@Service
@RequiredArgsConstructor
public class NachTransactionsService {

    private final NachTransactionsRepo nachTransactionsRepo;
    private final UtilApiClient utilApiClient;
    private final EnachDetailsRepo enachDetailsRepo;
    @Value("${txn.success.sms.sender}")
    private String txnSmsSender;
    @Value("${txn.success.sms.template}")
    private String txnSuccessSmsTemplate;
    @Value("${npci.merchant.id}")
    private String merchantId;
    @Value("${icici.schedule.txn.url}")
    private String iciciScheduleTransactionUrl;

    public NachTransactions addNewNachTxn(NachTransactions nachTxn) {
        nachTxn.setPresentmentTxnId(prepareAmTransactionId(nachTxn.getMandateId()));
        nachTxn.setStatus(NEW.name());
        nachTxn.setCreatedAt(LocalDateTime.now());
        nachTxn.setUpdatedDate(LocalDateTime.now());
        nachTransactionsRepo.save(nachTxn);
        return nachTxn;
    }

    public NachTransactions updateNachTransaction(NachTransactions nachTxn, String txnId) {
        nachTxn.setPresentmentTxnId(txnId);
        nachTxn.setUpdatedDate(LocalDateTime.now());
        nachTransactionsRepo.update(nachTxn);
        return nachTxn;
    }

    public NachTransactions getNachTxnByRequest(String txnId){
        NachTransactions nachTxn = null;
        Optional<NachTransactions> nachTxnOpt = nachTransactionsRepo.getNachTransactionByTxnId(txnId);
        if(nachTxnOpt.isPresent())
            nachTxn = nachTxnOpt.get();
        else
            log.info("NachTransactions not found for transaction_id {}",txnId);
        return nachTxn;
    }

    public boolean isNachTransactionExists(String txnId){
        return nachTransactionsRepo.isNachTransactionExists(txnId);
    }

    public Map<String,Object> searchEnachDetailsByCriteria(EnachTxnSearchRqstBdy requestBdy) {
        List<BasicDBObject> results;

        // if searchBy is present then search with it first
        // search nach_transaction first then get data from enach_details
        if (StringUtils.isNotBlank(requestBdy.getSearchBy())) {
            results = nachTransactionsRepo.searchNachTransactionBySearchKey(requestBdy.getSearchBy());

            if (CollectionUtils.isEmpty(results)) {
                results = enachDetailsRepo.txnSearchByExternalRefNum(requestBdy.getSearchBy());
            }

        } else {
            results = nachTransactionsRepo.searchEnachDetailsByCriteria(requestBdy);
        }

        var rcCnt = results.size();
        var offset = (requestBdy.getPage() - 1) * requestBdy.getLimit();
        var dataLst = results.stream().skip(offset).limit(requestBdy.getLimit()).collect(Collectors.toList());

        Map<String, Object> resp = new HashMap<>() {{
            put("data-lst", dataLst);
            put("page-cnt", (rcCnt % requestBdy.getLimit() == 0) ? rcCnt / requestBdy.getLimit() : rcCnt / requestBdy.getLimit() + 1);
            put("data-lst-size", dataLst.size());
            put("total-records", rcCnt);
        }};
        return resp;
    }

    private String prepareAmTransactionId(String mndtId){
        return mndtId + System.currentTimeMillis();

    }

    public NachTransactions checkScheduleNachTxnStatus(NachTransactions transaction, String calledBy) {
        log.info("Checking scheduled nach txn status for presentment id: {}", transaction.getPresentmentTxnId());
        String response = utilApiClient.callRestPostApi(buildScheduleNachTxnStatusReq(transaction), iciciScheduleTransactionUrl);
        if (response != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                ScheduleTransactionResponse resObj = mapper.readValue(response, ScheduleTransactionResponse.class);
                ScheduleTransactionResponse.PaymentTransaction txn = resObj.getPaymentMethod().getPaymentTransaction();
                if ( txn != null) {
                    if (StringUtils.isNotBlank(txn.getStatusMessage())) {
                        transaction.setTxnStatus(txn.getStatusMessage());
                    }
                    transaction.setTxnErrorMsg(txn.getErrorMessage());
                    transaction.setUpdatedDate(LocalDateTime.now());
                    transaction.setUpdatedBy(calledBy);
                }
                return transaction;
            } catch (Exception e) {
                log.error("Error while fetching status for scheduled transaction for presentment id: {}",
                        transaction.getPresentmentTxnId(), e);
            }
        }
        return null;
    }

    private String buildScheduleNachTxnStatusReq(NachTransactions nachTransaction) {
        ObjectNode merchant = new ObjectMapper().createObjectNode();
        merchant.put("identifier", merchantId);

        ObjectNode transaction = new ObjectMapper().createObjectNode();
        transaction.put("deviceIdentifier", "S");
        transaction.put("identifier", nachTransaction.getPresentmentTxnId());
        transaction.put("dateTime", nachTransaction.getPaymentDatetime());
        transaction.put("type", "002");
        transaction.put("subType", "004");
        transaction.put("requestType", "TSI");
        transaction.put("currency", "INR");

        ObjectNode req = new ObjectMapper().createObjectNode();
        req.put("merchant", merchant);
        req.put("transaction", transaction);

        return req.toString();
    }

    public void updateScheduleNachTxnStatus(List<? extends NachTransactions> items) {
        int count = nachTransactionsRepo.updateByQueryUpdatePair(items.stream()
                        .filter(Objects::nonNull)
                        .map(this::getScheduleNachTxnStatusUpdateQuery)
                        .toList());
        log.info("Number of nach_transactions updated with latest status: {}", count);
    }

    private Pair<Query, Update> getScheduleNachTxnStatusUpdateQuery(NachTransactions transaction) {
        return Pair.of(Query.query(Criteria.where("presentment_txn_id").is(transaction.getPresentmentTxnId())),
                Update.update("txn_status", transaction.getTxnStatus())
                        .set("txn_error_msg", transaction.getTxnErrorMsg())
                        .set("updated_by", transaction.getUpdatedBy())
                        .set("updated_at", transaction.getUpdatedDate())
        );
    }

    public SmsResponseBody sendTransactionSuccessSms(NachTransactions transaction) {
        try {
            Optional<EnachDetail> enachDetail = enachDetailsRepo.getEnachDetailsByRequestId(transaction.getSubscriptionId());
            if (enachDetail.isPresent()) {
                SmsRequestBody request = SmsRequestBody.builder()
                        .from(txnSmsSender)
                        .recipient(enachDetail.get().getCustomerMobileNo())
                        .message(getTxnSmsTemplate(transaction, enachDetail.get()))
                        .build();

                return utilApiClient.callSmsApi(request);
            } else {
                log.info("Subscription details not found requestId:{}", transaction.getSubscriptionId());
            }
        } catch (Exception e) {
            log.error("Exception occurred while sending sms to txnPresentmentId:{}, error:{}", transaction.getPresentmentTxnId(), e.getMessage());
        }
        return null;
    }

    private String getTxnSmsTemplate(NachTransactions transaction, EnachDetail enachDetail) {
        String smsTemplate = txnSuccessSmsTemplate;
        smsTemplate = smsTemplate.formatted(enachDetail.getMndtId(), enachDetail.getPurposeOfMandate(), enachDetail.getCorporateName(),
                enachDetail.getAmount().setScale(2, RoundingMode.DOWN), transaction.getPaymentDatetime(),
                enachDetail.getAccountNo().substring(enachDetail.getAccountNo().length() - 4), enachDetail.getBank());
        return smsTemplate;
    }

    public Pair<Query, Update> getScheduleNachTxnUpdateQuery(NachTransactions transaction) {
        return Pair.of(Query.query(Criteria.where("presentment_txn_id").is(transaction.getPresentmentTxnId())),
                Update.update("txn_status", transaction.getTxnStatus())
                        .set("txn_error_msg", transaction.getTxnErrorMsg())
                        .set("remarks", transaction.getRemarks())
                        .set("payment_txn_id", transaction.getPaymentTxnId())
                        .set("payment_datetime", transaction.getPaymentDatetime())
                        .set("status", transaction.getStatus())
                        .set("updated_by", transaction.getUpdatedBy())
                        .set("updated_at", transaction.getUpdatedDate())
        );
    }

}
