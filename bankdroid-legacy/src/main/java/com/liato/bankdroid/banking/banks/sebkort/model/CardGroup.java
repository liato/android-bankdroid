package com.liato.bankdroid.banking.banks.sebkort.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CardGroup implements Serializable {

    private static final long serialVersionUID = 4603347903479018508L;

    private List<TransactionGroup> mTransactionGroups = new ArrayList<TransactionGroup>();

    private double mTotalNumber;

    private String mTotal;

    private String mMaskedCardNumber;

    private String mNameOnCard;

    @JsonProperty("transactionGroups")
    public List<TransactionGroup> getTransactionGroups() {
        return mTransactionGroups;
    }

    @JsonSetter("transactionGroups")
    public void setTransactionGroups(List<TransactionGroup> t) {
        mTransactionGroups = t;
    }

    @JsonProperty("totalNumber")
    public double getTotalNumber() {
        return mTotalNumber;
    }

    @JsonSetter("totalNumber")
    public void setTotalNumber(double t) {
        mTotalNumber = t;
    }

    @JsonProperty("total")
    public String getTotal() {
        return mTotal;
    }

    @JsonSetter("total")
    public void setTotal(String t) {
        mTotal = t;
    }

    @JsonProperty("maskedCardNumber")
    public String getMaskedCardNumber() {
        return mMaskedCardNumber;
    }

    @JsonSetter("maskedCardNumber")
    public void setMaskedCardNumber(String m) {
        mMaskedCardNumber = m;
    }

    @JsonProperty("nameOnCard")
    public String getNameOnCard() {
        return mNameOnCard;
    }

    @JsonSetter("nameOnCard")
    public void setNameOnCard(String n) {
        mNameOnCard = n;
    }

}