package com.liato.bankdroid.banking.banks.ica.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction {

    @JsonProperty("TransactionDate")
    private String transactionDate;

    @JsonProperty("Amount")
    private double amount;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("TransactionDate")
    public String getTransactionDate() {
        return transactionDate;
    }

    @JsonProperty("TransactionDate")
    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    @JsonProperty("Amount")
    public double getAmount() {
        return amount;
    }

    @JsonProperty("Amount")
    public void setAmount(double amount) {
        this.amount = amount;
    }

    @JsonProperty("Description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("Description")
    public void setDescription(String description) {
        this.description = description;
    }

}