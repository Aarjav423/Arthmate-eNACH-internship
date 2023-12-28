package com.arthmate.enachapi.batch.processor;

import com.arthmate.enachapi.dto.SmsResponseBody;
import com.arthmate.enachapi.model.NachTransactions;
import com.arthmate.enachapi.service.NachTransactionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;

import static com.arthmate.enachapi.utils.ApplicationConstants.NACH_SCHEDULE_TRANSACTION_STATUS_BATCH;
import static com.arthmate.enachapi.utils.ApplicationConstants.TRANSACTION_SUCCESS_SMS_BATCH;

@Component
@Slf4j
public class TransactionSuccessSmsProcessor implements ItemProcessor<NachTransactions, NachTransactions> {

    @Autowired
    private NachTransactionsService nachTransactionsService;

    @Override
    public NachTransactions process(NachTransactions txn) throws Exception {
        if ("S".equals(txn.getTxnStatus())) {
            SmsResponseBody smsResponse = nachTransactionsService.sendTransactionSuccessSms(txn);
            log.info("Transaction success(S) sms response:{},PresentmentTxnId{}", smsResponse, txn.getPresentmentTxnId());
            if (!ObjectUtils.isEmpty(smsResponse) && !ObjectUtils.isEmpty(smsResponse.getTransactionId())) {
                txn.setTxnSuccessSmsSentAt(LocalDateTime.now());
                txn.setTxnSuccessSmsSent(true);
                txn.setTxnSuccessSmsTxnId(smsResponse.getTransactionId());
                txn.setUpdatedBy(TRANSACTION_SUCCESS_SMS_BATCH);
            }
        }
        return txn;
    }
}