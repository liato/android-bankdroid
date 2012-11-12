package com.liato.bankdroid.banking.banks.lansforsakringar.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class Transaction {
	private String mText;
	private long mTransactiondate;
	private float mAmmount;


	@JsonSetter("text")
	public void setText(String t) { mText = t; }
	@JsonProperty("text")
	public String getText() { return mText; }

	@JsonSetter("transactiondate")
	public void setTransactiondate(long t) { mTransactiondate = t; }
	@JsonProperty("transactiondate")
	public long getTransactiondate() { return mTransactiondate; }

	@JsonSetter("ammount")
	public void setAmmount(float a) { mAmmount = a; }
	@JsonProperty("ammount")
	public float getAmmount() { return mAmmount; }

}