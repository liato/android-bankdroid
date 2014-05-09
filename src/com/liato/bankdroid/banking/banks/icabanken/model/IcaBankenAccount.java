package com.liato.bankdroid.banking.banks.icabanken.model;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IcaBankenAccount {
    private String mAccountId;

    private String mAccountNumber;

    private String mAddress;

    private BigDecimal mAvailableAmount;

    private String mBic;

    private BigDecimal mCurrentAmount;

    private String mGroup;

    private String mHolder;

    private String mIban;

    private String mName;

    private BigDecimal mOutstandingAmount;

    private List<String> mServices;

    private List<IcaBankenTransaction> mTransactions;

    private List<String> mValidFor;

    @JsonProperty("AccountId")
    public String getAccountId() {
        return mAccountId;
    }

    public void setAccountId(String pAccountId) {
        this.mAccountId = pAccountId;
    }

    @JsonProperty("AccountNumber")
    public String getAccountNumber() {
        return mAccountNumber;
    }

    public void setAccountNumber(String pAccountNumber) {
        this.mAccountNumber = pAccountNumber;
    }

    @JsonProperty("Address")
    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String pAddress) {
        this.mAddress = pAddress;
    }

    @JsonProperty("AvailableAmount")
    public BigDecimal getAvailableAmount() {
        return mAvailableAmount;
    }

    public void setAvailableAmount(BigDecimal pAvailableAmount) {
        this.mAvailableAmount = pAvailableAmount;
    }

    @JsonProperty("BIC")
    public String getBic() {
        return mBic;
    }

    public void setBic(String pBic) {
        this.mBic = pBic;
    }

    @JsonProperty("CurrentAmount")
    public BigDecimal getCurrentAmount() {
        return mCurrentAmount;
    }

    public void setCurrentAmount(BigDecimal pCurrentAmount) {
        this.mCurrentAmount = pCurrentAmount;
    }

    @JsonProperty("Group")
    public String getGroup() {
        return mGroup;
    }

    public void setGroup(String pGroup) {
        this.mGroup = pGroup;
    }

    @JsonProperty("Holder")
    public String getHolder() {
        return mHolder;
    }

    public void setHolder(String pHolder) {
        this.mHolder = pHolder;
    }

    @JsonProperty("IBAN")
    public String getIban() {
        return mIban;
    }

    public void setIban(String pIban) {
        this.mIban = pIban;
    }

    @JsonProperty("Name")
    public String getName() {
        return mName;
    }

    public void setName(String pName) {
        this.mName = pName;
    }

    @JsonProperty("OutstandingAmount")
    public BigDecimal getOutstandingAmount() {
        return mOutstandingAmount;
    }

    public void setOutstandingAmount(BigDecimal pOutstandingAmount) {
        this.mOutstandingAmount = pOutstandingAmount;
    }

    @JsonProperty("Services")
    public List<String> getServices() {
        return mServices;
    }

    public void setServices(List<String> pServices) {
        this.mServices = pServices;
    }

    @JsonProperty("Transactions")
    public List<IcaBankenTransaction> getTransactions() {
        return mTransactions;
    }

    public void setTransactions(List<IcaBankenTransaction> pTransactions) {
        this.mTransactions = pTransactions;
    }

    @JsonProperty("ValidFor")
    public List<String> getValidFor() {
        return mValidFor;
    }

    public void setValidFor(List<String> pValidFor) {
        this.mValidFor = pValidFor;
    }
}
