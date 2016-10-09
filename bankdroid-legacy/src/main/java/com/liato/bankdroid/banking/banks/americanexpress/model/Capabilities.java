package com.liato.bankdroid.banking.banks.americanexpress.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Capabilities {

    private TransactionCapabilities transactions;

    public TransactionCapabilities getTransactions() {
        return transactions;
    }

    public void setTransactions(TransactionCapabilities transactions) {
        this.transactions = transactions;
    }
}
