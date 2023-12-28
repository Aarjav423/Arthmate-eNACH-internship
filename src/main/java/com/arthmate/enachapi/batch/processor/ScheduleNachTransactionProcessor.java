package com.arthmate.enachapi.batch.processor;

import com.arthmate.enachapi.client.UtilApiClient;
import com.arthmate.enachapi.dto.ScheduleTransactionResponse;
import com.arthmate.enachapi.model.EnachDetail;
import com.arthmate.enachapi.model.NachTransactions;
import com.arthmate.enachapi.repo.EnachDetailsRepo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static com.arthmate.enachapi.model.enums.NachTransactionBatchStatus.PROCESSED;
import static com.arthmate.enachapi.utils.ApplicationConstants.NACH_SCHEDULE_TRANSACTION_BATCH;

@Component
@Slf4j
public class ScheduleNachTransactionProcessor implements ItemProcessor<NachTransactions, NachTransactions> {

    @Autowired
    private UtilApiClient utilApiClient;

    @Value("${npci.merchant.id}")
    private String merchantId;
    @Value("${icici.schedule.txn.url}")
    private String iciciScheduleTransactionUrl;
    @Value("${icici.scheme.code}")
    private String schemeCode;
    @Autowired
    private EnachDetailsRepo enachDetailsRepo;

    @Override
    public NachTransactions process(NachTransactions transaction) {
        log.info("Scheduling nach transaction for presentment_txn_id: {}", transaction.getPresentmentTxnId());
        Optional<EnachDetail> enachDetail = enachDetailsRepo.getEnachDetailsByRequestId(transaction.getSubscriptionId());
        if (enachDetail.isPresent() && "active".equals(enachDetail.get().getStatus())) {

            String responseString = utilApiClient.callRestPostApi(prepareTransactionSchedulePayload(transaction), iciciScheduleTransactionUrl);
            if (StringUtils.isNotBlank(responseString)) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                    ScheduleTransactionResponse response = mapper.readValue(responseString, ScheduleTransactionResponse.class);
                    if (!ObjectUtils.isEmpty(response) && !ObjectUtils.isEmpty(response.getMerchantTransactionIdentifier())) {
                        ScheduleTransactionResponse.PaymentTransaction txn = response.getPaymentMethod().getPaymentTransaction();
                        transaction.setPaymentDatetime(txn.getDateTime());
                        transaction.setPaymentTxnId(txn.getIdentifier());
                        transaction.setTxnErrorMsg(txn.getErrorMessage());
                        transaction.setTxnStatus(txn.getStatusMessage());
                        transaction.setStatus(PROCESSED.label());
                        transaction.setRemarks("success");
                        transaction.setUpdatedDate(LocalDateTime.now());
                        transaction.setUpdatedBy(NACH_SCHEDULE_TRANSACTION_BATCH);
                    } else {
                        log.error("Response not present or presentment_txn id not present in response for presentment_txn_id: {}",
                                transaction.getPresentmentTxnId());
                        transaction.setRemarks("Response not present or presentment_txn id not present in response");
                    }
                } catch (Exception e) {
                    log.error("Exception occurred while mapping object in ScheduleNachTransactionProcessor for txn {}", transaction.getPresentmentTxnId());
                    transaction.setRemarks("Api response: " + responseString + "; Exception: " + e.getMessage());
                }
            } else {
                log.error("Response not received from txn scheduling api for subscription id: {}", transaction.getSubscriptionId());
                transaction.setRemarks("Response not received from txn scheduling api");
            }
        } else {
            log.info("Subscription is not active. subscriptionId {}", transaction.getSubscriptionId());
            transaction.setStatus(PROCESSED.label());
            transaction.setTxnStatus("F");
            transaction.setTxnErrorMsg("Subscription is not active");
            transaction.setRemarks("Subscription is not active");
            transaction.setUpdatedDate(LocalDateTime.now());
            transaction.setUpdatedBy(NACH_SCHEDULE_TRANSACTION_BATCH);
        }
        return transaction;
    }

    private String prepareTransactionSchedulePayload(NachTransactions details) {
        ObjectNode requestPayload = new ObjectMapper().createObjectNode();

        ObjectNode merchant = new ObjectMapper().createObjectNode();
        merchant.put("identifier", merchantId);

        String transactionId = details.getPresentmentTxnId();
        ObjectNode transaction = new ObjectMapper().createObjectNode();
        transaction.put("deviceIdentifier", "S");
        transaction.put("identifier", transactionId);
        transaction.put("type", "002");
        transaction.put("subType", "003");
        transaction.put("requestType", "TSI");
        transaction.put("currency", "INR");

        ObjectNode payment = new ObjectMapper().createObjectNode();

        ObjectNode instrument = new ObjectMapper().createObjectNode();
        instrument.put("identifier", schemeCode);

        ObjectNode instruction = new ObjectMapper().createObjectNode();
        instruction.put("amount", details.getAmount().doubleValue());
        instruction.put("endDateTime", details.getScheduledOn().format(DateTimeFormatter.ofPattern("ddMMyyyy")));
        instruction.put("identifier", details.getMandateId());

        payment.put("instrument", instrument);
        payment.put("instruction", instruction);

        requestPayload.put("merchant", merchant);
        requestPayload.put("payment", payment);
        requestPayload.put("transaction", transaction);

        log.info("requestPayload:{}", requestPayload.toPrettyString());

        return requestPayload.toString();
    }
}
