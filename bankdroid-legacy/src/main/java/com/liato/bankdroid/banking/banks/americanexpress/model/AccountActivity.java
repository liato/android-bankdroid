package com.liato.bankdroid.banking.banks.americanexpress.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountActivity {
    List<Transaction> transactionList;

    public List<Transaction> getTransactionList() {
        return transactionList == null ?
                Collections.<Transaction>emptyList() : transactionList;
    }

    public void setTransactionList(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }
}
