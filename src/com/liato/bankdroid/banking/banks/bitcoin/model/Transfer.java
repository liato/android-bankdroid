package com.liato.bankdroid.banking.banks.bitcoin.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Transfer implements Serializable {
	private static final long serialVersionUID = -2558362412750999606L;
	@JsonProperty("size")
	private long mSize;
	@JsonProperty("inputs")
	private List<Input> mInputs = Collections.emptyList();
	@JsonProperty("hash")
	private String mHash;
	@JsonProperty("tx_index")
	private long mTxIndex;
	@JsonProperty("relayed_by")
	private String mRelayedBy;
	@JsonProperty("block_height")
	private long mBlockHeight;
	@JsonProperty("vin_sz")
	private long mVinSz;
	@JsonProperty("vout_sz")
	private long mVoutSz;
	@JsonProperty("time")
	private long mTime;
	@JsonProperty("ver")
	private long mVer;
	@JsonProperty("out")
	private List<Out> mOut = Collections.emptyList();
	@JsonProperty("result")
	private long mResult;

	@JsonProperty("size")
	public long getSize() {
		return mSize;
	}

	@JsonProperty("inputs")
	public List<Input> getInputs() {
		return mInputs;
	}

	@JsonProperty("hash")
	public String getHash() {
		return mHash;
	}

	@JsonProperty("tx_index")
	public long getTxIndex() {
		return mTxIndex;
	}

	@JsonProperty("relayed_by")
	public String getRelayedBy() {
		return mRelayedBy;
	}

	@JsonProperty("block_height")
	public long getBlockHeight() {
		return mBlockHeight;
	}

	@JsonProperty("vin_sz")
	public long getVinSz() {
		return mVinSz;
	}

	@JsonProperty("vout_sz")
	public long getVoutSz() {
		return mVoutSz;
	}

	@JsonProperty("time")
	public long getTime() {
		return mTime;
	}

	@JsonProperty("ver")
	public long getVer() {
		return mVer;
	}

	@JsonProperty("out")
	public List<Out> getOut() {
		return mOut;
	}

	@JsonProperty("result")
	public long getResult() {
		return mResult;
	}

}