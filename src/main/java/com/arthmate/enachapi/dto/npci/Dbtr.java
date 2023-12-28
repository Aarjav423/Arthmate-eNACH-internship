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
@XmlRootElement(name = "Dbtr")
@XmlType(propOrder = {"nm", "accNo", "acctType", "consRefNo","mobile", "email", "pan"})
public class Dbtr {
    private String nm;
    private String accNo;
    private String acctType;
    private String consRefNo;
    private String mobile;
    private String email;
    private String pan;

    @XmlElement(name =  "Nm")
    public String getNm() {
        return nm;
    }

    public void setNm(String nm) {
        this.nm = nm;
    }

    @XmlElement(name =  "AccNo")
    public String getAccNo() {
        return accNo;
    }

    public void setAccNo(String accNo) {
        this.accNo = accNo;
    }

    @XmlElement(name =  "Acct_Type")
    public String getAcctType() {
        return acctType;
    }

    public void setAcctType(String acctType) {
        this.acctType = acctType;
    }

    @XmlElement(name =  "Cons_Ref_No")
    public String getConsRefNo() {
        return consRefNo;
    }

    public void setConsRefNo(String consRefNo) {
        this.consRefNo = consRefNo;
    }

    @XmlElement(name =  "Mobile")
    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    @XmlElement(name =  "Email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @XmlElement(name =  "Pan")
    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }
}
