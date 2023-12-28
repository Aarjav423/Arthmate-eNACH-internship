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
@XmlRootElement(name = "GrpHdr")
@XmlType(propOrder = {"msgId", "creDtTm", "reqInitPty"})
@ToString
public class GrpHdr {
    private String msgId;
    private String creDtTm;
    private ReqInitPty reqInitPty;

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
    public ReqInitPty getReqInitPty() {
        return reqInitPty;
    }

    public void setReqInitPty(ReqInitPty reqInitPty) {
        this.reqInitPty = reqInitPty;
    }

}
