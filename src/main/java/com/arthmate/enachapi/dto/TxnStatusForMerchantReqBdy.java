package com.arthmate.enachapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TxnStatusForMerchantReqBdy {

    @JsonProperty("mandateReqIDList")
    private List<MandateReqId> mandateReqIdList;

    @Data
    @Builder
    public static class MandateReqId {

        @JsonProperty("MerchantID")
        private String merchantId;
        @JsonProperty("MndtReqId")
        private String mndtReqId;
        @JsonProperty("ReqInitDate")
        private String reqInitDate;

    }

}
