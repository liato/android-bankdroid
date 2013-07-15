package com.liato.bankdroid.banking.banks.nordea.api.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.liato.bankdroid.util.ObjectUtils;

@JsonPropertyOrder({ "accountId", "accountNumber", "accountType", "productTypeExtension", "currency", "nickName",
        "balance", "fundsAvailable", "branchId" })
public class NordeaAccount {

    private AccountId mAccoundId;

    private long mAccountNumber;

    private int mAccountType;

    private String mProductTypeExtension;

    private String mCurrency;

    private String mNickname;

    private BigDecimal mBalance;

    private BigDecimal mFundsAvailable;

    private String mBranchId;

    /**
     * @return the mAccoundId
     */
    @JacksonXmlProperty(localName = "accountId")
    public AccountId getAccoundId() {
        return mAccoundId;
    }

    /**
     * @param mAccoundId
     *            the mAccoundId to set
     */
    public void setAccoundId(AccountId pAccoundId) {
        this.mAccoundId = pAccoundId;
    }

    /**
     * @return the mAccountNumber
     */
    @JacksonXmlProperty(localName = "accountNumber")
    public long getAccountNumber() {
        return mAccountNumber;
    }

    /**
     * @param mAccountNumber
     *            the mAccountNumber to set
     */
    public void setAccountNumber(long pAccountNumber) {
        this.mAccountNumber = pAccountNumber;
    }

    /**
     * @return the mAccountType
     */
    @JacksonXmlProperty(localName = "accountType")
    public int getAccountType() {
        return mAccountType;
    }

    /**
     * @param mAccountType
     *            the mAccountType to set
     */
    public void setAccountType(int pAccountType) {
        this.mAccountType = pAccountType;
    }

    /**
     * @return the mProductTypeExtension
     */
    @JacksonXmlProperty(localName = "productTypeExtension")
    public String getProductTypeExtension() {
        return mProductTypeExtension;
    }

    /**
     * @param mProductTypeExtension
     *            the mProductTypeExtension to set
     */
    public void setProductTypeExtension(String pProductTypeExtension) {
        this.mProductTypeExtension = pProductTypeExtension;
    }

    /**
     * @return the mCurrency
     */
    @JacksonXmlProperty(localName = "currency")
    public String getCurrency() {
        return mCurrency;
    }

    /**
     * @param mCurrency
     *            the mCurrency to set
     */
    public void setCurrency(String pCurrency) {
        this.mCurrency = pCurrency;
    }

    /**
     * @return the mNickname
     */
    @JacksonXmlProperty(localName = "nickName")
    public String getNickname() {
        return mNickname;
    }

    /**
     * @param mNickname
     *            the mNickname to set
     */
    public void setNickname(String pNickname) {
        this.mNickname = pNickname;
    }

    /**
     * @return the mBalance
     */
    @JacksonXmlProperty(localName = "balance")
    public BigDecimal getBalance() {
        return mBalance;
    }

    /**
     * @param mBalance
     *            the mBalance to set
     */
    public void setBalance(BigDecimal pBalance) {
        this.mBalance = pBalance;
    }

    /**
     * @return the mFundsAvailable
     */
    @JacksonXmlProperty(localName = "fundsAvailable")
    public BigDecimal getFundsAvailable() {
        return mFundsAvailable;
    }

    /**
     * @param mFundsAvailable
     *            the mFundsAvailable to set
     */
    public void setFundsAvailable(BigDecimal pFundsAvailable) {
        this.mFundsAvailable = pFundsAvailable;
    }

    /**
     * @return the mBranchId
     */
    @JacksonXmlProperty(localName = "branchId")
    public String getBranchId() {
        return mBranchId;
    }

    /**
     * @param mBranchId
     *            the mBranchId to set
     */
    public void setBranchId(String pBranchId) {
        this.mBranchId = pBranchId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NordeaAccount other = (NordeaAccount) obj;
        return ObjectUtils.equal(this.mAccoundId, other.mAccoundId)
                && ObjectUtils.equal(this.mAccountNumber,other.mAccountNumber)
                && ObjectUtils.equal(this.mAccountType, other.mAccountType)
                && ObjectUtils.equal(this.mBalance, other.mBalance)
                && ObjectUtils.equal(this.mBranchId, other.mBranchId)
                && ObjectUtils.equal(this.mCurrency, other.mCurrency)
                && ObjectUtils.equal(this.mNickname, other.mNickname)
                && ObjectUtils.equal(this.mProductTypeExtension, other.mProductTypeExtension);
    }
    @Override
    public int hashCode() {
        return ObjectUtils.hashCode(this.mAccoundId, 
                this.mAccountNumber,
                this.mAccountType,
                this.mBalance,
                this.mBranchId,
                this.mCurrency,
                this.mFundsAvailable,
                this.mNickname,
                this.mProductTypeExtension);
    }
}
