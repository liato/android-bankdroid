package com.liato.bankdroid.banking.banks.lansforsakringar.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class Account {
	private boolean mTransferTo;
	private String mProductCode;
	private boolean mYouthAccount;
	private String mAccountNumber;
	private String mClearingNumber;
	private boolean mTransferFrom;
	private String mLedger;
	private String mAccountName;
	private float mDispoibleAmount;
	private float mBalance;


	@JsonSetter("transferTo")
	public void setTransferTo(boolean t) { mTransferTo = t; }
	@JsonProperty("transferTo")
	public boolean getTransferTo() { return mTransferTo; }

	@JsonSetter("productCode")
	public void setProductCode(String p) { mProductCode = p; }
	@JsonProperty("productCode")
	public String getProductCode() { return mProductCode; }

	@JsonSetter("youthAccount")
	public void setYouthAccount(boolean y) { mYouthAccount = y; }
	@JsonProperty("youthAccount")
	public boolean getYouthAccount() { return mYouthAccount; }

	@JsonSetter("accountNumber")
	public void setAccountNumber(String a) { mAccountNumber = a; }
	@JsonProperty("accountNumber")
	public String getAccountNumber() { return mAccountNumber; }

	@JsonSetter("clearingNumber")
	public void setClearingNumber(String c) { mClearingNumber = c; }
	@JsonProperty("clearingNumber")
	public String getClearingNumber() { return mClearingNumber; }

	@JsonSetter("transferFrom")
	public void setTransferFrom(boolean t) { mTransferFrom = t; }
	@JsonProperty("transferFrom")
	public boolean getTransferFrom() { return mTransferFrom; }

	@JsonSetter("ledger")
	public void setLedger(String l) { mLedger = l; }
	@JsonProperty("ledger")
	public String getLedger() { return mLedger; }

	@JsonSetter("accountName")
	public void setAccountName(String a) { mAccountName = a; }
	@JsonProperty("accountName")
	public String getAccountName() { return mAccountName; }

	@JsonSetter("dispoibleAmount")
	public void setDispoibleAmount(float d) { mDispoibleAmount = d; }
	@JsonProperty("dispoibleAmount")
	public float getDispoibleAmount() { return mDispoibleAmount; }

	@JsonSetter("balance")
	public void setBalance(float b) { mBalance = b; }
	@JsonProperty("balance")
	public float getBalance() { return mBalance; }

}