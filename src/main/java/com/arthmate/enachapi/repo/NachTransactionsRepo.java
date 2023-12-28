package com.arthmate.enachapi.repo;

import com.arthmate.enachapi.dto.EnachTxnSearchRqstBdy;
import com.arthmate.enachapi.model.NachTransactions;
import com.mongodb.BasicDBObject;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.BulkOperations;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.arthmate.enachapi.model.enums.CollectionNames.ENACH_DETAILS;
import static com.arthmate.enachapi.model.enums.CollectionNames.NACH_TRANSACTIONS;

@Repository
@RequiredArgsConstructor
@Slf4j
public class NachTransactionsRepo {

    @Qualifier(value = "primaryMongoTemplate")
    private final MongoTemplate mongoTemplate;

    private final String[] txnSearchFieldsInclude = {
            "amount", "mandate_id", "scheduled_on", "presentment_txn_id", "request_id", "company_id",
            "txn_request_date", "txn_status", "txn_error_msg", "txn_utr_number", "txn_utr_datetime",
            "payment_txn_id", "payment_datetime", "remarks", "retry"
    };

    public NachTransactions save(NachTransactions nachTxn) {
        return mongoTemplate.insert(nachTxn);
    }

    public UpdateResult update(NachTransactions nachTxn) {
        Query query = Query.query(Criteria.where("presentment_txn_id").is(nachTxn.getPresentmentTxnId()));
        Update update = new Update();

        if (nachTxn.getMandateId() != null) update.set("mandate_id", nachTxn.getMandateId());
        if (nachTxn.getAmount() != null) update.set("amount", nachTxn.getAmount());
        if (nachTxn.getStatus() != null) update.set("status", nachTxn.getStatus());
        if (nachTxn.getSubscriptionId() != null) update.set("request_id", nachTxn.getSubscriptionId());
        if (nachTxn.getScheduledOn() != null) update.set("scheduled_on", nachTxn.getScheduledOn());
        if (nachTxn.getCompanyId() != null) update.set("company_id", nachTxn.getCompanyId());
        if (nachTxn.getUpdatedBy() != null) update.set("updated_by", nachTxn.getUpdatedBy());
        if (nachTxn.getRemarks() != null) update.set("remarks", nachTxn.getRemarks());
        if (nachTxn.isRetry()) update.set("retry", nachTxn.isRetry());

        return mongoTemplate.updateFirst(query, update, NachTransactions.class, NACH_TRANSACTIONS.getName());
    }


    public UpdateResult updateTransaction(NachTransactions transaction) {
        Query query = Query.query(Criteria.where("presentment_txn_id").is(transaction.getPresentmentTxnId()));
        Update update = new Update();
        if (transaction.getUpdatedBy() != null) update.set("updated_by", transaction.getUpdatedBy());
        if (transaction.getRemarks() != null) update.set("remarks", transaction.getRemarks());
        if (transaction.getUpdatedDate() != null) update.set("updated_at", LocalDateTime.now());
        if (transaction.getPaymentTxnId() != null) update.set("payment_txn_id", transaction.getPaymentTxnId());
        if (transaction.getPaymentDatetime() != null) update.set("payment_datetime", transaction.getPaymentDatetime());
        if (transaction.getTxnStatus() != null) update.set("txn_status", transaction.getTxnStatus());
        if (transaction.getTxnErrorMsg() != null) update.set("txn_error_msg", transaction.getTxnErrorMsg());
        if (transaction.getStatus() != null) update.set("status", transaction.getStatus());
        if (transaction.getTxnSuccessSmsSentAt() != null) update.set("txn_success_sms_sent_at", transaction.getTxnSuccessSmsSentAt());
        if (transaction.getTxnSuccessSmsTxnId() != null) update.set("txn_success_sms_txn_id", transaction.getTxnSuccessSmsTxnId());
        update.set("is_txn_success_sms_sent", transaction.isTxnSuccessSmsSent());

        return mongoTemplate.updateFirst(query, update, NachTransactions.class, NACH_TRANSACTIONS.getName());
    }

    public Optional<NachTransactions> getNachTransactionByTxnId(String txn_id) {
        Query query = Query.query(Criteria.where("presentment_txn_id").is(txn_id));
        return Optional.ofNullable(mongoTemplate.findOne(query, NachTransactions.class));
    }

    public boolean isNachTransactionExists(String txn_id){
        Query query = Query.query(Criteria.where("presentment_txn_id").is(txn_id));
        return mongoTemplate.exists(query,NACH_TRANSACTIONS.getName());
    }

    public int updateByQueryUpdatePair(List<Pair<Query, Update>> updateList) {
        if (updateList == null || updateList.isEmpty()) {
            return 0;
        }
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, NachTransactions.class);
        return bulkOps.updateOne(updateList).execute().getModifiedCount();
    }

    public List<BasicDBObject> searchNachTransactionBySearchKey(String searchKey) {
        List<AggregationOperation> pipeline = new ArrayList<>();
        pipeline.add(Aggregation.match(
                new Criteria().orOperator(
                        Criteria.where("mandate_id").is(searchKey),
                        Criteria.where("presentment_txn_id").is(searchKey),
                        Criteria.where("request_id").is(searchKey)
                )
        ));
        pipeline.add(Aggregation.lookup(ENACH_DETAILS.getName(), "request_id", "request_id", "enach"));
        pipeline.add(Aggregation.unwind("enach"));
        pipeline.add(Aggregation.project(txnSearchFieldsInclude)
                .and("enach.external_ref_num").as("external_ref_num"));

        return mongoTemplate.aggregate(Aggregation.newAggregation(pipeline), NACH_TRANSACTIONS.getName(), BasicDBObject.class)
                .getMappedResults();

    }

    public List<BasicDBObject> searchEnachDetailsByCriteria(EnachTxnSearchRqstBdy requestBdy) {
        List<AggregationOperation> pipeline = new ArrayList<>();
        Criteria criteria = new Criteria();

        if (StringUtils.isNotBlank(requestBdy.getCompanyId())) {
            criteria.and("company_id").is(Integer.valueOf(requestBdy.getCompanyId()));
        }
        if (StringUtils.isNotBlank(requestBdy.getStatus())) {
            criteria.and("txn_status").is(requestBdy.getStatus());
        }
        if (StringUtils.isNotBlank(requestBdy.getFromDate()) && StringUtils.isNotBlank(requestBdy.getToDate())) {
            criteria.and("created_at")
                    .gte(LocalDate.parse(requestBdy.getFromDate()).atStartOfDay())
                    .lte(LocalDate.parse(requestBdy.getToDate()).atTime(23, 59, 59));
        }

        pipeline.add(Aggregation.match(criteria));
        pipeline.add(Aggregation.lookup(ENACH_DETAILS.getName(),"request_id","request_id","enach"));
        pipeline.add(Aggregation.unwind("enach"));
        pipeline.add(Aggregation.sort(Sort.Direction.DESC,"created_at"));
        pipeline.add(Aggregation.project(txnSearchFieldsInclude)
                .and("enach.external_ref_num").as("external_ref_num"));

        return mongoTemplate.aggregate(Aggregation.newAggregation(pipeline), NACH_TRANSACTIONS.getName(), BasicDBObject.class)
                .getMappedResults();
    }

}
