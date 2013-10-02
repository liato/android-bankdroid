package com.liato.bankdroid.banking.banks.bitcoin.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BlockchainResponse implements Serializable {
	private static final long serialVersionUID = -5144109898538621019L;
	@JsonProperty("total_sent")
	private long mTotalSent;
	@JsonProperty("total_received")
	private long mTotalReceived;
	@JsonProperty("final_balance")
	private long mFinalBalance;
	@JsonProperty("address")
	private String mAddress;
	@JsonProperty("hash160")
	private String mHash160;
	@JsonProperty("txs")
	private List<Transfer> mTxs = Collections.emptyList();
	@JsonProperty("n_tx")
	private long mNTx;

	@JsonProperty("total_sent")
	public long getTotalSent() {
		return mTotalSent;
	}

	@JsonProperty("total_received")
	public long getTotalReceived() {
		return mTotalReceived;
	}

	@JsonProperty("final_balance")
	public long getFinalBalance() {
		return mFinalBalance;
	}

	@JsonProperty("address")
	public String getAddress() {
		return mAddress;
	}

	@JsonProperty("hash160")
	public String getHash160() {
		return mHash160;
	}

	@JsonProperty("txs")
	public List<Transfer> getTxs() {
		return mTxs;
	}

	@JsonProperty("n_tx")
	public long getNTx() {
		return mNTx;
	}

}