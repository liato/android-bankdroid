package com.liato.bankdroid.banking.banks.swedbank.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardTransaction {

    @JsonProperty
    private String date;

    @JsonProperty
    private String description;

    @JsonProperty
    private Amount foreignAmount;

    @JsonProperty
    private Amount localAmount;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Amount getForeignAmount() {
        return foreignAmount;
    }

    public void setForeignAmount(Amount foreignAmount) {
        this.foreignAmount = foreignAmount;
    }

    public Amount getLocalAmount() {
        return localAmount;
    }

    public void setLocalAmount(Amount localAmount) {
        this.localAmount = localAmount;
    }
}
