package com.arthmate.enachapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SmsRequestBody {

    @JsonProperty("recipient")
    private String recipient;
    @JsonProperty("message")
    private String message;
    @JsonProperty("from")
    private String from;

}
