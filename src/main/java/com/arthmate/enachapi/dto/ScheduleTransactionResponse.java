package com.arthmate.enachapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class ScheduleTransactionResponse {
    private String merchantCode;
    private String merchantTransactionIdentifier;
    private String merchantTransactionRequestType;
    private String responseType;
    private String transactionState;
    private String merchantAdditionalDetails;
    private PaymentMethod paymentMethod;
    private Error error;

    // Getters and setters here


    @Data
    public static class PaymentTransaction {
        private String amount;
        private String balanceAmount;
        private String bankReferenceIdentifier;
        private String dateTime;
        private String errorMessage;
        private String identifier;
        private String refundIdentifier;
        private String statusCode;
        private String statusMessage;
        private String instruction;
        private String reference;
        private String accountNo;

        // Getters and setters here
    }

    @Data
    public static class Error {
        private String code;
        private String desc;

        // Getters and setters here
    }

    @Data
    public static class PaymentMethod {
        private String token;
        private String instrumentAliasName;
        private String instrumentToken;
        private String bankSelectionCode;
        @JsonProperty("aCS")
        private String aCS;
        @JsonProperty("oTP")
        private String oTP;
        private PaymentTransaction paymentTransaction;
        private String authentication;
        private Error error;

        // Getters and setters here
    }


}