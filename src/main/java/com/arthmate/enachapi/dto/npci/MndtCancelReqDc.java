package com.arthmate.enachapi.dto.npci;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name="Document")
@ToString
public class MndtCancelReqDc {

    private MndtAuthReq mndtAuthReq;

    @XmlElement(name = "MndtAuthReq")
    public MndtAuthReq getMndtAuthReq() {
        return mndtAuthReq;
    }

    public void setMndtAuthReq(MndtAuthReq mndtAuthReq) {
        this.mndtAuthReq = mndtAuthReq;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @XmlRootElement(name = "MndtAuthReq")
    @XmlType(propOrder = {"grpHdr", "mndt"})
    @ToString
    private static class MndtAuthReq {

        private GrpHdr grpHdr;
        private Mandate mndt;

        @XmlElement(name = "GrpHdr")
        public GrpHdr getGrpHdr() {
            return grpHdr;
        }

        public void setGrpHdr(GrpHdr grpHdr) {
            this.grpHdr = grpHdr;
        }

        @XmlElement(name = "Mndt")
        public Mandate getMndt() {
            return mndt;
        }

        public void setMndt(Mandate mndt) {
            this.mndt = mndt;
        }

        @NoArgsConstructor
        @AllArgsConstructor
        @XmlRootElement(name = "GrpHdr")
        @XmlType(propOrder = {"msgId", "creDtTm", "reqInitPty"})
        @ToString
        private static class GrpHdr {
            private String msgId;
            private String creDtTm;
            private ReqInitPty reqInitPty;

            @XmlElement(name = "MsgId")
            public String getMsgId() {
                return msgId;
            }

            public void setMsgId(String msgId) {
                this.msgId = msgId;
            }

            @XmlElement(name = "CreDtTm")
            public String getCreDtTm() {
                return creDtTm;
            }

            public void setCreDtTm(String creDtTm) {
                this.creDtTm = creDtTm;
            }

            @XmlElement(name = "ReqInitPty")
            public ReqInitPty getReqInitPty() {
                return reqInitPty;
            }

            public void setReqInitPty(ReqInitPty reqInitPty) {
                this.reqInitPty = reqInitPty;
            }

            @NoArgsConstructor
            @AllArgsConstructor
            @XmlRootElement(name = "ReqInitPty")
            @XmlType(propOrder = {"info"})
            @ToString
            private static class ReqInitPty {
                private Info info;

                @XmlElement(name = "Info")
                public Info getInfo() {
                    return info;
                }

                public void setInfo(Info info) {
                    this.info = info;
                }

                @Builder
                @NoArgsConstructor
                @AllArgsConstructor
                @XmlRootElement(name = "Info")
                @XmlType(propOrder = {"id", "utilCode", "name", "spnBnk"})
                private static class Info {
                    private String id;
                    private String utilCode;
                    private String name;
                    private String spnBnk;

                    @XmlElement(name = "Id")
                    public String getId() {
                        return id;
                    }

                    public void setId(String id) {
                        this.id = id;
                    }

                    @XmlElement(name = "UtilCode")
                    public String getUtilCode() {
                        return utilCode;
                    }

                    public void setUtilCode(String utilCode) {
                        this.utilCode = utilCode;
                    }

                    @XmlElement(name = "Name")
                    public String getName() {
                        return name;
                    }

                    public void setName(String name) {
                        this.name = name;
                    }

                    @XmlElement(name = "Spn_Bnk_Nm")
                    public String getSpnBnk() {
                        return spnBnk;
                    }

                    public void setSpnBnk(String spnBnk) {
                        this.spnBnk = spnBnk;
                    }
                }
            }
        }

        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @XmlRootElement(name = "Mndt")
        @XmlType(propOrder = {"mndtReqId", "mndtId", "reason", "crAccDtl"})
        @ToString
        private static class Mandate {
            private String mndtReqId;
            private String mndtId;
            private String reason;
            private CrAccDtl crAccDtl;

            @XmlElement(name =  "MndtReqId")
            public String getMndtReqId() {
                return mndtReqId;
            }

            public void setMndtReqId(String mndtReqId) {
                this.mndtReqId = mndtReqId;
            }

            @XmlElement(name =  "MndtId")
            public String getMndtId() {  return mndtId;}
            public void setMndtId(String mndtId) { this.mndtId = mndtId; }

            @XmlElement(name =  "Reason")
            public String getReason() {  return reason;  }
            public void setReason(String reason) { this.reason = reason; }

            @XmlElement(name =  "CrAccDtl")
            public CrAccDtl getCrAccDtl() {
                return crAccDtl;
            }

            public void setCrAccDtl(CrAccDtl crAccDtl) {
                this.crAccDtl = crAccDtl;
            }

            @Builder
            @NoArgsConstructor
            @AllArgsConstructor
            @XmlRootElement(name = "CrAccDtl")
            @XmlType(propOrder = {"nm", "accNo", "mmbId"})
            @ToString
            private static class CrAccDtl {
                private String nm;
                private String mmbId;

                @XmlElement(name =  "Nm")
                public String getNm() {
                    return nm;
                }

                public void setNm(String nm) {
                    this.nm = nm;
                }

                @XmlElement(name =  "MmbId")
                public String getMmbId() {
                    return mmbId;
                }

                public void setMmbId(String mmbId) {
                    this.mmbId = mmbId;
                }
            }
        }

    }
}