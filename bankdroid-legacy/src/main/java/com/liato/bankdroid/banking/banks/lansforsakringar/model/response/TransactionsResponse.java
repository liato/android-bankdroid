package com.liato.bankdroid.banking.banks.lansforsakringar.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.ArrayList;

public class TransactionsResponse {

    private boolean mHasMore;

    private int mNextSequenceNumber;

    private ArrayList<Transaction> mTransactions = new ArrayList<Transaction>();

    @JsonProperty("hasMore")
    public boolean getHasMore() {
        return mHasMore;
    }

    @JsonSetter("hasMore")
    public void setHasMore(boolean h) {
        mHasMore = h;
    }

    @JsonProperty("nextSequenceNumber")
    public int getNextSequenceNumber() {
        return mNextSequenceNumber;
    }

    @JsonSetter("nextSequenceNumber")
    public void setNextSequenceNumber(int n) {
        mNextSequenceNumber = n;
    }

    @JsonProperty("transactions")
    public ArrayList<Transaction> getTransactions() {
        return mTransactions;
    }

    @JsonSetter("transactions")
    public void setTransactions(ArrayList<Transaction> t) {
        mTransactions = t;
    }

}