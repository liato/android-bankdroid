package com.liato.bankdroid.banking.banks.sebkort.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.io.Serializable;
import java.util.ArrayList;

public class BillingUnit implements Serializable {

    private static final long serialVersionUID = 335801680600207389L;

    private String mUnInvoicedAmount;

    private boolean mShowCreditAmount;

    private String mArrangementId;

    private ArrayList<Contract> mContracts = new ArrayList<Contract>();

    private String mCutOffDate;

    private String mInterestPercentage;

    private String mCreditAmount;

    private String mLatestPaymentDate;

    private boolean mShowInvoices;

    private String mCreditAmountNumber;

    private String mBillingUnitName;

    private String mBillingUnitId;

    private String mDisposableAmount;

    private String mBalance;

    private boolean mFinanceServiceAllowed;

    private String mLatestPaymentAmount;

    private String mArrangementNumber;

    private String mNextInvoiceDate;

    @JsonProperty("unInvoicedAmount")
    public String getUnInvoicedAmount() {
        return mUnInvoicedAmount;
    }

    @JsonSetter("unInvoicedAmount")
    public void setUnInvoicedAmount(String u) {
        mUnInvoicedAmount = u;
    }

    @JsonProperty("showCreditAmount")
    public boolean getShowCreditAmount() {
        return mShowCreditAmount;
    }

    @JsonSetter("showCreditAmount")
    public void setShowCreditAmount(boolean s) {
        mShowCreditAmount = s;
    }

    @JsonProperty("arrangementId")
    public String getArrangementId() {
        return mArrangementId;
    }

    @JsonSetter("arrangementId")
    public void setArrangementId(String a) {
        mArrangementId = a;
    }

    @JsonProperty("contracts")
    public ArrayList<Contract> getContracts() {
        return mContracts;
    }

    @JsonSetter("contracts")
    public void setContracts(ArrayList<Contract> c) {
        mContracts = c;
    }

    @JsonProperty("cutOffDate")
    public String getCutOffDate() {
        return mCutOffDate;
    }

    @JsonSetter("cutOffDate")
    public void setCutOffDate(String c) {
        mCutOffDate = c;
    }

    @JsonProperty("interestPercentage")
    public String getInterestPercentage() {
        return mInterestPercentage;
    }

    @JsonSetter("interestPercentage")
    public void setInterestPercentage(String i) {
        mInterestPercentage = i;
    }

    @JsonProperty("creditAmount")
    public String getCreditAmount() {
        return mCreditAmount;
    }

    @JsonSetter("creditAmount")
    public void setCreditAmount(String c) {
        mCreditAmount = c;
    }

    @JsonProperty("latestPaymentDate")
    public String getLatestPaymentDate() {
        return mLatestPaymentDate;
    }

    @JsonSetter("latestPaymentDate")
    public void setLatestPaymentDate(String l) {
        mLatestPaymentDate = l;
    }

    @JsonProperty("showInvoices")
    public boolean getShowInvoices() {
        return mShowInvoices;
    }

    @JsonSetter("showInvoices")
    public void setShowInvoices(boolean s) {
        mShowInvoices = s;
    }

    @JsonProperty("creditAmountNumber")
    public String getCreditAmountNumber() {
        return mCreditAmountNumber;
    }

    @JsonSetter("creditAmountNumber")
    public void setCreditAmountNumber(String c) {
        mCreditAmountNumber = c;
    }

    @JsonProperty("billingUnitName")
    public String getBillingUnitName() {
        return mBillingUnitName;
    }

    @JsonSetter("billingUnitName")
    public void setBillingUnitName(String b) {
        mBillingUnitName = b;
    }

    @JsonProperty("billingUnitId")
    public String getBillingUnitId() {
        return mBillingUnitId;
    }

    @JsonSetter("billingUnitId")
    public void setBillingUnitId(String b) {
        mBillingUnitId = b;
    }

    @JsonProperty("disposableAmount")
    public String getDisposableAmount() {
        return mDisposableAmount;
    }

    @JsonSetter("disposableAmount")
    public void setDisposableAmount(String d) {
        mDisposableAmount = d;
    }

    @JsonProperty("balance")
    public String getBalance() {
        return mBalance;
    }

    @JsonSetter("balance")
    public void setBalance(String b) {
        mBalance = b;
    }

    @JsonProperty("financeServiceAllowed")
    public boolean getFinanceServiceAllowed() {
        return mFinanceServiceAllowed;
    }

    @JsonSetter("financeServiceAllowed")
    public void setFinanceServiceAllowed(boolean f) {
        mFinanceServiceAllowed = f;
    }

    @JsonProperty("latestPaymentAmount")
    public String getLatestPaymentAmount() {
        return mLatestPaymentAmount;
    }

    @JsonSetter("latestPaymentAmount")
    public void setLatestPaymentAmount(String l) {
        mLatestPaymentAmount = l;
    }

    @JsonProperty("arrangementNumber")
    public String getArrangementNumber() {
        return mArrangementNumber;
    }

    @JsonSetter("arrangementNumber")
    public void setArrangementNumber(String a) {
        mArrangementNumber = a;
    }

    @JsonProperty("nextInvoiceDate")
    public String getNextInvoiceDate() {
        return mNextInvoiceDate;
    }

    @JsonSetter("nextInvoiceDate")
    public void setNextInvoiceDate(String n) {
        mNextInvoiceDate = n;
    }

}