package com.arthmate.enachapi.repo;

import com.arthmate.enachapi.model.SingleDataTranslation;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SingleDataTranslationRepo {

    @Qualifier(value = "primaryMongoTemplate")
    private final MongoTemplate mongoTemplate;

    @Value("${enach.frequency.translation}")
    private String freqFlag;

    @Value("${enach.mandate.purpose.translation}")
    private String purposeFlag;


    /*
     * Get Frequency Master
     */
    public Optional<List<SingleDataTranslation>> getFrequencyList() {
        return Optional.ofNullable(mongoTemplate.find(Query.query(Criteria.where("type").is(freqFlag)),
                        SingleDataTranslation.class));
    }

    /*
     * Get Mandate Purpose List
     */
    public Optional<List<SingleDataTranslation>> getMandatePurposeList() {
        return Optional.ofNullable(mongoTemplate.find(Query.query(Criteria.where("type").is(purposeFlag)),
                SingleDataTranslation.class));
    }

    public Optional<SingleDataTranslation> findByKeyAndType(String type, String key) {
        return Optional.ofNullable(mongoTemplate.findOne(
                Query.query(Criteria.where("type").is(type).and("key").is(key)),
                SingleDataTranslation.class
        ));
    }

}
