package com.arthmate.enachapi.batch.processor;

import com.arthmate.enachapi.model.NachTransactions;
import com.arthmate.enachapi.service.NachTransactionsService;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.arthmate.enachapi.utils.ApplicationConstants.NACH_SCHEDULE_TRANSACTION_STATUS_BATCH;

@Component
public class ScheduleNachTxnStatusProcessor implements ItemProcessor<NachTransactions, NachTransactions> {

    @Autowired
    private NachTransactionsService nachTransactionsService;

    @Override
    public NachTransactions process(NachTransactions item) throws Exception {
        return nachTransactionsService.checkScheduleNachTxnStatus(item, NACH_SCHEDULE_TRANSACTION_STATUS_BATCH);
    }

}
