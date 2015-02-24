package com.liato.bankdroid.banking.banks.payson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionHistory {

    @JsonProperty("total")
    private int total;
    @JsonProperty("TotalUnFilteredCount")
    private int totalUnFilteredCount;
    @JsonProperty("rows")
    private List<Transaction> rows = new ArrayList<Transaction>();
    @JsonProperty("MaxItemAmount")
    private double maxItemAmount;

    @JsonProperty("total")
    public int getTotal() {
        return total;
    }

    @JsonProperty("total")
    public void setTotal(int total) {
        this.total = total;
    }

    @JsonProperty("TotalUnFilteredCount")
    public int getTotalUnFilteredCount() {
        return totalUnFilteredCount;
    }

    @JsonProperty("TotalUnFilteredCount")
    public void setTotalUnFilteredCount(int totalUnFilteredCount) {
        this.totalUnFilteredCount = totalUnFilteredCount;
    }

    @JsonProperty("rows")
    public List<Transaction> getRows() {
        return rows;
    }

    @JsonProperty("rows")
    public void setRows(List<Transaction> rows) {
        this.rows = rows;
    }

    @JsonProperty("MaxItemAmount")
    public double getMaxItemAmount() {
        return maxItemAmount;
    }

    @JsonProperty("MaxItemAmount")
    public void setMaxItemAmount(double maxItemAmount) {
        this.maxItemAmount = maxItemAmount;
    }

}