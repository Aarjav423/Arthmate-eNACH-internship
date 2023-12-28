package com.arthmate.enachapi.repo;

import com.arthmate.enachapi.model.EnachReqResLog;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class EnachReqResLogRepo {

    @Qualifier(value = "primaryMongoTemplate")
    private final MongoTemplate mongoTemplate;

    public EnachReqResLog insert(EnachReqResLog item) {
        return mongoTemplate.insert(item);
    }

    public Collection<EnachReqResLog> insertAll(List<EnachReqResLog> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        return mongoTemplate.insertAll(items);
    }

}
