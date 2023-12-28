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
@XmlRootElement(name = "Ocrncs")
@XmlType(propOrder = {"seqTp", "frqcy", "frstColltnDt", "fnlColltnDt"})
public class Ocrncs {
    private String seqTp;
    private String frqcy;
    private String frstColltnDt;
    private String fnlColltnDt;

    @XmlElement(name =  "SeqTp")
    public String getSeqTp() {
        return seqTp;
    }

    public void setSeqTp(String seqTp) {
        this.seqTp = seqTp;
    }

    @XmlElement(name =  "Frqcy")
    public String getFrqcy() {
        return frqcy;
    }

    public void setFrqcy(String frqcy) {
        this.frqcy = frqcy;
    }

    @XmlElement(name =  "FrstColltnDt")
    public String getFrstColltnDt() {
        return frstColltnDt;
    }

    public void setFrstColltnDt(String frstColltnDt) {
        this.frstColltnDt = frstColltnDt;
    }

    @XmlElement(name =  "FnlColltnDt")
    public String getFnlColltnDt() {
        return fnlColltnDt;
    }

    public void setFnlColltnDt(String fnlColltnDt) {
        this.fnlColltnDt = fnlColltnDt;
    }
}
