package com.liato.bankdroid.banking.banks.swedbank.model.engagement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.liato.bankdroid.banking.banks.swedbank.model.CardAccount;
import com.liato.bankdroid.banking.banks.swedbank.model.CardTransaction;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardAccountResponse {

    @JsonProperty
    private CardAccount cardAccount;

    @JsonProperty
    private List<CardTransaction> transactions;

    @JsonProperty
    private List<CardTransaction> reservedTransactions;

    public CardAccount getCardAccount() {
        return cardAccount;
    }

    public void setCardAccount(CardAccount cardAccount) {
        this.cardAccount = cardAccount;
    }

    public List<CardTransaction> getTransactions() {
        if (transactions == null) {
            transactions = new ArrayList<CardTransaction>();
        }
        return transactions;
    }

    public void setTransactions(List<CardTransaction> transactions) {
        this.transactions = transactions;
    }

    public List<CardTransaction> getReservedTransactions() {
        if (reservedTransactions == null) {
            reservedTransactions = new ArrayList<CardTransaction>();
        }
        return reservedTransactions;
    }

    public void setReservedTransactions(List<CardTransaction> reservedTransactions) {
        this.reservedTransactions = reservedTransactions;
    }
}
