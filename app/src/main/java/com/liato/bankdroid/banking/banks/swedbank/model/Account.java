package com.liato.bankdroid.banking.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Account {

    @JsonProperty
    private boolean selectedForQuickBalance;

    @JsonProperty
    private String id;

    @JsonProperty
    private String name;

    @JsonProperty
    private BigDecimal balance;

    @JsonProperty
    private String currency;

    @JsonProperty
    private String fullyFormattedNumber;

    public boolean isSelectedForQuickBalance() {
        return selectedForQuickBalance;
    }

    public void setSelectedForQuickBalance(boolean selectedForQuickBalance) {
        this.selectedForQuickBalance = selectedForQuickBalance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getFullyFormattedNumber() {
        return fullyFormattedNumber;
    }

    public void setFullyFormattedNumber(String fullyFormattedNumber) {
        this.fullyFormattedNumber = fullyFormattedNumber;
    }
}
