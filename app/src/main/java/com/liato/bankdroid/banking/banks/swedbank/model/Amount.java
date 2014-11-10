package com.liato.bankdroid.banking.banks.swedbank.model;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class Amount {

    @JsonProperty
    private BigDecimal amount;

    @JsonProperty
    private String currencyCode;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
}
