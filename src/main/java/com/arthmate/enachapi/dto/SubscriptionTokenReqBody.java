package com.arthmate.enachapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class SubscriptionTokenReqBody {

    @JsonProperty("company_id")
    private String companyId;
    @NotNull(message = "user_id must be present")
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("source")
    private String source;
    @JsonProperty("scope")
    private List<String> scope;

}
