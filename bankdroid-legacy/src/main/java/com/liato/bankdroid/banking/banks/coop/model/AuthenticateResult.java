package com.liato.bankdroid.banking.banks.coop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthenticateResult {

    @JsonProperty("Token")
    private String token;
    @JsonProperty("UserID")
    private int userID;

    @JsonProperty("Token")
    public String getToken() {
        return token;
    }

    @JsonProperty("Token")
    public void setToken(String token) {
        this.token = token;
    }

    @JsonProperty("UserID")
    public int getUserID() {
        return userID;
    }

    @JsonProperty("UserID")
    public void setUserID(int userID) {
        this.userID = userID;
    }


}