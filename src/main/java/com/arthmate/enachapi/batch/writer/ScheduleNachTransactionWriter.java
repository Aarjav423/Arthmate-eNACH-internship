package com.arthmate.enachapi.batch.writer;

import com.arthmate.enachapi.model.NachTransactions;
import com.arthmate.enachapi.repo.NachTransactionsRepo;

import com.arthmate.enachapi.service.NachTransactionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;

@Slf4j
public class ScheduleNachTransactionWriter extends MongoItemWriter<NachTransactions> {

    @Autowired
    private NachTransactionsRepo repo;
    @Autowired
    private NachTransactionsService service;

    @Override
    public void write(List<? extends NachTransactions> items) throws Exception {
        log.info("Scheduled transactions to update : {}", items.size());

        int updates = repo.updateByQueryUpdatePair(items.stream()
                .filter(Objects::nonNull)
                .map(service::getScheduleNachTxnUpdateQuery)
                .toList());

        log.info("Number of nach txn schedule records updated: {}", updates);
    }
}
