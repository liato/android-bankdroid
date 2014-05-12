package com.liato.bankdroid.banking.banks.payson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    @JsonProperty("userName")
    private String userName;
    @JsonProperty("balance")
    private String balance;
    @JsonProperty("pending")
    private String pending;

    @JsonProperty("userName")
    public String getUserName() {
        return userName;
    }

    @JsonProperty("userName")
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @JsonProperty("balance")
    public String getBalance() {
        return balance;
    }

    @JsonProperty("balance")
    public void setBalance(String balance) {
        this.balance = balance;
    }

    @JsonProperty("pending")
    public String getPending() {
        return pending;
    }

    @JsonProperty("pending")
    public void setPending(String pending) {
        this.pending = pending;
    }

}