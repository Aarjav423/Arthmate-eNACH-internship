package com.arthmate.enachapi.dto.npci;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "Mndt")
@XmlType(propOrder = {"mndtReqId", "mndtId", "reason", "mndtType", "ocrncs", "colltnAmt", "dbtr", "crAccDtl"})
public class Mandate {
    private String mndtReqId;
    private String mndtId;
    private String reason;
    private String mndtType;
    private Ocrncs ocrncs;
    private ColltnAmt colltnAmt;
    private Dbtr dbtr;
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

    @XmlElement(name =  "Mndt_Type")
    public String getMndtType() {
        return mndtType;
    }

    public void setMndtType(String mndtType) {
        this.mndtType = mndtType;
    }

    @XmlElement(name =  "Ocrncs")
    public Ocrncs getOcrncs() {
        return ocrncs;
    }

    public void setOcrncs(Ocrncs ocrncs) {
        this.ocrncs = ocrncs;
    }

    @XmlElement(name =  "ColltnAmt")
    public ColltnAmt getColltnAmt() {
        return colltnAmt;
    }

    public void setColltnAmt(ColltnAmt colltnAmt) {
        this.colltnAmt = colltnAmt;
    }

    @XmlElement(name =  "Dbtr")
    public Dbtr getDbtr() {
        return dbtr;
    }

    public void setDbtr(Dbtr dbtr) {
        this.dbtr = dbtr;
    }

    @XmlElement(name =  "CrAccDtl")
    public CrAccDtl getCrAccDtl() {
        return crAccDtl;
    }

    public void setCrAccDtl(CrAccDtl crAccDtl) {
        this.crAccDtl = crAccDtl;
    }
}
