package com.liato.bankdroid.banking.banks.avanza.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class Position implements Serializable {
    private static final long serialVersionUID = 4138023852221811457L;
    @JsonProperty("instrumentName")
    private String mInstrumentName;
    @JsonProperty("averageAcquiredPrice")
    private double mAverageAcquiredPrice;
    @JsonProperty("marketValue")
    private double mMarketValue;
    @JsonProperty("price")
    private double mPrice;
    @JsonProperty("profit")
    private double mProfit;
    @JsonProperty("modified")
    private long mModified;
    @JsonProperty("expiryDate")
    private long mExpiryDate;
    @JsonProperty("volume")
    private int mVolume;
    @JsonProperty("tradable")
    private boolean mTradable;
    @JsonProperty("orderbookId")
    private long mOrderbookId;
    @JsonProperty("profitPercent")
    private double mProfitPercent;
    @JsonProperty("type")
    private int mType;
    @JsonProperty("instrumentType")
    private String mInstrumentType;
    @JsonProperty("change")
    private double mChange;


    @JsonProperty("instrumentName")
    public String getInstrumentName() {
        return mInstrumentName;
    }

    @JsonProperty("averageAcquiredPrice")
    public double getAverageAcquiredPrice() {
        return mAverageAcquiredPrice;
    }

    @JsonProperty("marketValue")
    public double getMarketValue() {
        return mMarketValue;
    }

    @JsonProperty("price")
    public double getPrice() {
        return mPrice;
    }

    @JsonProperty("profit")
    public double getProfit() {
        return mProfit;
    }

    @JsonProperty("modified")
    public long getModified() {
        return mModified;
    }

    @JsonProperty("expiryDate")
    public long getExpiryDate() {
        return mExpiryDate;
    }

    @JsonProperty("volume")
    public int getVolume() {
        return mVolume;
    }

    @JsonProperty("tradable")
    public boolean getTradable() {
        return mTradable;
    }

    @JsonProperty("orderbookId")
    public long getOrderbookId() {
        return mOrderbookId;
    }

    @JsonProperty("profitPercent")
    public double getProfitPercent() {
        return mProfitPercent;
    }

    @JsonProperty("type")
    public int getType() {
        return mType;
    }

    @JsonProperty("instrumentType")
    public String getInstrumentType() {
        return mInstrumentType;
    }

    @JsonProperty("change")
    public double getChange() {
        return mChange;
    }

}