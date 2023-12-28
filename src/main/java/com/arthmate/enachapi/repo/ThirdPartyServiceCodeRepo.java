package com.arthmate.enachapi.repo;

import com.arthmate.enachapi.model.ThirdPartyServiceCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ThirdPartyServiceCodeRepo {

    @Qualifier(value = "primaryMongoTemplate")
    private final MongoTemplate mongoTemplate;

    public Optional<ThirdPartyServiceCode> findByServiceAndServiceCodeId(String service, String serviceCodeId) {
        return Optional.ofNullable(mongoTemplate.findOne(
                Query.query(Criteria.where("service").is(service)
                        .and("service_code_id").is(serviceCodeId)),
                ThirdPartyServiceCode.class)
        );
    }

    public Optional<ThirdPartyServiceCode>  findByServiceServiceCodeAndCategory(String service,
                                                                                String category,
                                                                                String serviceCodeId) {
        return Optional.ofNullable(mongoTemplate.findOne(
                Query.query(Criteria.where("service").is(service)
                        .and("code_category").is(category)
                        .and("service_code_id").is(serviceCodeId)),
                ThirdPartyServiceCode.class)
        );
    }

}
