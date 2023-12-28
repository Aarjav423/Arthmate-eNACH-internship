package com.arthmate.enachapi.model;

import com.arthmate.enachapi.model.enums.AccountType;
import com.arthmate.enachapi.model.enums.AmountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.arthmate.enachapi.model.enums.AccountType.SAVINGS;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "enach_details")
public class EnachDetail {

    @Field(name = "_id")
    private ObjectId id;

    @Field("request_id")
    private String requestId;
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
    @Builder.Default
    @Field("account_type")
    private AccountType accountType = SAVINGS;
    @Field("amount")
    private BigDecimal amount;
    @Field("amount_type")
    private AmountType amountType;
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
    private String consentTimestamp;
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
    @Field("company_id")
    private Integer companyId;
    @Field("reject_by")
    private String rejectBy;
    @Field("error_code")
    private String errorCode;
    @Field("error_desc")
    private String errorDesc;
    @Field("status_req_res_s3_url")
    private String statusReqResS3Url;
    @Field("npci_xml_resp_s3_url")
    private String npciXmlRespS3Url;
    @Field("status_code")
    private String statusCode;
    @Field("status_desc")
    private String statusDesc;
    @Field("status")
    private String status;
    @Field("status_notified")
    @Builder.Default
    private byte statusNotified = 0;
    @Field("msg_id")
    private String msgId;
    @Field("mandate_id")
    private String mndtId;
    @Field("reject_reason")
    private String rejectReason;
    @Field("is_sms_required")
    private boolean isSmsRequired;
    @Field("is_sms_sent")
    private boolean isSmsSent;
    @Field("sms_remarks")
    private String smsRemarks;
    @Field("sms_sent_count")
    private int smsSentCount;
    @Field("last_sms_sent_at")
    private LocalDateTime lastSmsSentAt;
    @Field("mandate_link_sms_transaction_id")
    private String smsTransactionId;
    @Field("is_email_required")
    private boolean isEmailRequired;
    @Field("is_email_sent")
    private boolean isEmailSent;
    @Field("email_remarks")
    private String emailRemarks;
    @Field("email_sent_count")
    private int emailSentCount;
    @Field("last_email_sent_at")
    private LocalDateTime lastEmailSentAt;
    @Field("mandate_link_notification_retries")
    private int mandateLinkNotificationRetries;
    @Field("is_mandate_initiated_sms_sent")
    private boolean isMandateInitiatedSmsSent;
    @Field("mandate_initiated_sms_sent_at")
    private LocalDateTime mandateInitiatedSmsSentAt;
    @Field("mandate_initiated_sms_transaction_id")
    private String mandateInitiatedSmsTransactionId;
    @Field("mandate_initiated_sms_remarks")
    private String mandateInitiatedSmsRemarks;

    @Field("created_at")
    @CreatedDate
    private LocalDateTime createdAt;
    @Field("created_by")
    private String createdBy;
    @Field("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedDate;
    @Field("updated_by")
    @LastModifiedBy
    private String updatedBy;


    public void setAmount(BigDecimal amount){
        DecimalFormat format = new DecimalFormat();
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        this.amount = new BigDecimal(format.format(amount).replace(",",""));
    }
}
