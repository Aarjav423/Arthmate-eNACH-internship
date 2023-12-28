package com.arthmate.enachapi.dto.npci.callback;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;

import javax.xml.bind.annotation.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name="Document")
@ToString
public class MndtAccptRespDc {

    private MndtAccptResp mndtAccptResp;

    @XmlElement(name =  "MndtAccptResp")
    public MndtAccptResp getMndtAccptResp() {return mndtAccptResp;}

    public void setMndtAccptResp(MndtAccptResp mndtAccptResp) { this.mndtAccptResp = mndtAccptResp; }

    public String getRespMsgId(){ return mndtAccptResp.getUndrlygAccptncDtls().getOrgnlMsgInf().getMndtReqId(); }
    public String getaccptncRslt(){ return mndtAccptResp.getUndrlygAccptncDtls().getAccptncRslt().getAccptd(); }
    public String getRjctRsnCd(){return mndtAccptResp.getUndrlygAccptncDtls().getAccptncRslt().getRjctRsn().getReasonCode(); }
    public String getRjctRsn(){return mndtAccptResp.getUndrlygAccptncDtls().getAccptncRslt().getRjctRsn().getReasonDesc(); }
    public String getRefrncID(){return mndtAccptResp.getUndrlygAccptncDtls().getOrgnlMsgInf().getRefMsgId(); }
    public String getMndtID(){return mndtAccptResp.getUndrlygAccptncDtls().getOrgnlMsgInf().getMndtId(); }
    public String getMsgID(){return mndtAccptResp.getGrpHdr().getMsgId(); }


    @NoArgsConstructor
    @AllArgsConstructor
    @XmlRootElement(name="MndtAccptResp")
    @XmlType(propOrder = {"grpHdr", "undrlygAccptncDtls"})
    @ToString
    private static class MndtAccptResp{

        private GrpHdr grpHdr;
        private UndrlygAccptncDtls undrlygAccptncDtls;

        @XmlElement(name =  "GrpHdr")
        public GrpHdr getGrpHdr() {
            return grpHdr;
        }

        public void setGrpHdr(GrpHdr grpHdr) {
            this.grpHdr = grpHdr;
        }

        @XmlElement(name =  "UndrlygAccptncDtls")
        public UndrlygAccptncDtls getUndrlygAccptncDtls() { return undrlygAccptncDtls; }

        public void setUndrlygAccptncDtls(UndrlygAccptncDtls undrlygAccptncDtls) { this.undrlygAccptncDtls = undrlygAccptncDtls; }


        @NoArgsConstructor
        @AllArgsConstructor
        @XmlRootElement(name = "GrpHdr")
        @XmlType(propOrder = {"msgId", "creDtTm", "reqInitPty"})
        @ToString
        private static class GrpHdr {
            private String msgId;
            private String creDtTm;
            private String reqInitPty;

            @XmlElement(name =  "MsgId")
            public String getMsgId() {
                return msgId;
            }

            public void setMsgId(String msgId) {
                this.msgId = msgId;
            }

            @XmlElement(name =  "CreDtTm")
            public String getCreDtTm() {
                return creDtTm;
            }

            public void setCreDtTm(String creDtTm) {
                this.creDtTm = creDtTm;
            }

            @XmlElement(name =  "ReqInitPty")
            public String getReqInitPty() {
                return reqInitPty;
            }

            public void setReqInitPty(String reqInitPty) {
                this.reqInitPty = reqInitPty;
            }

        }

        @NoArgsConstructor
        @AllArgsConstructor
        @XmlRootElement(name = "UndrlygAccptncDtls")
        @XmlType(propOrder = {"orgnlMsgInf", "accptncRslt" })
        @ToString
        private static class UndrlygAccptncDtls{

            private OrgnlMsgInf orgnlMsgInf;
            private AccptncRslt accptncRslt;

            @XmlElement(name =  "OrgnlMsgInf")
            public OrgnlMsgInf getOrgnlMsgInf() { return orgnlMsgInf;  }

