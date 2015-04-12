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

    @JsonProperty("transferTo")
    public boolean getTransferTo() {
        return mTransferTo;
    }

    @JsonSetter("transferTo")
    public void setTransferTo(boolean t) {
        mTransferTo = t;
    }

    @JsonProperty("productCode")
    public String getProductCode() {
        return mProductCode;
    }

    @JsonSetter("productCode")
    public void setProductCode(String p) {
        mProductCode = p;
    }

    @JsonProperty("youthAccount")
    public boolean getYouthAccount() {
        return mYouthAccount;
    }

    @JsonSetter("youthAccount")
    public void setYouthAccount(boolean y) {
        mYouthAccount = y;
    }

    @JsonProperty("accountNumber")
    public String getAccountNumber() {
        return mAccountNumber;
    }

    @JsonSetter("accountNumber")
    public void setAccountNumber(String a) {
        mAccountNumber = a;
    }

    @JsonProperty("clearingNumber")
    public String getClearingNumber() {
        return mClearingNumber;
    }

    @JsonSetter("clearingNumber")
    public void setClearingNumber(String c) {
        mClearingNumber = c;
    }

    @JsonProperty("transferFrom")
    public boolean getTransferFrom() {
        return mTransferFrom;
    }

    @JsonSetter("transferFrom")
    public void setTransferFrom(boolean t) {
        mTransferFrom = t;
    }

    @JsonProperty("ledger")
    public String getLedger() {
        return mLedger;
    }

    @JsonSetter("ledger")
    public void setLedger(String l) {
        mLedger = l;
    }

    @JsonProperty("accountName")
    public String getAccountName() {
        return mAccountName;
    }

    @JsonSetter("accountName")
    public void setAccountName(String a) {
        mAccountName = a;
    }

    @JsonProperty("dispoibleAmount")
    public float getDispoibleAmount() {
        return mDispoibleAmount;
    }

    @JsonSetter("dispoibleAmount")
    public void setDispoibleAmount(float d) {
        mDispoibleAmount = d;
    }

    @JsonProperty("balance")
    public float getBalance() {
        return mBalance;
    }

    @JsonSetter("balance")
    public void setBalance(float b) {
        mBalance = b;
    }

}
