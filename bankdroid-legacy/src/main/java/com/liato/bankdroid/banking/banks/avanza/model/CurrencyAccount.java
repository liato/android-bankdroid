package com.liato.bankdroid.banking.banks.avanza.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class CurrencyAccount implements Serializable {

    private static final long serialVersionUID = 6004713686055778196L;

    @JsonProperty("currency")
    private String mCurrency;

    @JsonProperty("balance")
    private double mBalance;

    @JsonProperty("accountId")
    private String mAccountId;


    @JsonProperty("currency")
    public String getCurrency() {
        return mCurrency;
    }

    @JsonProperty("balance")
    public double getBalance() {
        return mBalance;
    }

    @JsonProperty("accountId")
    public String getAccountId() {
        return mAccountId;
    }

}