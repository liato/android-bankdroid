package com.liato.bankdroid.banking.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class VODB implements Serializable {

    private static final long serialVersionUID = 6084093222386932965L;

    @JsonProperty("USRINF01")
    private Object usrinf01;

    @JsonProperty("DBZV160")
    private List<Object> dbzv160 = new ArrayList<Object>();

    @JsonProperty("DEVID01")
    private DEVID devid01;

    @JsonProperty("HWINFO01")
    private HWINFO hWINFO01;

    @JsonProperty("CBEW501")
    private List<Object> cbew501 = new ArrayList<Object>();

    @JsonProperty("DBZV170")
    private List<Object> dbzv170 = new ArrayList<Object>();

    @JsonProperty("CBEW502")
    private List<Object> cbew502 = new ArrayList<Object>();

    @JsonProperty("USRINF01")
    public Object getUSRINF01() {
        return usrinf01;
    }

    @JsonProperty("USRINF01")
    public void setUSRINF01(Object uSRINF01) {
        this.usrinf01 = uSRINF01;
    }

    @JsonProperty("DBZV160")
    public List<Object> getDBZV160() {
        return dbzv160;
    }

    @JsonProperty("DBZV160")
    public void setDBZV160(List<Object> dBZV160) {
        this.dbzv160 = dBZV160;
    }

    @JsonProperty("DEVID01")
    public DEVID getDEVID01() {
        return devid01;
    }

    @JsonProperty("DEVID01")
    public void setDEVID01(DEVID dEVID01) {
        this.devid01 = dEVID01;
    }

    @JsonProperty("HWINFO01")
    public HWINFO getHWINFO01() {
        return hWINFO01;
    }

    @JsonProperty("HWINFO01")
    public void setHWINFO01(HWINFO hWINFO01) {
        this.hWINFO01 = hWINFO01;
    }

    @JsonProperty("CBEW501")
    public List<Object> getCBEW501() {
        return cbew501;
    }

    @JsonProperty("CBEW501")
    public void setCBEW501(List<Object> cBEW501) {
        this.cbew501 = cBEW501;
    }

    @JsonProperty("DBZV170")
    public List<Object> getDBZV170() {
        return dbzv170;
    }

    @JsonProperty("DBZV170")
    public void setDBZV170(List<Object> dBZV170) {
        this.dbzv170 = dBZV170;
    }

    @JsonProperty("CBEW502")
    public List<Object> getCBEW502() {
        return cbew502;
    }

    @JsonProperty("CBEW502")
    public void setCBEW502(List<Object> cBEW502) {
        this.cbew502 = cBEW502;
    }

}
