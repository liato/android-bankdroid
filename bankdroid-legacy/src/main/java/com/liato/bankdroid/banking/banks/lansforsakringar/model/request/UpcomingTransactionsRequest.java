package com.liato.bankdroid.banking.banks.lansforsakringar.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class UpcomingTransactionsRequest {

    private String mAccountNumber;


    public UpcomingTransactionsRequest(String accountNumber) {
        mAccountNumber = accountNumber;
    }

    @JsonProperty("accountNumber")
    public String getAccountNumber() {
        return mAccountNumber;
    }

    @JsonSetter("accountNumber")
    public void setAccountNumber(String a) {
        mAccountNumber = a;
    }

}