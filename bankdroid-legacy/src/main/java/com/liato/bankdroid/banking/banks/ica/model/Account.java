package com.liato.bankdroid.banking.banks.ica.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Account {

    @JsonProperty("Name")
    private String name;

    @JsonProperty("AccountNumber")
    private String accountNumber;

    @JsonProperty("AvailableAmount")
    private double availableAmount;

    @JsonProperty("ReservedAmount")
    private double reservedAmount;

    @JsonProperty("Saldo")
    private double saldo;

    @JsonProperty("CreditLimit")
    private double creditLimit;

    @JsonProperty("Transactions")
    private List<Transaction> transactions = new ArrayList<Transaction>();

    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("Name")
    public String getName() {
        return name;
    }

    @JsonProperty("Name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("AccountNumber")
    public String getAccountNumber() {
        return accountNumber;
    }

    @JsonProperty("AccountNumber")
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    @JsonProperty("AvailableAmount")
    public double getAvailableAmount() {
        return availableAmount;
    }

    @JsonProperty("AvailableAmount")
    public void setAvailableAmount(double availableAmount) {
        this.availableAmount = availableAmount;
    }

    @JsonProperty("ReservedAmount")
    public double getReservedAmount() {
        return reservedAmount;
    }

    @JsonProperty("ReservedAmount")
    public void setReservedAmount(double reservedAmount) {
        this.reservedAmount = reservedAmount;
    }

    @JsonProperty("Saldo")
    public double getSaldo() {
        return saldo;
    }

    @JsonProperty("Saldo")
    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    @JsonProperty("CreditLimit")
    public double getCreditLimit() {
        return creditLimit;
    }

    @JsonProperty("CreditLimit")
    public void setCreditLimit(double creditLimit) {
        this.creditLimit = creditLimit;
    }

    @JsonProperty("Transactions")
    public List<Transaction> getTransactions() {
        return transactions;
    }

    @JsonProperty("Transactions")
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

}
