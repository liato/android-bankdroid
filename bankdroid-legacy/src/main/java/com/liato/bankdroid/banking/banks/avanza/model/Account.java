package com.liato.bankdroid.banking.banks.avanza.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class Account implements Serializable {

    private static final long serialVersionUID = -5718585872348469144L;

    @JsonProperty("balance")
    private double mBalance;

    @JsonProperty("totalProfit")
    private double mTotalProfit;

    @JsonProperty("accountName")
    private String mAccountName;

    @JsonProperty("totalAccruedInterest")
    private double mTotalAccruedInterest;

    @JsonProperty("adjustedForwardAmount")
    private double mAdjustedForwardAmount;

    @JsonProperty("unUsedCredit")
    private double mUnUsedCredit;

    @JsonProperty("superInterest")
    private double mSuperInterest;

    @JsonProperty("totalMarginRequirement")
    private double mTotalMarginRequirement;

    @JsonProperty("tradingPower")
    private double mTradingPower;

    @JsonProperty("resAmount")
    private double mResAmount;

    @JsonProperty("loanAmount")
    private double mLoanAmount;

    @JsonProperty("accountId")
    private String mAccountId;

    @JsonProperty("currencyAccounts")
    private List<CurrencyAccount> mCurrencyAccounts = Collections.emptyList();

    @JsonProperty("creditLimit")
    private double mCreditLimit;

    @JsonProperty("totalProfitPercent")
    private double mTotalProfitPercent;

    @JsonProperty("ownCapital")
    private double mOwnCapital;

    @JsonProperty("totalValue")
    private double mTotalValue;

    @JsonProperty("interestAmount")
    private double mInterestAmount;

    @JsonProperty("secAmount")
    private double mSecAmount;

    @JsonProperty("positionAggregations")
    private List<PositionAggregation> mPositionAggregations = Collections.emptyList();


    @JsonProperty("balance")
    public double getBalance() {
        return mBalance;
    }

    @JsonProperty("totalProfit")
    public double getTotalProfit() {
        return mTotalProfit;
    }

    @JsonProperty("accountName")
    public String getAccountName() {
        return mAccountName;
    }

    @JsonProperty("totalAccruedInterest")
    public double getTotalAccruedInterest() {
        return mTotalAccruedInterest;
    }

    @JsonProperty("adjustedForwardAmount")
    public double getAdjustedForwardAmount() {
        return mAdjustedForwardAmount;
    }

    @JsonProperty("unUsedCredit")
    public double getUnUsedCredit() {
        return mUnUsedCredit;
    }

    @JsonProperty("superInterest")
    public double getSuperInterest() {
        return mSuperInterest;
    }

    @JsonProperty("totalMarginRequirement")
    public double getTotalMarginRequirement() {
        return mTotalMarginRequirement;
    }

    @JsonProperty("tradingPower")
    public double getTradingPower() {
        return mTradingPower;
    }

    @JsonProperty("resAmount")
    public double getResAmount() {
        return mResAmount;
    }

    @JsonProperty("loanAmount")
    public double getLoanAmount() {
        return mLoanAmount;
    }

    @JsonProperty("accountId")
    public String getAccountId() {
        return mAccountId;
    }

    @JsonProperty("currencyAccounts")
    public List<CurrencyAccount> getCurrencyAccounts() {
        return mCurrencyAccounts;
    }

    @JsonProperty("creditLimit")
    public double getCreditLimit() {
        return mCreditLimit;
    }

    @JsonProperty("totalProfitPercent")
    public double getTotalProfitPercent() {
        return mTotalProfitPercent;
    }

    @JsonProperty("ownCapital")
    public double getOwnCapital() {
        return mOwnCapital;
    }

    @JsonProperty("totalValue")
    public double getTotalValue() {
        return mTotalValue;
    }

    @JsonProperty("interestAmount")
    public double getInterestAmount() {
        return mInterestAmount;
    }

    @JsonProperty("secAmount")
    public double getSecAmount() {
        return mSecAmount;
    }

    @JsonProperty("positionAggregations")
    public List<PositionAggregation> getPositionAggregations() {
        return mPositionAggregations;
    }

}
