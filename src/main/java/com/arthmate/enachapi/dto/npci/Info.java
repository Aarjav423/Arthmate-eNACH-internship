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
@XmlRootElement(name = "Info")
@XmlType(propOrder = {"id", "catCode", "utilCode", "catDesc", "name", "spnBnk"})
public class Info {
    private String id;
    private String catCode;
    private String utilCode;
    private String catDesc;
    private String name;

    private String spnBnk;

    @XmlElement(name =  "Id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlElement(name =  "CatCode")
    public String getCatCode() {
        return catCode;
    }

    public void setCatCode(String catCode) {
        this.catCode = catCode;
    }

    @XmlElement(name =  "UtilCode")
    public String getUtilCode() {
        return utilCode;
    }

    public void setUtilCode(String utilCode) {
        this.utilCode = utilCode;
    }

    @XmlElement(name =  "CatDesc")
    public String getCatDesc() {
        return catDesc;
    }

    public void setCatDesc(String catDesc) {
        this.catDesc = catDesc;
    }

    @XmlElement(name =  "Name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name =  "Spn_Bnk_Nm")
    public String getSpnBnk() { return spnBnk; }

    public void setSpnBnk(String spnBnk) { this.spnBnk = spnBnk; }
}
