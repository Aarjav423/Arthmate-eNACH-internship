package com.arthmate.enachapi.dto;

import com.arthmate.enachapi.utils.validator.BankDetails;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@BankDetails(globalBeanName = "bankDetails")
public class EnachMandateDetails {

    @JsonProperty("bank")
    @NotBlank(message = "bank is required.")
    private String bank;

    @JsonProperty("authentication_mode")
    @NotBlank(message = "authentication_mode is required.")
    private String authenticationMode;

    @JsonProperty("account_no")
    private Long accountNo;

}
