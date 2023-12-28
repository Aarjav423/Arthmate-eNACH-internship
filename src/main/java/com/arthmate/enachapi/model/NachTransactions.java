package com.arthmate.enachapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
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

import static com.arthmate.enachapi.model.enums.NachTransactionBatchStatus.NEW;

@Data
@Document(collection = "nach_transactions")
public class NachTransactions {

    @Field(name = "_id")
    private ObjectId id; //auto generated id

    @Field("amount")
    private BigDecimal amount;

    @Field("mandate_id")
    private String mandateId; //UMRN

    @Field("scheduled_on")
    private LocalDate scheduledOn; // date when the transaction need to be debited

    @Field("status")
    private String status = NEW.name(); // NEW/PROCESSED for batch

    @Field("presentment_txn_id")
    private String presentmentTxnId; // generated by us mandateId+currentTimeInMillis

    @Field("request_id")
    private String subscriptionId; // subscriptionId

    @Field("company_id")
    private Integer companyId;

    @Field("txn_request_date")
    private LocalDate txnRequestDate; //current date when txn is made

    @Field("txn_status")
    private String txnStatus; // Response: F/S/I

    @Field("txn_error_msg")
    private String txnErrorMsg; // error desc in case of failure

    @Field("txn_utr_number")
    private String txnUtrNumber;

    @Field("txn_utr_datetime")
    private String txnUtrDatetime;

    @Field("payment_txn_id")
    private String paymentTxnId; //Response txn id from ICICI

    @Field("payment_datetime")
    private String paymentDatetime; //Response datetime from ICICI

    @Field("remarks")
    private String remarks;

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

    @Field("is_txn_success_sms_sent")
    private boolean isTxnSuccessSmsSent = false;

    @Field("txn_success_sms_sent_at")
    private LocalDateTime txnSuccessSmsSentAt;

    @Field("txn_success_sms_txn_id")
    private String txnSuccessSmsTxnId;

    @Field("retry")
    private boolean retry;

    public void setAmount(BigDecimal amount){
        DecimalFormat format = new DecimalFormat();
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        this.amount = new BigDecimal(format.format(amount).replace(",",""));
    }

}