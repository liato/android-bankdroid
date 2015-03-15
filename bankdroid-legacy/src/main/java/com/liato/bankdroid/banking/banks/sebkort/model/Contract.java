package com.liato.bankdroid.banking.banks.sebkort.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.io.Serializable;

public class Contract implements Serializable {

    private static final long serialVersionUID = 9210413430068677151L;

    private String mUnInvoicedAmount;

    private String mContractName;

    private String mCreditAmount;

    private String mContractId;

    @JsonProperty("unInvoicedAmount")
    public String getUnInvoicedAmount() {
        return mUnInvoicedAmount;
    }

    @JsonSetter("unInvoicedAmount")
    public void setUnInvoicedAmount(String u) {
        mUnInvoicedAmount = u;
    }

    @JsonProperty("contractName")
    public String getContractName() {
        return mContractName;
    }

    @JsonSetter("contractName")
    public void setContractName(String c) {
        mContractName = c;
    }

    @JsonProperty("creditAmount")
    public String getCreditAmount() {
        return mCreditAmount;
    }

    @JsonSetter("creditAmount")
    public void setCreditAmount(String c) {
        mCreditAmount = c;
    }

    @JsonProperty("contractId")
    public String getContractId() {
        return mContractId;
    }

    @JsonSetter("contractId")
    public void setContractId(String c) {
        mContractId = c;
    }

}