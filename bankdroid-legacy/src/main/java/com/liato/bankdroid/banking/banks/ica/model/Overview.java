package com.liato.bankdroid.banking.banks.ica.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Overview {

    @JsonProperty("Saldo")
    private double saldo;

    @JsonProperty("YearlyTotalPurchased")
    private double yearlyTotalPurchased;

    @JsonProperty("PurchaseToDate")
    private String purchaseToDate;

    @JsonProperty("AcquiredDiscount")
    private double acquiredDiscount;

    @JsonProperty("AmountSinceLastBonusCheck")
    private double amountSinceLastBonusCheck;

    @JsonProperty("AmountLeftUntilNextBonusCheck")
    private double amountLeftUntilNextBonusCheck;

    @JsonProperty("NextBonusCheckValue")
    private double nextBonusCheckValue;

    @JsonProperty("AcquiredBonus")
    private double acquiredBonus;

    @JsonProperty("BonusToDate")
    private String bonusToDate;

    @JsonProperty("IcaBankUrl")
    private String icaBankUrl;

    @JsonProperty("AccountNumber")
    private String accountNumber;

    @JsonProperty("AccountName")
    private String accountName;

    @JsonProperty("AvailableAmount")
    private double availableAmount;

    @JsonProperty("CreditLimit")
    private double creditLimit;

    @JsonProperty("Accounts")
    private List<Account> accounts = new ArrayList<Account>();

    @JsonProperty("Transactions")
    private List<Transaction> transactions = new ArrayList<Transaction>();

    @JsonProperty("Saldo")
    public double getSaldo() {
        return saldo;
    }

    @JsonProperty("Saldo")
    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    @JsonProperty("YearlyTotalPurchased")
    public double getYearlyTotalPurchased() {
        return yearlyTotalPurchased;
    }

    @JsonProperty("YearlyTotalPurchased")
    public void setYearlyTotalPurchased(double yearlyTotalPurchased) {
        this.yearlyTotalPurchased = yearlyTotalPurchased;
    }

    @JsonProperty("PurchaseToDate")
    public String getPurchaseToDate() {
        return purchaseToDate;
    }

    @JsonProperty("PurchaseToDate")
    public void setPurchaseToDate(String purchaseToDate) {
        this.purchaseToDate = purchaseToDate;
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

    @JsonProperty("NextBonusCheckValue")
    public double getNextBonusCheckValue() {
        return nextBonusCheckValue;
    }

    @JsonProperty("NextBonusCheckValue")
    public void setNextBonusCheckValue(double nextBonusCheckValue) {
        this.nextBonusCheckValue = nextBonusCheckValue;
    }

    @JsonProperty("AcquiredBonus")
    public double getAcquiredBonus() {
        return acquiredBonus;
    }

    @JsonProperty("AcquiredBonus")
    public void setAcquiredBonus(double acquiredBonus) {
        this.acquiredBonus = acquiredBonus;
    }

    @JsonProperty("BonusToDate")
    public String getBonusToDate() {
        return bonusToDate;
    }

    @JsonProperty("BonusToDate")
    public void setBonusToDate(String bonusToDate) {
        this.bonusToDate = bonusToDate;
    }

    @JsonProperty("IcaBankUrl")
    public String getIcaBankUrl() {
        return icaBankUrl;
    }

    @JsonProperty("IcaBankUrl")
    public void setIcaBankUrl(String icaBankUrl) {
        this.icaBankUrl = icaBankUrl;
    }

    @JsonProperty("AccountNumber")
    public String getAccountNumber() {
        return accountNumber;
    }

    @JsonProperty("AccountNumber")
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    @JsonProperty("AccountName")
    public String getAccountName() {
        return accountName;
    }

    @JsonProperty("AccountName")
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    @JsonProperty("AvailableAmount")
    public double getAvailableAmount() {
        return availableAmount;
    }

    @JsonProperty("AvailableAmount")
    public void setAvailableAmount(double availableAmount) {
        this.availableAmount = availableAmount;
    }

    @JsonProperty("CreditLimit")
    public double getCreditLimit() {
        return creditLimit;
    }

    @JsonProperty("CreditLimit")
    public void setCreditLimit(double creditLimit) {
        this.creditLimit = creditLimit;
    }

    @JsonProperty("Accounts")
    public List<Account> getAccounts() {
        return accounts;
    }

    @JsonProperty("Accounts")
    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
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