package com.liato.bankdroid.banking.banks.coop.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RefundSummaryRequest {

    @JsonProperty("token")
    private String token;
    @JsonProperty("userID")
    private String userID;
    @JsonProperty("applicationID")
    private String applicationID;

    public RefundSummaryRequest(String userID, String token, String applicationID) {
        this.userID = userID;
        this.token = token;
        this.applicationID = applicationID;
    }

    public RefundSummaryRequest() {

    }

    @JsonProperty("token")
    public String getToken() {
        return token;
    }

    @JsonProperty("token")
    public void setToken(String token) {
        this.token = token;
    }

    @JsonProperty("userID")
    public String getUserID() {
        return userID;
    }

    @JsonProperty("userID")
    public void setUserID(String userID) {
        this.userID = userID;
    }

    @JsonProperty("applicationID")
    public String getApplicationID() {
        return applicationID;
    }

    @JsonProperty("applicationID")
    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }

}