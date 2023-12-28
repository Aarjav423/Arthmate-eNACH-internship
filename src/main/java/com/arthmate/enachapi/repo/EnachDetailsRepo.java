package com.arthmate.enachapi.repo;

import com.arthmate.enachapi.dto.EnachDtlSearchRqstBdy;
import com.arthmate.enachapi.dto.EnachMandateDetails;
import com.arthmate.enachapi.model.EnachDetail;
import com.mongodb.BasicDBObject;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.arthmate.enachapi.model.enums.CollectionNames.ENACH_DETAILS;
import static com.arthmate.enachapi.model.enums.CollectionNames.NACH_TRANSACTIONS;

@Repository
@RequiredArgsConstructor
@Slf4j
public class EnachDetailsRepo {

    @Qualifier(value = "primaryMongoTemplate")
    private final MongoTemplate mongoTemplate;

    public EnachDetail save(EnachDetail enachDetail) {
        return mongoTemplate.insert(enachDetail);
    }

    public UpdateResult updateStatusToNotified(ObjectId Id){
        Query query = Query.query(Criteria.where("_id").is(Id));
        Update update = new Update();
        update.set("status_notified",1);
        return mongoTemplate.updateFirst(query, update, EnachMandateDetails.class ,ENACH_DETAILS.getName());
    }

    public UpdateResult updateStatusToFailed(ObjectId Id){
        Query query = Query.query(Criteria.where("_id").is(Id));
        Update update = new Update();
        update.set("status_notified",2);
        return mongoTemplate.updateFirst(query, update, EnachMandateDetails.class ,ENACH_DETAILS.getName());
    }

    public UpdateResult update(EnachDetail enachDetail) {
        Query query = Query.query(Criteria.where("request_id").is(enachDetail.getRequestId()));
        Update update = new Update();
        if(enachDetail.getCustomerTitle() != null) update.set("customer_title", enachDetail.getCustomerTitle());
        if(enachDetail.getCustomerName() != null) update.set("customer_name", enachDetail.getCustomerName());
        if(enachDetail.getCustomerEmailId() != null) update.set("customer_email_id", enachDetail.getCustomerEmailId());
        if(enachDetail.getCustomerMobileCode() != null) update.set("customer_mobile_code", enachDetail.getCustomerMobileCode());
        if(enachDetail.getCustomerMobileNo() != null) update.set("customer_mobile_no", enachDetail.getCustomerMobileNo());
        if(enachDetail.getCustomerTelephoneCode() != null) update.set("customer_telephone_code", enachDetail.getCustomerTelephoneCode());
        if(enachDetail.getCustomerTelephoneNo() != null) update.set("customer_telephone_no", enachDetail.getCustomerTelephoneNo());
        if(enachDetail.getCustomerPAN() != null) update.set("customer_pan", enachDetail.getCustomerPAN());
        if(enachDetail.getAccountNo() != null) update.set("account_no", enachDetail.getAccountNo());
        if(enachDetail.getAccountType() != null) update.set("account_type", enachDetail.getAccountType());
        if(enachDetail.getAmount() != null) update.set("amount", enachDetail.getAmount());
        if(enachDetail.getAmountType() != null) update.set("amount_type", enachDetail.getAmountType());
        if(enachDetail.getEnachReason() != null) update.set("enach_reason", enachDetail.getEnachReason());
        if(enachDetail.getEmiFrequency() != null) update.set("emi_frequency", enachDetail.getEmiFrequency());
        if(enachDetail.getPurposeOfMandate() != null) update.set("purpose_of_mandate", enachDetail.getPurposeOfMandate());
        if(enachDetail.getBank() != null) update.set("bank", enachDetail.getBank());
        if(enachDetail.getAuthenticationMode() != null) update.set("authentication_mode", enachDetail.getAuthenticationMode());
        if(enachDetail.getCorporateName() != null) update.set("corporate_name", enachDetail.getCorporateName());
        if(enachDetail.getUtilityNumber() != null) update.set("utility_number", enachDetail.getUtilityNumber());
        if(enachDetail.getReferenceNumber() != null) update.set("reference_number", enachDetail.getReferenceNumber());
        if(enachDetail.getExternalRefNum() != null) update.set("external_ref_num", enachDetail.getExternalRefNum());
        if(enachDetail.getConsent() != null) update.set("consent", enachDetail.getConsent());
        if(enachDetail.getConsentTimestamp() != null) update.set("consent_timestamp", enachDetail.getConsentTimestamp());
        if(enachDetail.getStartDate() != null) update.set("start_date", enachDetail.getStartDate());
        if(enachDetail.getEndDate() != null) update.set("end_date", enachDetail.getEndDate());
        if(enachDetail.getUpdatedDate() != null) update.set("updated_at", enachDetail.getUpdatedDate());
        if(enachDetail.getStatus() != null) {
            update.set("status", enachDetail.getStatus());
            update.set("status_notified", 0);
        }
        if(enachDetail.getMndtId() != null) update.set("mandate_id", enachDetail.getMndtId());
        if(enachDetail.getMsgId() != null) update.set("msg_id", enachDetail.getMsgId());
        if(enachDetail.getCompanyId() != null) update.set("company_id", enachDetail.getCompanyId());
        if(enachDetail.isSmsRequired()){update.set("is_sms_required", true);} else{update.set("is_sms_required", false);}
        if(enachDetail.isEmailRequired()){update.set("is_email_required", true);} else{update.set("is_email_required", false);}
        if(enachDetail.isSmsSent()){update.set("is_sms_sent", true);} else{update.set("is_sms_sent", false);}
        if(enachDetail.isEmailSent()){update.set("is_email_sent", true);} else{update.set("is_email_sent", false);}
        if(enachDetail.getRejectReason() != null) update.set("reject_reason", enachDetail.getRejectReason());
        if(enachDetail.isAccptd()) update.set("accptd", enachDetail.isAccptd());
        if(enachDetail.getNpciRequestId() != null) update.set("npci_request_id", enachDetail.getNpciRequestId());
        if (enachDetail.getUpdatedBy() != null) update.set("updated_by", enachDetail.getUpdatedBy());
        if (enachDetail.getStatusCode() != null) update.set("status_code", enachDetail.getStatusCode());
        if (enachDetail.getStatusDesc() != null) update.set("status_desc", enachDetail.getStatusDesc());

        return mongoTemplate.updateFirst(query, update, EnachDetail.class ,ENACH_DETAILS.getName());
    }

