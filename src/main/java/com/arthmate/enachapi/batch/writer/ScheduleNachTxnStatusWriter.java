package com.arthmate.enachapi.batch.writer;

import com.arthmate.enachapi.model.NachTransactions;
import com.arthmate.enachapi.service.NachTransactionsService;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ScheduleNachTxnStatusWriter extends MongoItemWriter<NachTransactions> {

    @Autowired
    private NachTransactionsService nachTransactionsService;

    @Override
    public void write(List<? extends NachTransactions> items) throws Exception {
        nachTransactionsService.updateScheduleNachTxnStatus(items);
    }

}
