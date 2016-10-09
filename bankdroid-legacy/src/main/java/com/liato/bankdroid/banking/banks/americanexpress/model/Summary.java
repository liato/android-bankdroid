package com.liato.bankdroid.banking.banks.americanexpress.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Summary {
    private TotalBalance totalBalance;

    public TotalBalance getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(TotalBalance totalBalance) {
        this.totalBalance = totalBalance;
    }
}
