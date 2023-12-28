package com.arthmate.enachapi.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SmsResponseBody {
    @JsonProperty("transactionId")
    @JsonAlias("transaction_id")
    private String transactionId;

    @JsonProperty("state")
    private String state;

    @JsonProperty("statusCode")
    @JsonAlias("status_code")
    private String statusCode;

    @JsonProperty("description")
    private String description;

    @JsonProperty("pdu")
    private Integer pdu;

}
