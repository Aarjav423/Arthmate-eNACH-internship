package com.arthmate.enachapi.dto;

import com.arthmate.enachapi.model.enums.AccountType;
import com.arthmate.enachapi.model.enums.AmountType;
import com.arthmate.enachapi.model.enums.Consent;
import com.arthmate.enachapi.utils.validator.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedBy;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.arthmate.enachapi.utils.ApplicationConstants.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@BankDetails(globalBeanName = "bankDetails")
@EndDateNotBeforeStartDate
public class EnachDtlRqstBdy {

    @JsonProperty("customer_title")
    private String customerTitle;

    @NotBlank(message = "customer_name is required.")
    @Pattern(regexp = "^[A-Za-z0-9\\s\\_\\-]*$", message = "customer_name is not having valid characters")
    @JsonProperty("customer_name")
    private String customerName;

    @NotBlank(message = "customer_email_id is required.")
    @Email(regexp=".+@.+\\..+", message = "customer_email_id is not a valid email address")
    @JsonProperty("customer_email_id")
    private String customerEmailId;

    @JsonProperty("customer_mobile_code")
    private String customerMobileCode = "+91";

    @NotBlank(message = "customer_mobile_no is required.")
    @Pattern(regexp = "\\d{10}", message = "customer_mobile_no is not a valid mobile number")
    @JsonProperty("customer_mobile_no")
    private String customerMobileNo;

    @JsonProperty("customer_telephone_code")
    private String customerTelephoneCode = "+91";

    @JsonProperty("customer_telephone_no")
    private String customerTelephoneNo;

    @NotBlank(message = "customer_pan is required.")
    @Pattern(regexp = "[A-Z]{5}\\d{4}[A-Z]", message = "customer_pan is not a valid PAN number")
    @JsonProperty("customer_pan")
    private String customerPAN;

    @JsonProperty("account_no")
    private String accountNo;

    @EnumNamePattern(enumClass = AccountType.class, message = "account_type must be from {anyOf}")
    @JsonProperty("account_type")
    private String accountType;

    @NotNull(message = "amount is required.")
    @DecimalMin(value = "0.00", inclusive = false)
    @Digits(integer=11, fraction=2, message="amount is not correct")
    @JsonProperty("amount")
    private BigDecimal amount;

    @EnumNamePattern(enumClass = AmountType.class, message = "amount_type must be from {anyOf}")
    @JsonProperty("amount_type")
    private String amountType;

    @NotBlank(message = "enach_reason is required.")
    @JsonProperty("enach_reason")
    private String enachReason;

    @NotNull(message = "start_date is required.")
    @Pattern(regexp = "\\d{4}-\\d{1,2}-\\d{2}", message = "start_date is not a Date")
    @JsonProperty("start_date")
    private String startDate;

    @Pattern(regexp = "^$|\\d{4}-\\d{1,2}-\\d{2}", message = "end_date is not a Date")
    @JsonProperty("end_date")
    private String endDate;

    @NotBlank(message = "emi_frequency is required.")
    @MapNamePattern(globalBeanName = "enachFrequencyMap", fld = "emi_frequency")
    @JsonProperty("emi_frequency")
    private String emiFrequency;

    @MapNamePattern(globalBeanName = "enachMandatePurposeMap", fld = "purpose_of_mandate")
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

    @JsonProperty("status")
    private String status;

    @JsonProperty("msg_id")
    private String msgId;

    @JsonProperty("mandate_id")
    private String mndtId;

    @Digits(integer = Integer.MAX_VALUE, fraction = 0, message = "company_id must be a valid integer.")
    @JsonProperty("company_id")
    private String companyId;

    @Pattern(regexp = "true|false", flags = Pattern.Flag.CASE_INSENSITIVE, message = "is_sms_required must be true | false.")
    @JsonProperty("is_sms_required")
    private String isSmsRequired;

    @Pattern(regexp = "true|false", flags = Pattern.Flag.CASE_INSENSITIVE, message = "is_email_required must be true | false.")
    @JsonProperty("is_email_required")
    private String isEmailRequired;

    @Pattern(regexp = "true|false", flags = Pattern.Flag.CASE_INSENSITIVE, message = "is_sms_sent must be true | false.")
    @JsonProperty("is_sms_sent")
    private String isSmsSent;

    @Pattern(regexp = "true|false", flags = Pattern.Flag.CASE_INSENSITIVE, message = "is_email_sent must be true | false.")
    @JsonProperty("is_email_sent")
    private String isEmailSent;

    @JsonProperty("reject_reason")
    private String rejectReason;

    @ActionPermissionMatrixByExtRef(cnd = ACT_USEEXTREFNUM, act = ACT_USEEXTREFNUM_NM, val = ACT_DENIED ,message = "Registration with the External Reference Number external ref num already exists in the system")
    @JsonProperty("external_ref_num")
    private String externalRefNum;

    @NotNull(message = "consent is required.")
    @EnumNamePattern(enumClass = Consent.class, message = "consent must be from {anyOf}")
    @JsonProperty("consent")
    private String consent;

    //    @ConsentTimestamp()
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}[+-]\\d{2}:\\d{2}$", message = "consent_timestamp is in an invalid format")
    @JsonProperty("consent_timestamp")
    private String consentTimestamp;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("updated_by")
    @LastModifiedBy
    private String updatedBy;

    public LocalDate getStartDate(){
        return LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
    public LocalDate getEndDate() {
        if (endDate != null && !endDate.isBlank()) {
            return LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } else {
            return null;
        }
    }
}
