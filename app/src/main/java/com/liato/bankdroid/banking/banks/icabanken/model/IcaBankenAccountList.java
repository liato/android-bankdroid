package com.liato.bankdroid.banking.banks.icabanken.model;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IcaBankenAccountList {

    private List<IcaBankenAccount> mAccounts;

    private String mDefaultAccountIdForEgiros;
    private String mDefaultAccountIdForPayments;
    private String mDefaultAccountIdForTransfers;
    private BigDecimal mJointAccountsTotalAvailableAmount;

    private BigDecimal mJointAccountsTotalCurrentAmount;
    private BigDecimal mMinorsAccountsTotalAvailableAmount;
    private BigDecimal mMinorsAccountsTotalCurrentAmount;
    private BigDecimal mOwnAccountsTotalAvailableAmount;
    private BigDecimal mOwnAccountsTotalCurrentAmount;

    @JsonProperty("Accounts")
    public List<IcaBankenAccount> getAccounts() {
        return mAccounts;
    }

    public void setAccounts(List<IcaBankenAccount> pAccounts) {
        this.mAccounts = pAccounts;
    }

    @JsonProperty("DefaultAccountIdForEgiros")
    public String getDefaultAccountIdForEgiros() {
        return mDefaultAccountIdForEgiros;
    }

    public void setDefaultAccountIdForEgiros(String pDefaultAccountIdForEgiros) {
        this.mDefaultAccountIdForEgiros = pDefaultAccountIdForEgiros;
    }

    @JsonProperty("DefaultAccountIdForPayments")
    public String getDefaultAccountIdForPayments() {
        return mDefaultAccountIdForPayments;
    }

    public void setDefaultAccountIdForPayments(
            String pDefaultAccountIdForPayments) {
        this.mDefaultAccountIdForPayments = pDefaultAccountIdForPayments;
    }

    @JsonProperty("DefaultAccountIdForTransfers")
    public String getDefaultAccountIdForTransfers() {
        return mDefaultAccountIdForTransfers;
    }

    public void setDefaultAccountIdForTransfers(
            String pDefaultAccountIdForTransfers) {
        this.mDefaultAccountIdForTransfers = pDefaultAccountIdForTransfers;
    }

    @JsonProperty("JointAccountsTotalAvailableAmount")
    public BigDecimal getJointAccountsTotalAvailableAmount() {
        return mJointAccountsTotalAvailableAmount;
    }

    public void setJointAccountsTotalAvailableAmount(
            BigDecimal pJointAccountsTotalAvailableAmount) {
        this.mJointAccountsTotalAvailableAmount = pJointAccountsTotalAvailableAmount;
    }

    @JsonProperty("JointAccountsTotalCurrentAmount")
    public BigDecimal getJointAccountsTotalCurrentAmount() {
        return mJointAccountsTotalCurrentAmount;
    }

    public void setJointAccountsTotalCurrentAmount(
            BigDecimal pJointAccountsTotalCurrentAmount) {
        this.mJointAccountsTotalCurrentAmount = pJointAccountsTotalCurrentAmount;
    }

    @JsonProperty("MinorsAccountsTotalAvailableAmount")
    public BigDecimal getMinorsAccountsTotalAvailableAmount() {
        return mMinorsAccountsTotalAvailableAmount;
    }

    public void setMinorsAccountsTotalAvailableAmount(
            BigDecimal pMinorsAccountsTotalAvailableAmount) {
        this.mMinorsAccountsTotalAvailableAmount = pMinorsAccountsTotalAvailableAmount;
    }

    @JsonProperty("MinorsAccountsTotalCurrentAmount")
    public BigDecimal getMinorsAccountsTotalCurrentAmount() {
        return mMinorsAccountsTotalCurrentAmount;
    }

    public void setMinorsAccountsTotalCurrentAmount(
            BigDecimal pMinorsAccountsTotalCurrentAmount) {
        this.mMinorsAccountsTotalCurrentAmount = pMinorsAccountsTotalCurrentAmount;
    }

    @JsonProperty("OwnAccountsTotalAvailableAmount")
    public BigDecimal getOwnAccountsTotalAvailableAmount() {
        return mOwnAccountsTotalAvailableAmount;
    }

    public void setOwnAccountsTotalAvailableAmount(
            BigDecimal pOwnAccountsTotalAvailableAmount) {
        this.mOwnAccountsTotalAvailableAmount = pOwnAccountsTotalAvailableAmount;
    }

    @JsonProperty("OwnAccountsTotalCurrentAmount")
    public BigDecimal getOwnAccountsTotalCurrentAmount() {
        return mOwnAccountsTotalCurrentAmount;
    }

    public void setOwnAccountsTotalCurrentAmount(
            BigDecimal pOwnAccountsTotalCurrentAmount) {
        this.mOwnAccountsTotalCurrentAmount = pOwnAccountsTotalCurrentAmount;
    }
}
