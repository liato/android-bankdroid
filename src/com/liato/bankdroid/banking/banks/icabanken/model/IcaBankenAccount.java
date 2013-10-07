package com.liato.bankdroid.banking.banks.icabanken.model;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IcaBankenAccount {
	private String mAccountNumber;

	private BigDecimal mAvailableAmount;

	private BigDecimal mCurrentAmount;

	private String mName;

	private List<IcaBankenTransaction> mTransactions;

	@JsonProperty("AccountNumber")
	public String getAccountNumber() {
		return mAccountNumber;
	}

	public void setAccountNumber(String pAccountNumber) {
		this.mAccountNumber = pAccountNumber;
	}

	@JsonProperty("AvailableAmount")
	public BigDecimal getAvailableAmount() {
		return mAvailableAmount;
	}

	public void setAvailableAmount(BigDecimal pAvailableAmount) {
		this.mAvailableAmount = pAvailableAmount;
	}

	@JsonProperty("CurrentAmount")
	public BigDecimal getCurrentAmount() {
		return mCurrentAmount;
	}

	public void setCurrentAmount(BigDecimal pCurrentAmount) {
		this.mCurrentAmount = pCurrentAmount;
	}

	@JsonProperty("Name")
	public String getName() {
		return mName;
	}

	public void setName(String pName) {
		this.mName = pName;
	}

	@JsonProperty("Transactions")
	public List<IcaBankenTransaction> getTransactions() {
		return mTransactions;
	}

	public void setTransactions(List<IcaBankenTransaction> pTransactions) {
		this.mTransactions = pTransactions;
	}
}
