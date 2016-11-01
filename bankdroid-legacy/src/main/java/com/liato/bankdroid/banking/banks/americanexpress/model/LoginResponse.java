package com.liato.bankdroid.banking.banks.americanexpress.model;


import android.support.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {

    private int status;

    @Nullable
    private LogonData logonData;

    public SummaryData getSummaryData() {
        return summaryData;
    }

    public void setSummaryData(SummaryData summaryData) {
        this.summaryData = summaryData;
    }

    private SummaryData summaryData;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public LogonData getLogonData() {
        return logonData;
    }

    public void setLogonData(LogonData logonData) {
        this.logonData = logonData;
    }

    public List<Card> getCards() {
        if (summaryData != null && summaryData.getCardList() != null) {
            return summaryData.getCardList();
        }
        return Collections.emptyList();
    }
}
