package com.liato.bankdroid.banking.banks.americanexpress.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.liato.bankdroid.Helpers;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Card {
    private String cardProductName;
    private String cardKey;
    private String cardNumberDisplay;
    private int sortedIndex;

    private Summary summary;
    private Capabilities capabilities;

    public String getCardProductName() {
        return cardProductName;
    }

    public void setCardProductName(String cardProductName) {
        this.cardProductName = cardProductName;
    }

    public String getCardKey() {
        return cardKey;
    }

    public void setCardKey(String cardKey) {
        this.cardKey = cardKey;
    }

    public String getCardNumberDisplay() {
        return cardNumberDisplay;
    }

    public void setCardNumberDisplay(String cardNumberDisplay) {
        this.cardNumberDisplay = cardNumberDisplay;
    }

    public Summary getSummary() {
        return summary;
    }

    public void setSummary(Summary summary) {
        this.summary = summary;
    }

    public Capabilities getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Capabilities capabilities) {
        this.capabilities = capabilities;
    }

    public int getSortedIndex() {
        return sortedIndex;
    }

    public void setSortedIndex(int sortedIndex) {
        this.sortedIndex = sortedIndex;
    }

    public boolean isTransactionsEnabled() {
        return capabilities != null &&
                capabilities.getTransactions() != null &&
                capabilities.getTransactions().isEnabled();
    }

    public BigDecimal getBalance() {
        if (summary != null && summary.getTotalBalance() != null) {
            return Helpers.parseBalance(summary.getTotalBalance().getValue()).negate();
        }
        return BigDecimal.ZERO;
    }
}
