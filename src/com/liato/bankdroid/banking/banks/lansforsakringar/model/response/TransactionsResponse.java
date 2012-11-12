package com.liato.bankdroid.banking.banks.lansforsakringar.model.response;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class TransactionsResponse {
	private boolean mHasMore;
	private int mNextSequenceNumber;
	private ArrayList<Transaction> mTransactions = new ArrayList<Transaction>();


	@JsonSetter("hasMore")
	public void setHasMore(boolean h) { mHasMore = h; }
	@JsonProperty("hasMore")
	public boolean getHasMore() { return mHasMore; }

	@JsonSetter("nextSequenceNumber")
	public void setNextSequenceNumber(int n) { mNextSequenceNumber = n; }
	@JsonProperty("nextSequenceNumber")
	public int getNextSequenceNumber() { return mNextSequenceNumber; }

	@JsonSetter("transactions")
	public void setTransactions(ArrayList<Transaction> t) { mTransactions = t; }
	@JsonProperty("transactions")
	public ArrayList<Transaction> getTransactions() { return mTransactions; }

}