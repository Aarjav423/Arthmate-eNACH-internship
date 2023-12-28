package com.arthmate.enachapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnachTxnRespBdy {

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("mandate_id")
    private String mandateId; //UMRN

    @JsonProperty("scheduled_on")
    private String scheduledOn; // date when the transaction need to be debited

    @JsonProperty("status")
    private String status; // NEW/COMPLETE for batch

    @JsonProperty("presentment_txn_id")
    private String presentmentTxnId;

    @JsonProperty("request_id")
    private String subscriptionId; // subscriptionId

    @JsonProperty("company_id")
    private Integer companyId;

    @JsonProperty("txn_request_date")
    @Pattern(regexp = "yyyy-MM-dd")
    private String txnRequestDate; //current date when txn is made

    @JsonProperty("txn_status")
    private String txnStatus; // Response: F/S/I

    @JsonProperty("txn_error_msg")
    private String txnErrorMsg; // error desc in case of failure

    @JsonProperty("txn_utr_number")
    private String txnUtrNumber;

    @JsonProperty("txn_utr_datetime")
    @Pattern(regexp = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime txnUtrDatetime; //Response datetime

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("updated_by")
    private String updatedBy;

    @JsonProperty("payment_txn_id")
    private String paymentTxnId;

    @JsonProperty("payment_datetime")
    private String paymentDatetime;

    @JsonProperty("remarks")
    private String remarks;

    @JsonProperty("retry")
    private String retry;
}
