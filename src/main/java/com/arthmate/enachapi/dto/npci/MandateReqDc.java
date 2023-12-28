package com.arthmate.enachapi.dto.npci;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name="Document")
public class MandateReqDc {

    private String xmlns = "http://npci.org/onmags/schema";
    private MndtAuthReq mndtAuthReq;

    @XmlAttribute(name = "xmlns")
    public String getXmlns() {
        return xmlns;
    }

    public void setXmlns(String xmlns) {
        this.xmlns = xmlns;
    }
    @XmlElement(name =  "MndtAuthReq")
    public MndtAuthReq getMndtAuthReq() {
        return mndtAuthReq;
    }

    public void setMndtAuthReq(MndtAuthReq mndtAuthReq) {
        this.mndtAuthReq = mndtAuthReq;
    }

}
