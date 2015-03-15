package com.liato.bankdroid.banking.banks.sebkort.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.io.Serializable;

public class Transaction implements Serializable {

    private static final long serialVersionUID = 2810644466680342679L;

    private String mCity;

    private String mRefTransactionId;

    private String mDescription;

    private String mExchangeRateDescription;

    private String mOriginalAmountDate;

    private String mOriginalAmountOrVat;

    private String mPostingDate;

    private long mPostingDateDate;

    private String mCurrency;

    private String mAmount;

    private double mAmountNumber;

    private long mTransactionId;

    private String mOriginalAmountOrVatNumber;

    private long mOriginalAmountDateDate;

    @JsonProperty("city")
    public String getCity() {
        return mCity;
    }

    @JsonSetter("city")
    public void setCity(String c) {
        mCity = c;
    }

    @JsonProperty("refTransactionId")
    public String getRefTransactionId() {
        return mRefTransactionId;
    }

    @JsonSetter("refTransactionId")
    public void setRefTransactionId(String r) {
        mRefTransactionId = r;
    }

    @JsonProperty("description")
    public String getDescription() {
        return mDescription;
    }

    @JsonSetter("description")
    public void setDescription(String d) {
        mDescription = d;
    }

    @JsonProperty("exchangeRateDescription")
    public String getExchangeRateDescription() {
        return mExchangeRateDescription;
    }

    @JsonSetter("exchangeRateDescription")
    public void setExchangeRateDescription(String e) {
        mExchangeRateDescription = e;
    }

    @JsonProperty("originalAmountDate")
    public String getOriginalAmountDate() {
        return mOriginalAmountDate;
    }

    @JsonSetter("originalAmountDate")
    public void setOriginalAmountDate(String o) {
        mOriginalAmountDate = o;
    }

    @JsonProperty("originalAmountOrVat")
    public String getOriginalAmountOrVat() {
        return mOriginalAmountOrVat;
    }

    @JsonSetter("originalAmountOrVat")
    public void setOriginalAmountOrVat(String o) {
        mOriginalAmountOrVat = o;
    }

    @JsonProperty("postingDate")
    public String getPostingDate() {
        return mPostingDate;
    }

    @JsonSetter("postingDate")
    public void setPostingDate(String p) {
        mPostingDate = p;
    }

    @JsonProperty("postingDateDate")
    public long getPostingDateDate() {
        return mPostingDateDate;
    }

    @JsonSetter("postingDateDate")
    public void setPostingDateDate(long p) {
        mPostingDateDate = p;
    }

    @JsonProperty("currency")
    public String getCurrency() {
        return mCurrency;
    }

    @JsonSetter("currency")
    public void setCurrency(String c) {
        mCurrency = c;
    }

    @JsonProperty("amount")
    public String getAmount() {
        return mAmount;
    }

    @JsonSetter("amount")
    public void setAmount(String a) {
        mAmount = a;
    }

    @JsonProperty("amountNumber")
    public double getAmountNumber() {
        return mAmountNumber;
    }

    @JsonSetter("amountNumber")
    public void setAmountNumber(double a) {
        mAmountNumber = a;
    }

    @JsonProperty("transactionId")
    public long getTransactionId() {
        return mTransactionId;
    }

    @JsonSetter("transactionId")
    public void setTransactionId(long t) {
        mTransactionId = t;
    }

    @JsonProperty("originalAmountOrVatNumber")
    public String getOriginalAmountOrVatNumber() {
        return mOriginalAmountOrVatNumber;
    }

    @JsonSetter("originalAmountOrVatNumber")
    public void setOriginalAmountOrVatNumber(String o) {
        mOriginalAmountOrVatNumber = o;
    }

    @JsonProperty("originalAmountDateDate")
    public long getOriginalAmountDateDate() {
        return mOriginalAmountDateDate;
    }

    @JsonSetter("originalAmountDateDate")
    public void setOriginalAmountDateDate(long o) {
        mOriginalAmountDateDate = o;
    }

}