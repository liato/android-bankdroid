package com.liato.bankdroid.banking.banks.sebkort.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PendingTransactions implements Serializable {
    private static final long serialVersionUID = 8675073239578979403L;
    private double mTotalNumber;
    private boolean mMoreDataExists;
    private List<CardGroup> mCobrandCardGroups = new ArrayList<CardGroup>();
    private String mReservedAmount;
    private String mReservedAmountNumber;
    private List<CardGroup> mCardGroups = new ArrayList<CardGroup>();
    private List<TransactionGroup> mTransactionGroups = new ArrayList<TransactionGroup>();
    private String mTotal;

    @JsonProperty("totalNumber")
    public double getTotalNumber() {
        return mTotalNumber;
    }

    @JsonSetter("totalNumber")
    public void setTotalNumber(double t) {
        mTotalNumber = t;
    }

    @JsonProperty("moreDataExists")
    public boolean getMoreDataExists() {
        return mMoreDataExists;
    }

    @JsonSetter("moreDataExists")
    public void setMoreDataExists(boolean m) {
        mMoreDataExists = m;
    }

    @JsonProperty("cobrandCardGroups")
    public List<CardGroup> getCobrandCardGroups() {
        return mCobrandCardGroups;
    }

    @JsonSetter("cobrandCardGroups")
    public void setCobrandCardGroups(List<CardGroup> c) {
        mCobrandCardGroups = c;
    }

    @JsonProperty("reservedAmount")
    public String getReservedAmount() {
        return mReservedAmount;
    }

    @JsonSetter("reservedAmount")
    public void setReservedAmount(String r) {
        mReservedAmount = r;
    }

    @JsonProperty("reservedAmountNumber")
    public String getReservedAmountNumber() {
        return mReservedAmountNumber;
    }

    @JsonSetter("reservedAmountNumber")
    public void setReservedAmountNumber(String r) {
        mReservedAmountNumber = r;
    }

    @JsonProperty("cardGroups")
    public List<CardGroup> getCardGroups() {
        return mCardGroups;
    }

    @JsonSetter("cardGroups")
    public void setCardGroups(List<CardGroup> c) {
        mCardGroups = c;
    }

    @JsonProperty("transactionGroups")
    public List<TransactionGroup> getTransactionGroups() {
        return mTransactionGroups;
    }

    @JsonSetter("transactionGroups")
    public void setTransactionGroups(List<TransactionGroup> t) {
        mTransactionGroups = t;
    }

    @JsonProperty("total")
    public String getTotal() {
        return mTotal;
    }

    @JsonSetter("total")
    public void setTotal(String t) {
        mTotal = t;
    }

}