            public void setOrgnlMsgInf(OrgnlMsgInf orgnlMsgInf) {   this.orgnlMsgInf = orgnlMsgInf;  }

            @XmlElement(name =  "AccptncRslt")
            public AccptncRslt getAccptncRslt() {   return accptncRslt;  }

            public void setAccptncRslt(AccptncRslt accptncRslt) { this.accptncRslt = accptncRslt; }

            @NoArgsConstructor
            @AllArgsConstructor
            @XmlRootElement(name = "OrgnlMsgInf")
            @XmlType(propOrder = { "mndtReqId", "refMsgId", "mndtId", "creDtTm" })
            @ToString
            private static class OrgnlMsgInf {
                private String mndtReqId ;
                private String refMsgId;
                private String mndtId;
                private String creDtTm;


                @XmlElement(name =  "MndtReqId")
                public String getMndtReqId() {return mndtReqId; }

                public void setMndtReqId(String mndtReqId) {this.mndtReqId = mndtReqId; }

                public String getRefMsgId() { return refMsgId;}
                @XmlElement(name =  "NPCI_RefMsgId")
                public void setRefMsgId(String refMsgId) {   this.refMsgId = refMsgId;}

                @XmlElement(name =  "MndtId")
                public String getMndtId() { return mndtId; }

                public void setMndtId(String mndtId) { this.mndtId = mndtId; }

                @XmlElement(name =  "CreDtTm")
                public String getCreDtTm() {
                    return creDtTm;
                }

                public void setCreDtTm(String creDtTm) {
                    this.creDtTm = creDtTm;
                }

            }

            @NoArgsConstructor
            @AllArgsConstructor
            @XmlRootElement(name = "AccptncRslt")
            @XmlType(propOrder = {"accptd", "accptRefNo", "rjctRsn", "dbtr" })
            @ToString
            private static class AccptncRslt {
                private String accptd;
                private String accptRefNo;
                private RjctRsn rjctRsn ;
                private Dbtr dbtr;

                @XmlElement(name =  "Accptd")
                public String getAccptd() {return accptd;  }

                public void setAccptd(String accptd) { this.accptd = accptd;}

                @XmlElement(name =  "AccptRefNo")
                public String getAccptRefNo() {  return accptRefNo;  }

                public void setAccptRefNo(String accptRefNo) {this.accptRefNo = accptRefNo; }

                @XmlElement(name =  "RjctRsn")
                public RjctRsn getRjctRsn() {return rjctRsn;}

                public void setRjctRsn(RjctRsn rjctRsn) {this.rjctRsn = rjctRsn; }

                @XmlElement(name =  "DBTR")
                public Dbtr getDbtr() { return dbtr;  }

                public void setDbtr(Dbtr dbtr) { this.dbtr = dbtr; }

                @NoArgsConstructor
                @AllArgsConstructor
                @XmlRootElement(name = "RjctRsn")
                @XmlType(propOrder = {"reasonCode", "reasonDesc", "rejectBy" })
                @ToString
                private static class RjctRsn {
                    private String reasonCode;
                    private String reasonDesc;
                    private String rejectBy;

                    @XmlElement(name =  "ReasonCode")
                    public String getReasonCode() { return reasonCode; }

                    public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode;  }

                    @XmlElement(name =  "ReasonDesc")
                    public String getReasonDesc() { return reasonDesc; }

                    public void setReasonDesc(String reasonDesc) {  this.reasonDesc = reasonDesc; }

                    @XmlElement(name =  "RejectBy")
                    public String getRejectBy() {  return rejectBy;  }

                    public void setRejectBy(String rejectBy) { this.rejectBy = rejectBy;  }
                }

                @NoArgsConstructor
                @AllArgsConstructor
                @XmlRootElement(name = "DBTR")
                @XmlType(propOrder = {"ifsc" })
                @ToString
                private static class Dbtr {
                    private String ifsc;

                    @XmlElement(name =  "IFSC")
                    public String getIfsc() {  return ifsc; }

                    public void setIfsc(String ifsc) {  this.ifsc = ifsc;   }
                }
            }
        }
    }
}
