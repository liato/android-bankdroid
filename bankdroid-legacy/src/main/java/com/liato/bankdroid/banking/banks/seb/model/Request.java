package com.liato.bankdroid.banking.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Request implements Serializable {

    @JsonProperty("ResultInfo")
    private Object resultInfo;
    @JsonProperty("VODB")
    private VODB vODB;
    @JsonProperty("ServiceInput")
    private List<ServiceInput> serviceInput = new ArrayList<ServiceInput>();
    @JsonProperty("UserCredentials")
    private UserCredentials userCredentials;
    @JsonProperty("ServiceInfo")
    private Object serviceInfo;

    @JsonProperty("ResultInfo")
    public Object getResultInfo() {
        return resultInfo;
    }

    @JsonProperty("ResultInfo")
    public void setResultInfo(Object resultInfo) {
        this.resultInfo = resultInfo;
    }

    @JsonProperty("VODB")
    public VODB getVODB() {
        return vODB;
    }

    @JsonProperty("VODB")
    public void setVODB(VODB vODB) {
        this.vODB = vODB;
    }

    @JsonProperty("ServiceInput")
    public List<ServiceInput> getServiceInput() {
        return serviceInput;
    }

    @JsonProperty("ServiceInput")
    public void setServiceInput(List<ServiceInput> serviceInput) {
        this.serviceInput = serviceInput;
    }

    @JsonProperty("UserCredentials")
    public UserCredentials getUserCredentials() {
        return userCredentials;
    }

    @JsonProperty("UserCredentials")
    public void setUserCredentials(UserCredentials userCredentials) {
        this.userCredentials = userCredentials;
    }

    @JsonProperty("ServiceInfo")
    public Object getServiceInfo() {
        return serviceInfo;
    }

    @JsonProperty("ServiceInfo")
    public void setServiceInfo(Object serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

}
