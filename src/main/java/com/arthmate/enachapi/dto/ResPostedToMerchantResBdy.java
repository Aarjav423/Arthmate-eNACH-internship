package com.arthmate.enachapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ResPostedToMerchantResBdy {

    @JsonProperty("responseDtl")
    private List<ResponseDtl> responseDtl;

    @Data
    public static class ResponseDtl {

        @JsonProperty("MerchantID")
        private String merchantId;
        @JsonProperty("MndtReqId")
        private String mndtReqId;
        @JsonProperty("ReqInitDate")
        private String reqInitDate;
        @JsonProperty("NpciRefMsgID")
        private String npciRefMsgId;
        @JsonProperty("MndtId")
        private String mndtId;
        @JsonProperty("MandateRespDoc")
        private String mandateRespDoc;
        @JsonProperty("CheckSumVal")
        private String checkSumVal;
        @JsonProperty("RespType")
        private String respType;
        @JsonProperty("ErrorCode")
        private String errorCode;
        @JsonProperty("ErrorDesc")
        private String errorDesc;

    }

}
