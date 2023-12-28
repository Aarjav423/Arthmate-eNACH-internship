package com.arthmate.enachapi.dto.npci.callback;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name="Document")
@ToString
public class MandateRejRespDc {

    private String xmlns;
    private MndtRejResp mndtRejResp;
    @XmlAttribute(name = "xmlns")
    public String getXmlns() {
        return xmlns;
    }

    public void setXmlns(String xmlns) {
        this.xmlns = xmlns;
    }

    public MndtRejResp getMndtRejResp() { return mndtRejResp;}

    @XmlElement(name =  "MndtRejResp")
    public void setMndtRejResp(MndtRejResp mndtRejResp) {this.mndtRejResp = mndtRejResp; }
    public String getRespMsgId(){ return mndtRejResp.origReqInfo.getMndtReqId(); }
    public String getRjctRsnCd(){ return mndtRejResp.getMndtErrorDtls().getErrorCode(); }
    public String getRjctRsn(){ return mndtRejResp.getMndtErrorDtls().getErrorDesc(); }
    public String getRefrncID(){return mndtRejResp.getOrigReqInfo().getRefMsgId();}
    public String getMsgID(){return mndtRejResp.getGrpHdr().getMsgId(); }


    @NoArgsConstructor
    @AllArgsConstructor
    @XmlRootElement(name="MndtRejResp")
    @XmlType(propOrder = {"grpHdr", "origReqInfo", "mndtErrorDtls"})
    @ToString
    private static class MndtRejResp{

        private GrpHdr grpHdr;
        private OrigReqInfo origReqInfo;
        private MndtErrorDtls mndtErrorDtls;


        public GrpHdr getGrpHdr() {
            return grpHdr;
        }

        @XmlElement(name =  "GrpHdr")
        public void setGrpHdr(GrpHdr grpHdr) {
            this.grpHdr = grpHdr;
        }

        public OrigReqInfo getOrigReqInfo() {return origReqInfo;}

        @XmlElement(name =  "OrigReqInfo")
        public void setOrigReqInfo(OrigReqInfo origReqInfo) { this.origReqInfo = origReqInfo; }

        public MndtErrorDtls getMndtErrorDtls() { return mndtErrorDtls; }

        @XmlElement(name =  "MndtErrorDtls")
        public void setMndtErrorDtls(MndtErrorDtls mndtErrorDtls) {  this.mndtErrorDtls = mndtErrorDtls;  }

        @NoArgsConstructor
        @AllArgsConstructor
        @XmlRootElement(name = "GrpHdr")
        @XmlType(propOrder = {"msgId", "creDtTm", "reqInitPty"})
        @ToString
        private static class GrpHdr {
            private String msgId;
            private String creDtTm;
            private String reqInitPty;


            public String getMsgId() {
                return msgId;
            }

            @XmlElement(name =  "MsgId")
            public void setMsgId(String msgId) {
                this.msgId = msgId;
            }

            public String getCreDtTm() {
                return creDtTm;
            }

            @XmlElement(name =  "CreDtTm")
            public void setCreDtTm(String creDtTm) {
                this.creDtTm = creDtTm;
            }

            public String getReqInitPty() {
                return reqInitPty;
            }

            @XmlElement(name =  "ReqInitPty")
            public void setReqInitPty(String reqInitPty) {
                this.reqInitPty = reqInitPty;
            }

        }

        @NoArgsConstructor
        @AllArgsConstructor
        @XmlRootElement(name = "OrigReqInfo")
        @XmlType(propOrder = {"refMsgId", "creDtTm", "mndtReqId" })
        @ToString
        private static class OrigReqInfo {
            private String refMsgId;
            private String creDtTm;
            private String mndtReqId ;

            public String getRefMsgId() {return refMsgId;}

            @XmlElement(name =  "NPCI_RefMsgId")
            public void setRefMsgId(String refMsgId) {this.refMsgId = refMsgId;}

            public String getMndtReqId() {return mndtReqId; }

            @XmlElement(name =  "MndtReqId")
            public void setMndtReqId(String mndtReqId) {this.mndtReqId = mndtReqId; }

            public String getCreDtTm() {
                return creDtTm;
            }

            @XmlElement(name =  "CreDtTm")
            public void setCreDtTm(String creDtTm) {
                this.creDtTm = creDtTm;
            }

        }

        @NoArgsConstructor
        @AllArgsConstructor
        @XmlRootElement(name = "MndtErrorDtls")
        @XmlType(propOrder = {"errorCode", "errorDesc", "rejectBy" })
        @ToString
        private static class MndtErrorDtls {
            private String errorCode;
            private String errorDesc;
            private String rejectBy ;


            public String getErrorCode() {return errorCode;}

            @XmlElement(name =  "ErrorCode")
            public void setErrorCode(String errorCode) {this.errorCode = errorCode;}

            public String getErrorDesc() {  return errorDesc; }

            @XmlElement(name =  "ErrorDesc")
            public void setErrorDesc(String errorDesc) {this.errorDesc = errorDesc;  }

            public String getRejectBy() { return rejectBy; }

            @XmlElement(name =  "RejectBy")
            public void setRejectBy(String rejectBy) {  this.rejectBy = rejectBy;  }

        }

    }


}
