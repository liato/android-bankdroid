package com.liato.bankdroid.banking.banks.coop.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthenticateRequest {

    @JsonProperty("password")
    private String password;
    @JsonProperty("username")
    private String username;
    @JsonProperty("applicationID")
    private String applicationID;

    public AuthenticateRequest(String username, String password, String applicationID) {
        this.username = username;
        this.password = password;
        this.applicationID = applicationID;
    }

    public AuthenticateRequest() {

    }

    @JsonProperty("password")
    public String getPassword() {
        return password;
    }

    @JsonProperty("password")
    public void setPassword(String password) {
        this.password = password;
    }

    @JsonProperty("username")
    public String getUsername() {
        return username;
    }

    @JsonProperty("username")
    public void setUsername(String username) {
        this.username = username;
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