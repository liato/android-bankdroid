package com.liato.bankdroid.banking.banks.lansforsakringar.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.ArrayList;

public class AccountsResponse {

    private ArrayList<Account> mAccounts = new ArrayList<Account>();

    @JsonProperty("accounts")
    public ArrayList<Account> getAccounts() {
        return mAccounts;
    }

    @JsonSetter("accounts")
    public void setAccounts(ArrayList<Account> a) {
        mAccounts = a;
    }

}
