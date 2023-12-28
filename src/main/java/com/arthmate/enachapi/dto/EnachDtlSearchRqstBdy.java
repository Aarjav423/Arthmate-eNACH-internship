package com.arthmate.enachapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnachDtlSearchRqstBdy {

    @NotEmpty(groups = {DirectSearch.class},message = "search_by is required.")
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
    private List<String> status;

    @Builder.Default
    @JsonProperty("page")
    private short page = 1;

    @Builder.Default
    @JsonProperty("limit")
    private short limit = 10;
}