    public boolean isRequestIdExists(String request_id){
        Query query = Query.query(Criteria.where("request_id").is(request_id));
        return mongoTemplate.exists(query,ENACH_DETAILS.getName());
    }

    public boolean isNPCIRequestIdExists(String request_id){
        Query query = Query.query(Criteria.where("npci_request_id").is(request_id));
        return mongoTemplate.exists(query,ENACH_DETAILS.getName());
    }

    public Map<String,Object> searchEnachDetailsByCriteria(EnachDtlSearchRqstBdy requestBdy)  {
        Map<String,Object> resp = new HashMap<String,Object>(){{
            put("data-lst", new Object());
            put("page-cnt", 0);
        }};

        final List<AggregationOperation> pipeline = new ArrayList<>();
        pipeline.add(Aggregation.sort(Sort.Direction.DESC,"created_at"));
        if(StringUtils.isNoneEmpty(requestBdy.getSearchBy()))
            pipeline.add(Aggregation.match(
                    Criteria.where("").orOperator(
                            Criteria.where("mandate_id").is(requestBdy.getSearchBy()),
                            Criteria.where("external_ref_num").is(requestBdy.getSearchBy()),
                            Criteria.where("request_id").is(requestBdy.getSearchBy())
                    )
            ));
        if(StringUtils.isNoneEmpty(requestBdy.getCompanyId()))
            pipeline.add(Aggregation.match(
                    Criteria.where("").and("company_id").is(Integer.valueOf(requestBdy.getCompanyId()))
            ));
        if((CollectionUtils.isNotEmpty(requestBdy.getStatus())))
            pipeline.add(Aggregation.match(
                    Criteria.where("").and("status").in(requestBdy.getStatus())
            ));
        if(StringUtils.isNoneEmpty(requestBdy.getFromDate()) && StringUtils.isNoneEmpty(requestBdy.getToDate())) {
            pipeline.add(Aggregation.match(
                    Criteria.where("").andOperator(
                            Criteria.where("created_at").gte(LocalDate.parse(requestBdy.getFromDate()).atStartOfDay()),
                            Criteria.where("created_at").lte(LocalDate.parse(requestBdy.getToDate()).atTime(23, 59, 59))
                    )
            ));
        }else if(StringUtils.isNoneEmpty(requestBdy.getFromDate()) && !StringUtils.isNoneEmpty(requestBdy.getToDate())) {
            pipeline.add(Aggregation.match(
                    Criteria.where("").and("created_at").gte(LocalDate.parse(requestBdy.getFromDate()).atStartOfDay())
            ));
        }else if(!StringUtils.isNoneEmpty(requestBdy.getFromDate()) && StringUtils.isNoneEmpty(requestBdy.getToDate())) {
            pipeline.add(Aggregation.match(
                    Criteria.where("").and("created_at").lte(LocalDate.parse(requestBdy.getToDate()).atTime(23, 59, 59))
            ));
        }
        Aggregation aggregation = Aggregation.newAggregation(pipeline);
        List<EnachDetail> results =  mongoTemplate.aggregate(aggregation, mongoTemplate.getCollectionName(EnachDetail.class), EnachDetail.class)
                .getMappedResults();

        var rcCnt = results.size();
        var offset = (requestBdy.getPage() - 1) * requestBdy.getLimit();
        var dataLst = results.stream().skip(offset).limit(requestBdy.getLimit()).collect(Collectors.toList());
        resp.put("data-lst", dataLst);
        resp.put("page-cnt", (rcCnt % requestBdy.getLimit() == 0) ? rcCnt / requestBdy.getLimit() : rcCnt / requestBdy.getLimit() + 1);
        resp.put("data-lst-size", dataLst.size());
        resp.put("total-records", rcCnt);
        return resp;
    }

