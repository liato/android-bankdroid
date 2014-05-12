package com.liato.bankdroid.banking.banks.coop.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BaseResponse {

    @JsonProperty("errorid")
    private String errorid;
    @JsonProperty("message")
    private String message;

    @JsonProperty("errorid")
    public String getErrorid() {
        return errorid;
    }

    @JsonProperty("errorid")
    public void setErrorid(String errorid) {
        this.errorid = errorid;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

}