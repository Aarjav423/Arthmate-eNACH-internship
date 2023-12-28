package com.arthmate.enachapi.batch.writer;

import com.arthmate.enachapi.model.NachTransactions;
import com.arthmate.enachapi.repo.NachTransactionsRepo;
import com.arthmate.enachapi.service.NachTransactionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class TransactionSuccessSmsWriter extends MongoItemWriter<NachTransactions> {
    @Autowired
    private NachTransactionsRepo repo;

    @Override
    public void write(List<? extends NachTransactions> items) throws Exception {
        log.info("Transactions are: {}", items.size());

        List<NachTransactions> transactions = items.stream()
                .filter(txn -> Objects.nonNull(txn) && !ObjectUtils.isEmpty(txn.getPresentmentTxnId())
                        && "S".equals(txn.getTxnStatus()))
                .collect(Collectors.toList());
        log.info("NonNull success transactions are: {}", transactions.size());
        transactions.forEach(txn -> repo.updateTransaction(txn));
        log.info(" End of TransactionSuccessSmsWriter");
    }
}
