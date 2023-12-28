package com.arthmate.enachapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BICResponseDto {

    @JsonProperty("status")
    private String status;
    @JsonProperty("message")
    private String message;

}
