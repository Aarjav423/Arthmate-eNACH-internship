package com.arthmate.enachapi.dto;

import com.arthmate.enachapi.utils.validator.ValidateRetry;
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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnachTxnRqstBdy {

    @NotNull(message = "amount is required.")
    @DecimalMin(value = "0.00", inclusive = false)
    @Digits(integer=11, fraction=2, message="amount is not correct")
    @JsonProperty("amount")
    private BigDecimal amount;

    @NotBlank(message = "mandate_id is required.")
    @JsonProperty("mandate_id")
    private String mandateId; //UMRN

    @NotNull(message = "scheduled_on is required.")
    @Pattern(regexp = "\\d{4}-\\d{1,2}-\\d{2}", message = "scheduled_on is not a Date")
    @JsonProperty("scheduled_on")
    private String scheduledOn; // date when the transaction need to be debited

    @JsonProperty("status")
    private String status; // NEW/COMPLETE for batch

    @NotBlank(message = "request_id is required.")
    @JsonProperty("request_id")
    private String subscriptionId; // subscriptionId

    @Digits(integer = Integer.MAX_VALUE, fraction = 0, message = "company_id must be a valid integer.")
    @JsonProperty("company_id")
    private String companyId;

    @Pattern(regexp = "\\d{4}-\\d{1,2}-\\d{2}", message = "txn_request_date is not a Date")
    @JsonProperty("txn_request_date")
    private String txnRequestDate; //current date when txn is made

    @JsonProperty("txn_status")
    private String txnStatus; // Response: F/S/I

    @JsonProperty("txn_error_msg")
    private String txnErrorMsg; // error desc in case of failure

    @JsonProperty("txn_utr_number")
    private String txnUtrNumber;

    @JsonProperty("txn_utr_datetime")
    private String txnUtrDatetime; //Response datetime

    @ValidateRetry(message ="old_presentment_txn_id already retried.")
    @JsonProperty("old_presentment_txn_id")
    private String oldPresentmentTxnId;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("updated_by")
    @LastModifiedBy
    private String updatedBy;

    @JsonProperty("sequence_no")
    private String sequenceNumber;

    @JsonProperty("remarks")
    private String remarks;

    public LocalDate getScheduledOn(){
        return LocalDate.parse(scheduledOn, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
