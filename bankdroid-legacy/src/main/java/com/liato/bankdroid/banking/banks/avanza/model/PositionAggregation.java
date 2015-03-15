package com.liato.bankdroid.banking.banks.avanza.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class PositionAggregation implements Serializable {

    private static final long serialVersionUID = 5531947007427482418L;

    @JsonProperty("totalChange")
    private double mTotalChange;

    @JsonProperty("positions")
    private List<Position> mPositions = Collections.emptyList();

    @JsonProperty("totalProfit")
    private double mTotalProfit;

    @JsonProperty("instrumentTypeName")
    private String mInstrumentTypeName;

    @JsonProperty("totalProfitPercent")
    private double mTotalProfitPercent;

    @JsonProperty("totalValue")
    private double mTotalValue;

    @JsonProperty("instrumentType")
    private int mInstrumentType;

    @JsonProperty("totalAverage")
    private double mTotalAverage;


    @JsonProperty("totalChange")
    public double getTotalChange() {
        return mTotalChange;
    }

    @JsonProperty("positions")
    public List<Position> getPositions() {
        return mPositions;
    }

    @JsonProperty("totalProfit")
    public double getTotalProfit() {
        return mTotalProfit;
    }

    @JsonProperty("instrumentTypeName")
    public String getInstrumentTypeName() {
        return mInstrumentTypeName;
    }

    @JsonProperty("totalProfitPercent")
    public double getTotalProfitPercent() {
        return mTotalProfitPercent;
    }

    @JsonProperty("totalValue")
    public double getTotalValue() {
        return mTotalValue;
    }

    @JsonProperty("instrumentType")
    public int getInstrumentType() {
        return mInstrumentType;
    }

    @JsonProperty("totalAverage")
    public double getTotalAverage() {
        return mTotalAverage;
    }

}