package com.arthmate.enachapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ResPostedToMerchantReqBdy {

    @JsonProperty("getRespForNPCIRefID")
    private List<MandateReqIdNpci> getRespForNpciRefId;

    @Data
    @Builder
    public static class MandateReqIdNpci {

        @JsonProperty("MerchantID")
        private String merchantId;
        @JsonProperty("MndtReqId")
        private String mndtReqId;
        @JsonProperty("ReqInitDate")
        private String reqInitDate;
        @JsonProperty("NpciRefMsgID")
        private String npciRefMsgId;

    }

}
