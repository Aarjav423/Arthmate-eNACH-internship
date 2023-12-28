package com.arthmate.enachapi.dto.npci;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement
public class ColltnAmt {
    private String ccy;
    private String value;

    @XmlAttribute(name = "Ccy")
    public String getCcy() {
        return ccy;
    }

    public void setCcy(String ccy) {
        this.ccy = ccy;
    }

    @XmlValue
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
