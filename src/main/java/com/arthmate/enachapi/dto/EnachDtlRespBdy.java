package com.arthmate.enachapi.dto;

import com.arthmate.enachapi.model.enums.AccountType;
import com.arthmate.enachapi.model.enums.AmountType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.LastModifiedBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnachDtlRespBdy {

    @JsonProperty("_id")
    private ObjectId id;

    @JsonProperty("request_id")
    private String requestId;
    @JsonProperty("customer_title")
    private String customerTitle;
    @JsonProperty("customer_name")
    private String customerName;
    @JsonProperty("customer_email_id")
    private String customerEmailId;
    @JsonProperty("customer_mobile_code")
    private String customerMobileCode;
    @JsonProperty("customer_mobile_no")
    private String customerMobileNo;
    @JsonProperty("customer_telephone_code")
    private String customerTelephoneCode;
    @JsonProperty("customer_telephone_no")
    private String customerTelephoneNo;
    @JsonProperty("customer_pan")
    private String customerPAN;
    @JsonProperty("account_no")
    private String accountNo;
    @JsonProperty("account_type")
    private AccountType accountType;
    @JsonProperty("amount")
    private BigDecimal amount;
    @JsonProperty("amount_type")
    private AmountType AmountType;
    @JsonProperty("enach_reason")
    private String enachReason;
    @JsonProperty("start_date")
    private LocalDate startDate;
    @JsonProperty("end_date")
    private LocalDate endDate;
    @JsonProperty("emi_frequency")
    private String emiFrequency;
    @JsonProperty("purpose_of_mandate")
    private String purposeOfMandate;
    @JsonProperty("bank")
    private String bank;
    @JsonProperty("authentication_mode")
    private String authenticationMode;
    @JsonProperty("corporate_name")
    private String corporateName;
    @JsonProperty("utility_number")
    private String utilityNumber;
    @JsonProperty("reference_number")
    private String referenceNumber;
    @JsonProperty("npci_request_id")
    private String npciRequestId;
    @JsonProperty("status")
    private String status;
    @JsonProperty("msg_id")
    private String msgId;
    @JsonProperty("mandate_id")
    private String mndtId;
    @JsonProperty("company_id")
    private Integer companyId;
    @JsonProperty("is_sms_required")
    private boolean isSmsRequired = false;
    @JsonProperty("is_email_required")
    private boolean isEmailRequired = false;
    @JsonProperty("is_sms_sent")
    private boolean isSmsSent = false;
    @JsonProperty("is_email_sent")
    private boolean isEmailSent = false;
    @JsonProperty("reject_reason")
    private String rejectReason;
    @JsonProperty("external_ref_num")
    private String externalRefNum;
    @JsonProperty("created_by")
    private String createdBy;
    @JsonProperty("updated_by")
    private String updatedBy;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("updated_at")
    private LocalDateTime updatedDate;

}