    public Map<String,Object> searchEnachDetailsByCriteria1(EnachDtlSearchRqstBdy requestBdy)  {
        Map<String,Object> resp = new HashMap<String,Object>(){{
            put("data-lst", new Object());
            put("page-cnt", 0);
        }};
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC, "created_at"));
        if(StringUtils.isNoneEmpty(requestBdy.getSearchBy()))
            query.addCriteria(
                    Criteria.where("").orOperator(
                                    Criteria.where("mandate_id").is(requestBdy.getSearchBy()),
                                    Criteria.where("external_ref_num").is(requestBdy.getSearchBy()),
                                    Criteria.where("request_id").is(requestBdy.getSearchBy())
                            )
            );
        if(StringUtils.isNoneEmpty(requestBdy.getCompanyId()))
            query.addCriteria(
                    Criteria.where("").and("company_id").is(Integer.valueOf(requestBdy.getCompanyId()))
            );
        if((CollectionUtils.isNotEmpty(requestBdy.getStatus())))
            query.addCriteria(
                    Criteria.where("").and("status").in(requestBdy.getStatus())
            );
        if(StringUtils.isNoneEmpty(requestBdy.getFromDate()) && StringUtils.isNoneEmpty(requestBdy.getToDate())) {
                query.addCriteria(
                        Criteria.where("").andOperator(
                                Criteria.where("created_at").gte(LocalDate.parse(requestBdy.getFromDate()).atStartOfDay()),
                                Criteria.where("created_at").lte(LocalDate.parse(requestBdy.getToDate()).atTime(23, 59, 59))
                        )
                );
        }else if(StringUtils.isNoneEmpty(requestBdy.getFromDate()) && !StringUtils.isNoneEmpty(requestBdy.getToDate())) {
            query.addCriteria(
                    Criteria.where("").and("created_at").gte(LocalDate.parse(requestBdy.getFromDate()).atStartOfDay())
            );
        }else if(!StringUtils.isNoneEmpty(requestBdy.getFromDate()) && StringUtils.isNoneEmpty(requestBdy.getToDate())) {
            query.addCriteria(
                    Criteria.where("").and("created_at").lte(LocalDate.parse(requestBdy.getToDate()).atTime(23, 59, 59))
            );
        }

        var rcCnt= mongoTemplate.count(query , EnachDetail.class, ENACH_DETAILS.getName());
        var offset = (requestBdy.getPage() - 1) * requestBdy.getLimit();
        var dataLst = mongoTemplate.find(query , EnachDetail.class, ENACH_DETAILS.getName()).stream()
                .skip(offset).limit(requestBdy.getLimit()).collect(Collectors.toList());
        resp.put("data-lst", dataLst);
        resp.put("page-cnt", (rcCnt % requestBdy.getLimit() == 0) ? rcCnt / requestBdy.getLimit() : rcCnt / requestBdy.getLimit()+1);
        resp.put("data-lst-size",dataLst.size());
        resp.put("total-records", rcCnt);
        return  resp;

    }

    public Optional<EnachDetail> getEnachDetailsByRequestId(String request_id){
        Query query = Query.query(Criteria.where("request_id").is(request_id));
        return Optional.ofNullable(mongoTemplate.findOne(query,EnachDetail.class));
    }

    public long getEnachCountByExtRefNum(String extRefNum, List<String> str){
        Query query = Query.query(Criteria.where("external_ref_num")
                .is(extRefNum).and("status")
                .nin(str));
        return mongoTemplate.count(query, EnachDetail.class, ENACH_DETAILS.getName());
    }

    public UpdateResult patchEnachDetails(EnachMandateDetails enachDetail,String requestId) {
        Query query = Query.query(Criteria.where("request_id").is(requestId));
        Update update = new Update();
        update.set("authentication_mode",enachDetail.getAuthenticationMode())
                .set("bank", enachDetail.getBank())
                .set("updated_at", LocalDateTime.now());

        if(enachDetail.getAccountNo()!=null){
            update.set("account_no",enachDetail.getAccountNo());
        }
        return mongoTemplate.updateFirst(query, update, EnachMandateDetails.class ,ENACH_DETAILS.getName());
    }

    private static List<String> getMissingFields(EnachDetail enachDetail) {
        List<String> missingFields = new ArrayList<>();

        if (StringUtils.isBlank(enachDetail.getCustomerTitle())) missingFields.add("customer_title");
        if (StringUtils.isBlank(enachDetail.getCustomerMobileCode())) missingFields.add("customer_mobile_code");
        if (StringUtils.isBlank(enachDetail.getCustomerTelephoneCode())) missingFields.add("customer_telephone_code");
        if (enachDetail.getAccountType() == null) missingFields.add("account_type");
        if (enachDetail.getAmountType() == null) missingFields.add("amount_type");
        if (StringUtils.isBlank(enachDetail.getPurposeOfMandate())) missingFields.add("purpose_of_mandate");
        if (StringUtils.isBlank(enachDetail.getCorporateName())) missingFields.add("corporate_name");
        if (StringUtils.isBlank(enachDetail.getUtilityNumber())) missingFields.add("utility_number");
        if (StringUtils.isBlank(enachDetail.getExternalRefNum())) missingFields.add("external_ref_num");

        return missingFields;
    }
    public List<String> getMissingFieldsForToken(String request_id) {
        Query query = Query.query(Criteria.where("request_id").is(request_id));

        EnachDetail enachDetail = mongoTemplate.findOne(query, EnachDetail.class);

        if (enachDetail == null) {
            return Collections.emptyList();
        }

        return getMissingFields(enachDetail);
    }

    public int updateByQueryUpdatePairs(List<Pair<Query, Update>> queryUpdatePairList) {
        if (queryUpdatePairList == null || queryUpdatePairList.isEmpty()) {
            return 0;
        }
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, EnachDetail.class);
        return bulkOps.updateOne(queryUpdatePairList).execute().getModifiedCount();
    }

    public List<BasicDBObject> txnSearchByExternalRefNum(String externalRefNum) {
        List<AggregationOperation> pipeline = new ArrayList<>();
        pipeline.add(Aggregation.match(Criteria.where("external_ref_num").is(externalRefNum)));
        pipeline.add(Aggregation.project("request_id", "external_ref_num"));
        pipeline.add(Aggregation.lookup(NACH_TRANSACTIONS.getName(), "request_id", "request_id", "txn"));
        pipeline.add(Aggregation.unwind("txn"));
        pipeline.add(Aggregation.project("external_ref_num")
                .and("txn.amount").as("amount")
                .and("txn.mandate_id").as("mandate_id")
                .and("txn.scheduled_on").as("scheduled_on")
                .and("txn.presentment_txn_id").as("presentment_txn_id")
                .and("txn.request_id").as("request_id")
                .and("txn.company_id").as("company_id")
                .and("txn.txn_request_date").as("txn_request_date")
                .and("txn.txn_status").as("txn_status")
                .and("txn.txn_error_msg").as("txn_error_msg")
                .and("txn.txn_utr_number").as("txn_utr_number")
                .and("txn.txn_utr_datetime").as("txn_utr_datetime")
                .and("txn.payment_txn_id").as("payment_txn_id")
                .and("txn.payment_datetime").as("payment_datetime")
                .and("txn.remarks").as("remarks")
                .and("txn.retry").as("retry"));
        return mongoTemplate.aggregate(Aggregation.newAggregation(pipeline), ENACH_DETAILS.getName(), BasicDBObject.class)
                .getMappedResults();
    }

}
