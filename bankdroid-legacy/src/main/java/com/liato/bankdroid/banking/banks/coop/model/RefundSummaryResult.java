package com.liato.bankdroid.banking.banks.coop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RefundSummaryResult {

    @JsonProperty("AccountBalance")
    private double accountBalance;
    @JsonProperty("MonthName")
    private String monthName;
    @JsonProperty("PeriodRefund")
    private int periodRefund;
    @JsonProperty("ProfileNextRateDistance")
    private int profileNextRateDistance;
    @JsonProperty("ProfileRate")
    private double profileRate;
    @JsonProperty("TotalRefund")
    private int totalRefund;

    @JsonProperty("AccountBalance")
    public double getAccountBalance() {
        return accountBalance;
    }

    @JsonProperty("AccountBalance")
    public void setAccountBalance(double accountBalance) {
        this.accountBalance = accountBalance;
    }

    @JsonProperty("MonthName")
    public String getMonthName() {
        return monthName;
    }

    @JsonProperty("MonthName")
    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }

    @JsonProperty("PeriodRefund")
    public int getPeriodRefund() {
        return periodRefund;
    }

    @JsonProperty("PeriodRefund")
    public void setPeriodRefund(int periodRefund) {
        this.periodRefund = periodRefund;
    }

    @JsonProperty("ProfileNextRateDistance")
    public int getProfileNextRateDistance() {
        return profileNextRateDistance;
    }

    @JsonProperty("ProfileNextRateDistance")
    public void setProfileNextRateDistance(int profileNextRateDistance) {
        this.profileNextRateDistance = profileNextRateDistance;
    }

    @JsonProperty("ProfileRate")
    public double getProfileRate() {
        return profileRate;
    }

    @JsonProperty("ProfileRate")
    public void setProfileRate(double profileRate) {
        this.profileRate = profileRate;
    }

    @JsonProperty("TotalRefund")
    public int getTotalRefund() {
        return totalRefund;
    }

    @JsonProperty("TotalRefund")
    public void setTotalRefund(int totalRefund) {
        this.totalRefund = totalRefund;
    }

}