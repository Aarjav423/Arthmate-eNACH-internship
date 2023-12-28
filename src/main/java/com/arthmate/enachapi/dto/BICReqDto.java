package com.arthmate.enachapi.dto;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class BICReqDto {

    @Field("request_id")
    private String requestId;

    @Field("status")
    private String status;

    @Field("_id")
    private ObjectId id;

    @Field("external_ref_num")
    private String externalRefNum;

    @Field("customer_name")
    private String customerName;

    @Field("start_date")
    private Date startDate;

    @Field("end_date")
    private Date endDate;

    @Field("status_desc")
    private String remarks;

    @Field("account_no")
    private String accountNo;

    @Field("mandate_id")
    private String mandateId;

    @Field("created_at")
    private Date createdAt;

    @Field("amount")
    private BigDecimal amount;

    @Field("corporate_name")
    private String corporateName;

    @Field("purpose_of_mandate")
    private String purposeOfMandate;

    @Field("customer_mobile_no")
    private String customerMobileNo;
}
