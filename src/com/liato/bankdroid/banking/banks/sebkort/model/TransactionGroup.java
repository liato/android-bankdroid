package com.liato.bankdroid.banking.banks.sebkort.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TransactionGroup implements Serializable {
    private static final long serialVersionUID = 5011424262690542196L;
    private String mTotal;
    private String mType;
    private double mTotalNumber;
    private List<Transaction> mTransactions = new ArrayList<Transaction>();
    private String mTitle;

    @JsonProperty("total")
    public String getTotal() {
        return mTotal;
    }

    @JsonSetter("total")
    public void setTotal(String t) {
        mTotal = t;
    }

    @JsonProperty("type")
    public String getType() {
        return mType;
    }

    @JsonSetter("type")
    public void setType(String t) {
        mType = t;
    }

    @JsonProperty("totalNumber")
    public double getTotalNumber() {
        return mTotalNumber;
    }

    @JsonSetter("totalNumber")
    public void setTotalNumber(double t) {
        mTotalNumber = t;
    }

    @JsonProperty("transactions")
    public List<Transaction> getTransactions() {
        return mTransactions;
    }

    @JsonSetter("transactions")
    public void setTransactions(List<Transaction> t) {
        mTransactions = t;
    }

    @JsonProperty("title")
    public String getTitle() {
        return mTitle;
    }

    @JsonSetter("title")
    public void setTitle(String t) {
        mTitle = t;
    }

}