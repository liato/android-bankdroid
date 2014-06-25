package com.liato.bankdroid.banking.banks.icabanken.model;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IcaBankenTransaction {

    private BigDecimal mAccountBalance;

    private BigDecimal mAmount;

    private String mMemoText;

    private Date mPostedDate;

    @JsonProperty("AccountBalance")
    public BigDecimal getAccountBalance() {
        return mAccountBalance;
    }

    public void setAccountBalance(BigDecimal pAccountBalance) {
        this.mAccountBalance = pAccountBalance;
    }

    @JsonProperty("Amount")
    public BigDecimal getAmount() {
        return mAmount;
    }

    public void setAmount(BigDecimal pAmount) {
        this.mAmount = pAmount;
    }

    @JsonProperty("MemoText")
    public String getMemoText() {
        return mMemoText;
    }

    public void setMemoText(String pMemoText) {
        this.mMemoText = pMemoText;
    }

    @JsonProperty("PostedDate")
    public Date getPostedDate() {
        return mPostedDate;
    }

    public void setPostedDate(Date pPostedDate) {
        this.mPostedDate = pPostedDate;
    }
}
