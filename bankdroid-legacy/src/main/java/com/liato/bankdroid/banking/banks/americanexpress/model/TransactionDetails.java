package com.liato.bankdroid.banking.banks.americanexpress.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionDetails {
    private long status;
    private String message;

    private List<AccountActivity> activityList;

    public long getStatus() {
        return status;
    }

    public void setStatus(long status) {
        this.status = status;
    }

    public List<AccountActivity> getActivityList() {
        return activityList;
    }

    public void setActivityList(List<AccountActivity> activityList) {
        this.activityList = activityList;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Transaction> getTransactions() {
        if (activityList != null) {
            List<Transaction> transactions = new ArrayList<>();
            for (AccountActivity activity : activityList) {
                transactions.addAll(activity.getTransactionList());
            }
            return transactions;
        }
        return Collections.emptyList();
    }
}
