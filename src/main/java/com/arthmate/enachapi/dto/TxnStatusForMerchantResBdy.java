package com.arthmate.enachapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TxnStatusForMerchantResBdy {

    @JsonProperty("tranStatus")
    private List<TranStatus> tranStatus;

    @Data
    public static class TranStatus {

        @JsonProperty("MerchantID")
        private String merchantId;
        @JsonProperty("MndtReqId")
        private String mndtReqId;
        @JsonProperty("ReqInitDate")
        private String reqInitDate;
        @JsonProperty("npcirefmsgID")
        private String npciRefMsgId;
        @JsonProperty("MndtId")
        private String mndtId;
        @JsonProperty("Accptd")
        private String accptd;
        @JsonProperty("AccptRefNo")
        private String accptRefNo;
        @JsonProperty("ReasonCode")
        private String reasonCode;
        @JsonProperty("ReasonDesc")
        private String reasonDesc;
        @JsonProperty("RejectBy")
        private String rejectBy;
        @JsonProperty("ErrorCode")
        private String errorCode;
        @JsonProperty("ErrorDesc")
        private String errorDesc;

    }

}
