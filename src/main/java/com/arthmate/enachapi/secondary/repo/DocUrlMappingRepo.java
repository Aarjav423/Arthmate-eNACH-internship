package com.arthmate.enachapi.secondary.repo;

import com.arthmate.enachapi.secondary.model.DocUrlMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DocUrlMappingRepo {

    @Autowired
    @Qualifier(value = "secondaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    public int insert(List<DocUrlMapping> items) {
        if (items == null || items.isEmpty()) {
            return 0;
        }
        return mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, DocUrlMapping.class)
                .insert(items)
                .execute()
                .getInsertedCount();
    }

    public Optional<DocUrlMapping> findByDocumentId(String documentId) {
        return Optional.ofNullable(mongoTemplate.findOne(Query.query(Criteria.where("document_id").is(documentId)),
                DocUrlMapping.class));
    }

}
