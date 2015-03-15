package com.liato.bankdroid.banking.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserCredentials {

    @JsonProperty("ApplicationName")
    private String applicationName;

    @JsonProperty("WorkstationID")
    private String workstationID;

    @JsonProperty("LoggedOnUser")
    private String loggedOnUser;

    @JsonProperty("AuthMethod")
    private String authMethod;

    @JsonProperty("UserId")
    private String userId;

    @JsonProperty("Password")
    private String password;

    public UserCredentials() {

    }

    public UserCredentials(String userId, String password) {
        this.userId = userId;
        this.password = password;
        this.applicationName = "RFO";
        this.loggedOnUser = "Mobile";
        this.workstationID = "";
        this.authMethod = "0";
    }

    @JsonProperty("ApplicationName")
    public String getApplicationName() {
        return applicationName;
    }

    @JsonProperty("ApplicationName")
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    @JsonProperty("WorkstationID")
    public String getWorkstationID() {
        return workstationID;
    }

    @JsonProperty("WorkstationID")
    public void setWorkstationID(String workstationID) {
        this.workstationID = workstationID;
    }

    @JsonProperty("LoggedOnUser")
    public String getLoggedOnUser() {
        return loggedOnUser;
    }

    @JsonProperty("LoggedOnUser")
    public void setLoggedOnUser(String loggedOnUser) {
        this.loggedOnUser = loggedOnUser;
    }

    @JsonProperty("AuthMethod")
    public String getAuthMethod() {
        return authMethod;
    }

    @JsonProperty("AuthMethod")
    public void setAuthMethod(String authMethod) {
        this.authMethod = authMethod;
    }

    @JsonProperty("UserId")
    public String getUserId() {
        return userId;
    }

    @JsonProperty("UserId")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @JsonProperty("Password")
    public String getPassword() {
        return password;
    }

    @JsonProperty("Password")
    public void setPassword(String password) {
        this.password = password;
    }

}
