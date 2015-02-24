package com.liato.bankdroid.banking.banks.avanza.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class AccountOverview implements Serializable {
    private static final long serialVersionUID = -5511775495529857976L;
    @JsonProperty("totalOwnCapital")
    private float mTotalOwnCapital;
    @JsonProperty("accounts")
    private List<Account> mAccounts = Collections.emptyList();


    @JsonProperty("totalOwnCapital")
    public float getTotalOwnCapital() {
        return mTotalOwnCapital;
    }

    @JsonProperty("accounts")
    public List<Account> getAccounts() {
        return mAccounts;
    }

}