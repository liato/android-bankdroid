package com.liato.bankdroid.banking.banks.bitcoin.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Out implements Serializable {
	private static final long serialVersionUID = 3773393008409837454L;
	@JsonProperty("tx_index")
	private long mTxIndex;
	@JsonProperty("type")
	private int mType;
	@JsonProperty("addr")
	private String mAddr;
	@JsonProperty("value")
	private long mValue;
	@JsonProperty("n")
	private long mN;

	@JsonProperty("tx_index")
	public long getTxIndex() {
		return mTxIndex;
	}

	@JsonProperty("type")
	public int getType() {
		return mType;
	}

	@JsonProperty("addr")
	public String getAddr() {
		return mAddr;
	}

	@JsonProperty("value")
	public long getValue() {
		return mValue;
	}

	@JsonProperty("n")
	public long getN() {
		return mN;
	}

}