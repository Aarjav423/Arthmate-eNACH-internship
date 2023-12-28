package com.arthmate.enachapi.model;

import com.arthmate.enachapi.model.enums.AccountType;
import com.arthmate.enachapi.model.enums.AmountType;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "enach_details_history")
public class EnachDetailHistory {


    @Field(name = "_id")
    private ObjectId id;

    @Field("request_id")
    private RequestId requestId = new RequestId();
    @Field("customer_title")
    private String customerTitle;
    @Field("customer_name")
    private String customerName;
    @Field("customer_email_id")
    private String customerEmailId;
    @Builder.Default
    @Field("customer_mobile_code")
    private String customerMobileCode = "+91";
    @Field("customer_mobile_no")
    private String customerMobileNo;
    @Builder.Default
    @Field("customer_telephone_code")
    private String customerTelephoneCode = "+91";
    @Builder.Default
    @Field("customer_telephone_no")
    private String customerTelephoneNo = "";
    @Field("customer_pan")
    private String customerPAN;
    @Field("account_no")
    private String accountNo;
    @Field("account_type")
    private AccountType accountType;
    @Field("amount")
    private BigDecimal amount;
    @Field("amount_type")
    private AmountType AmountType;
    @Field("enach_reason")
    private String enachReason;
    @Field("start_date")
    private LocalDate startDate;
    @Field("end_date")
    private LocalDate endDate;
    @Field("emi_frequency")
    private String emiFrequency;
    @Field("purpose_of_mandate")
    private String purposeOfMandate;
    @Field("bank")
    private String bank;
    @Field("authentication_mode")
    private String authenticationMode;
    @Field("corporate_name")
    private String corporateName;
    @Field("utility_number")
    private String utilityNumber;
    @Field("reference_number")
    private String referenceNumber;
    @Field("external_ref_num")
    private String externalRefNum;
    @Field("consent")
    private String consent;
    @Field("consent_timestamp")
    private LocalDateTime consentTimestamp;
    @Field("npci_request_id")
    private String npciRequestId;
    @Field("npci_ref_msg_id")
    private String npciRefMsgId;
    @Field("accptd")
    private boolean accptd;
    @Field("accpt_ref_no")
    private String accptRefNo;
    @Field("reason_code")
    private String reasonCode;
    @Field("reason_desc")
    private String reasonDesc;
    @Field("reject_by")
    private String rejectBy;
    @Field("error_code")
    private String errorCode;
    @Field("error_desc")
    private String errorDesc;
    @Field("status_req_res_s3_url")
    private String statusReqResS3Url;
    @Field("service_status_code")
    private String serviceStatusCode;
    @Field("service_status_desc")
    private String serviceStatusDesc;
    @Field("status")
    private String status;
    @Field("msg_id")
    private String MsgId;
    @Field("mandate_id")
    private String mndtId;
    @Field("reject_reason")
    private String rejectReason;

    @Field("created_at")
    @CreatedDate
    private LocalDateTime createdAt;
    @Field("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedDate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    private static class RequestId {
        @Field("req_id")
        public String reqId;

        @Field("timestamp")
        public LocalDateTime timeStamp = LocalDateTime.now();

    }

    public void setReqId(String reqId) {
        requestId.reqId = reqId;
    }
}
