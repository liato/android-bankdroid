package com.liato.bankdroid.banking.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditCard {

    @JsonProperty
    private String currency;

    @JsonProperty
    private String name;

    @JsonProperty
    private String balance;

    @JsonProperty
    private List<String> cardNumbers;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public List<String> getCardNumbers() {
        if(cardNumbers == null) {
            cardNumbers = new ArrayList<String>();
        }
        return cardNumbers;
    }

    public void setCardNumbers(List<String> cardNumbers) {
        this.cardNumbers = cardNumbers;
    }
}
