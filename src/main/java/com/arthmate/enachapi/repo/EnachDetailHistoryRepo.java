package com.arthmate.enachapi.repo;

import com.arthmate.enachapi.model.EnachDetailHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.arthmate.enachapi.model.enums.CollectionNames.ENACH_DETAILS_HISTORY;

@Repository
@RequiredArgsConstructor
@Slf4j
public class EnachDetailHistoryRepo {

    @Qualifier(value = "primaryMongoTemplate")
    private final MongoTemplate mongoTemplate;

    public EnachDetailHistory save(EnachDetailHistory enachDetailClnObj) {
        return mongoTemplate.insert(enachDetailClnObj);
    }

    public boolean isNPCIRequestIdExists(String request_id, String npci_request_id){
        Query query = Query.query(Criteria.where("request_id").is(request_id).and("npci_request_id").is(npci_request_id));
        return mongoTemplate.exists(query,ENACH_DETAILS_HISTORY.getName());
    }

    public Optional<EnachDetailHistory> getEnachDetailsByRequestId(String request_id, String npci_request_id){
        Query query = Query.query(Criteria.where("request_id").is(request_id).and("npci_request_id").is(npci_request_id));
        return Optional.ofNullable(mongoTemplate.findOne(query,EnachDetailHistory.class));
    }

    public List<EnachDetailHistory> getDescOrderdListOfEnachDetailHistory(String request_id){
        Query query = Query.query(Criteria.where("request_id.req_id").is(request_id)).with(Sort.by(Sort.Direction.DESC, "request_id.timestamp"));
        return mongoTemplate.find(query, EnachDetailHistory.class);
    }

    public Collection<EnachDetailHistory> insertAll(List<EnachDetailHistory> items) {
        return mongoTemplate.insertAll(items);
    }
}
