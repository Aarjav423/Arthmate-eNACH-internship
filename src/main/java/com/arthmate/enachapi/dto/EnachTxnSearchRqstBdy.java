package com.arthmate.enachapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnachTxnSearchRqstBdy {

    @JsonProperty("search_by")
    private String searchBy;

    @Digits(fraction = 0, integer = 10,message="company_id must be a number")
    @JsonProperty("company_id")
    private String companyId;

    @Pattern(regexp = "\\d{4}-\\d{1,2}-\\d{2}", message = "from_date is not a Date")
    @JsonProperty("from_date")
    private String fromDate;

    @Pattern(regexp = "\\d{4}-\\d{1,2}-\\d{2}", message = "to_date is not a Date")
    @JsonProperty("to_date")
    private String toDate;
    
    @JsonProperty("status")
    private String status;

    @Builder.Default
    @JsonProperty("page")
    private short page = 1;

    @Builder.Default
    @JsonProperty("limit")
    private short limit = 10;
}