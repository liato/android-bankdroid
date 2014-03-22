package com.liato.bankdroid.banking.banks.ica.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Overview {

    @JsonProperty("YearlyTotalPurchased")
    private double yearlyTotalPurchased;
    @JsonProperty("AcquiredDiscount")
    private double acquiredDiscount;
    @JsonProperty("AmountSinceLastBonusCheck")
    private double amountSinceLastBonusCheck;
    @JsonProperty("AmountLeftUntilNextBonusCheck")
    private double amountLeftUntilNextBonusCheck;
    @JsonProperty("AcquiredBonus")
    private double acquiredBonus;
    @JsonProperty("IcaBankUrl")
    private String icaBankUrl;
    @JsonProperty("BonusToDate")
    private String bonusToDate;
    @JsonProperty("Accounts")
    private List<Account> accounts = new ArrayList<Account>();

    @JsonProperty("YearlyTotalPurchased")
    public double getYearlyTotalPurchased() {
        return yearlyTotalPurchased;
    }

    @JsonProperty("YearlyTotalPurchased")
    public void setYearlyTotalPurchased(double yearlyTotalPurchased) {
        this.yearlyTotalPurchased = yearlyTotalPurchased;
    }

    @JsonProperty("AcquiredDiscount")
    public double getAcquiredDiscount() {
        return acquiredDiscount;
    }

    @JsonProperty("AcquiredDiscount")
    public void setAcquiredDiscount(double acquiredDiscount) {
        this.acquiredDiscount = acquiredDiscount;
    }

    @JsonProperty("AmountSinceLastBonusCheck")
    public double getAmountSinceLastBonusCheck() {
        return amountSinceLastBonusCheck;
    }

    @JsonProperty("AmountSinceLastBonusCheck")
    public void setAmountSinceLastBonusCheck(double amountSinceLastBonusCheck) {
        this.amountSinceLastBonusCheck = amountSinceLastBonusCheck;
    }

    @JsonProperty("AmountLeftUntilNextBonusCheck")
    public double getAmountLeftUntilNextBonusCheck() {
        return amountLeftUntilNextBonusCheck;
    }

    @JsonProperty("AmountLeftUntilNextBonusCheck")
    public void setAmountLeftUntilNextBonusCheck(double amountLeftUntilNextBonusCheck) {
        this.amountLeftUntilNextBonusCheck = amountLeftUntilNextBonusCheck;
    }

    @JsonProperty("AcquiredBonus")
    public double getAcquiredBonus() {
        return acquiredBonus;
    }

    @JsonProperty("AcquiredBonus")
    public void setAcquiredBonus(double acquiredBonus) {
        this.acquiredBonus = acquiredBonus;
    }

    @JsonProperty("IcaBankUrl")
    public String getIcaBankUrl() {
        return icaBankUrl;
    }

    @JsonProperty("IcaBankUrl")
    public void setIcaBankUrl(String icaBankUrl) {
        this.icaBankUrl = icaBankUrl;
    }

    @JsonProperty("BonusToDate")
    public String getBonusToDate() {
        return bonusToDate;
    }

    @JsonProperty("BonusToDate")
    public void setBonusToDate(String bonusToDate) {
        this.bonusToDate = bonusToDate;
    }

    @JsonProperty("Accounts")
    public List<Account> getAccounts() {
        return accounts;
    }

    @JsonProperty("Accounts")
    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

}