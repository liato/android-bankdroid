package com.liato.bankdroid.banking.banks.nordea.api.model.response;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.liato.bankdroid.util.ObjectUtils;

@JacksonXmlRootElement(localName = "getAccountTransactionDetailsOut")
public class GetAccountTransactionDetailsOut extends AbstractResponse {

    private Date mTransactionDate;

    private String mTransactionEntryType;

    private String mTransactionText;

    private long mFromAccountId;

    private String mFromAccountName;

    private String mToAccountId;

    private String mToAccountName;

    private String mTransactionReference;

    private String mTransactionCurrency;

    private BigDecimal mTransactionAmount;

    private Date mTransactionEntryDate;

    private Date mTransactionValueDate;

    private String mTransactionReceiptCode;

    private String mMessageRow;

    private String mTransactionArchiveId;

    private boolean mCoverReservationTransaction;

    private String mLinkIndicator;

    private String mCountryCode;

    private String mServiceType;

    private String mAccountNbr;

    private boolean mAllowedToCopy;

    private BigDecimal mExchangedFromAmount;

    private String mExchangedFromCurrency;

    private String mReceivingBankBIC;

    private String mOriginatorsReference;

    private String mUltimateDebtor;

    private String mUltimateCreditor;

    private BigDecimal mExchangedToAmount;

    private String mExchangedToCurrency;

    private String mExchangeRate;

    private BigDecimal mPrice;

    private String mTransactionsCardNr;

    private String mDebtorIdentification;

    /**
     * @return the TransactionDate
     */
    @JacksonXmlProperty(localName = "transactionDate")
    public Date getTransactionDate() {
        return mTransactionDate;
    }

    /**
     * @param pTransactionDate
     *            the TransactionDate to set
     */
    public void setTransactionDate(Date pTransactionDate) {
        this.mTransactionDate = pTransactionDate;
    }

    /**
     * @return the TransactionEntryType
     */
    @JacksonXmlProperty(localName = "transactionEntryType")
    public String getTransactionEntryType() {
        return mTransactionEntryType;
    }

    /**
     * @param pTransactionEntryType
     *            the TransactionEntryType to set
     */
    public void setTransactionEntryType(String pTransactionEntryType) {
        this.mTransactionEntryType = pTransactionEntryType;
    }

    /**
     * @return the TransactionText
     */
    public String getTransactionText() {
        return mTransactionText;
    }

    /**
     * @param pTransactionText
     *            the TransactionText to set
     */
    public void setTransactionText(String pTransactionText) {
        this.mTransactionText = pTransactionText;
    }

    /**
     * @return the FromAccountId
     */
    @JacksonXmlProperty(localName = "fromAccountId")
    public long getFromAccountId() {
        return mFromAccountId;
    }

    /**
     * @param pFromAccountId
     *            the FromAccountId to set
     */
    public void setFromAccountId(long pFromAccountId) {
        this.mFromAccountId = pFromAccountId;
    }

    /**
     * @return the FromAccountName
     */
    @JacksonXmlProperty(localName = "fromAccountName")
    public String getFromAccountName() {
        return mFromAccountName;
    }

    /**
     * @param pFromAccountName
     *            the FromAccountName to set
     */
    public void setFromAccountName(String pFromAccountName) {
        this.mFromAccountName = pFromAccountName;
    }

    /**
     * @return the ToAccountId
     */
    public String getToAccountId() {
        return mToAccountId;
    }

    /**
     * @param pToAccountId
     *            the ToAccountId to set
     */
    public void setToAccountId(String pToAccountId) {
        this.mToAccountId = pToAccountId;
    }

    /**
     * @return the ToAccountName
     */
    @JacksonXmlProperty(localName = "toAccountName")
    public String getToAccountName() {
        return mToAccountName;
    }

    /**
     * @param pToAccountName
     *            the ToAccountName to set
     */
    public void setToAccountName(String pToAccountName) {
        this.mToAccountName = pToAccountName;
    }

    /**
     * @return the TransactionReference
     */
    @JacksonXmlProperty(localName = "transactionReference")
    public String getTransactionReference() {
        return mTransactionReference;
    }

    /**
     * @param pTransactionReference
     *            the TransactionReference to set
     */
    public void setTransactionReference(String pTransactionReference) {
        this.mTransactionReference = pTransactionReference;
    }

    /**
     * @return the TransactionCurrency
     */
    @JacksonXmlProperty(localName = "transactionCurrency")
    public String getTransactionCurrency() {
        return mTransactionCurrency;
    }

    /**
     * @param pTransactionCurrency
     *            the TransactionCurrency to set
     */
    public void setTransactionCurrency(String pTransactionCurrency) {
        this.mTransactionCurrency = pTransactionCurrency;
    }

    /**
     * @return the TransactionAmount
     */
    @JacksonXmlProperty(localName = "transactionAmount")
    public BigDecimal getTransactionAmount() {
        return mTransactionAmount;
    }

    /**
     * @param pTransactionAmount
     *            the TransactionAmount to set
     */
    public void setTransactionAmount(BigDecimal pTransactionAmount) {
        this.mTransactionAmount = pTransactionAmount;
    }

    /**
     * @return the TransactionEntryDate
     */
    @JacksonXmlProperty(localName = "transactionEntryDate")
    public Date getTransactionEntryDate() {
        return mTransactionEntryDate;
    }

    /**
     * @param pTransactionEntryDate
     *            the TransactionEntryDate to set
     */
    public void setTransactionEntryDate(Date pTransactionEntryDate) {
        this.mTransactionEntryDate = pTransactionEntryDate;
    }

    /**
     * @return the TransactionValueDate
     */
    @JacksonXmlProperty(localName = "transactionValueDate")
    public Date getTransactionValueDate() {
        return mTransactionValueDate;
    }

    /**
     * @param pTransactionValueDate
     *            the TransactionValueDate to set
     */
    public void setTransactionValueDate(Date pTransactionValueDate) {
        this.mTransactionValueDate = pTransactionValueDate;
    }

    /**
     * @return the TransactionReceiptCode
     */
    @JacksonXmlProperty(localName = "transactionReceiptCode")
    public String getTransactionReceiptCode() {
        return mTransactionReceiptCode;
    }

    /**
     * @param pTransactionReceiptCode
     *            the TransactionReceiptCode to set
     */
    public void setTransactionReceiptCode(String pTransactionReceiptCode) {
        this.mTransactionReceiptCode = pTransactionReceiptCode;
    }

    /**
     * @return the mMessageRow
     */
    @JacksonXmlProperty(localName = "messageRow")
    public String getMessageRow() {
        return mMessageRow;
    }

    /**
     * @param pMessageRow
     *            the MessageRow to set
     */
    public void setMessageRow(String pMessageRow) {
        this.mMessageRow = pMessageRow;
    }

    /**
     * @return the TransactionArchiveId
     */
    @JacksonXmlProperty(localName = "transactionArchiveId")
    public String getTransactionArchiveId() {
        return mTransactionArchiveId;
    }

    /**
     * @param pTransactionArchiveId
     *            the TransactionArchiveId to set
     */
    public void setTransactionArchiveId(String pTransactionArchiveId) {
        this.mTransactionArchiveId = pTransactionArchiveId;
    }

    /**
     * @return the CoverReservationTransaction
     */
    @JacksonXmlProperty(localName = "isCoverReservationTransaction")
    public boolean ismCoverReservationTransaction() {
        return mCoverReservationTransaction;
    }

    /**
     * @param pCoverReservationTransaction
     *            the CoverReservationTransaction to set
     */
    public void setCoverReservationTransaction(boolean pCoverReservationTransaction) {
        this.mCoverReservationTransaction = pCoverReservationTransaction;
    }

    /**
     * @return the LinkIndicator
     */
    @JacksonXmlProperty(localName = "linkIndicator")
    public String getLinkIndicator() {
        return mLinkIndicator;
    }

    /**
     * @param pLinkIndicator
     *            the LinkIndicator to set
     */
    public void setLinkIndicator(String pLinkIndicator) {
        this.mLinkIndicator = pLinkIndicator;
    }

    /**
     * @return the mCountryCode
     */
    @JacksonXmlProperty(localName = "countryCode")
    public String getCountryCode() {
        return mCountryCode;
    }

    /**
     * @param pCountryCode
     *            the CountryCode to set
     */
    public void setCountryCode(String pCountryCode) {
        this.mCountryCode = pCountryCode;
    }

    /**
     * @return the ServiceType
     */
    @JacksonXmlProperty(localName = "serviceType")
    public String getServiceType() {
        return mServiceType;
    }

    /**
     * @param pServiceType
     *            the ServiceType to set
     */
    public void setServiceType(String pServiceType) {
        this.mServiceType = pServiceType;
    }

    /**
     * @return the accountNbr
     */
    @JacksonXmlProperty(localName = "accountNbr")
    public String getAccountNbr() {
        return mAccountNbr;
    }

    /**
     * @param accountNbr
     *            the accountNbr to set
     */
    public void setAccountNbr(String pAccountNbr) {
        this.mAccountNbr = pAccountNbr;
    }

    /**
     * @return the AllowedToCopy
     */
    @JacksonXmlProperty(localName = "isAllowedToCopy")
    public boolean isAllowedToCopy() {
        return mAllowedToCopy;
    }

    /**
     * @param pAllowedToCopy
     *            the AllowedToCopy to set
     */
    public void setAllowedToCopy(boolean pAllowedToCopy) {
        this.mAllowedToCopy = pAllowedToCopy;
    }

    /**
     * @return the ExchangedFromAmount
     */
    @JacksonXmlProperty(localName = "exchangedFromAmount")
    public BigDecimal getExchangedFromAmount() {
        return mExchangedFromAmount;
    }

    /**
     * @param pExchangedFromAmount
     *            the ExchangedFromAmount to set
     */
    public void setExchangedFromAmount(BigDecimal pExchangedFromAmount) {
        this.mExchangedFromAmount = pExchangedFromAmount;
    }

    /**
     * @return the ExchangedFromCurrency
     */
    @JacksonXmlProperty(localName = "exchangedFromCurrency")
    public String getExchangedFromCurrency() {
        return mExchangedFromCurrency;
    }

    /**
     * @param pExchangedFromCurrency
     *            the ExchangedFromCurrency to set
     */
    public void setExchangedFromCurrency(String pExchangedFromCurrency) {
        this.mExchangedFromCurrency = pExchangedFromCurrency;
    }

    /**
     * @return the ReceivingBankBIC
     */
    @JacksonXmlProperty(localName = "receivingBankBIC")
    public String getReceivingBankBIC() {
        return mReceivingBankBIC;
    }

    /**
     * @param pReceivingBankBIC
     *            the ReceivingBankBIC to set
     */
    public void setReceivingBankBIC(String pReceivingBankBIC) {
        this.mReceivingBankBIC = pReceivingBankBIC;
    }

    /**
     * @return the OriginatorsReference
     */
    @JacksonXmlProperty(localName = "originatorsReference")
    public String getOriginatorsReference() {
        return mOriginatorsReference;
    }

    /**
     * @param pOriginatorsReference
     *            the OriginatorsReference to set
     */
    public void setOriginatorsReference(String pOriginatorsReference) {
        this.mOriginatorsReference = pOriginatorsReference;
    }

    /**
     * @return the UltimateDebtor
     */
    @JacksonXmlProperty(localName = "ultimateDebtor")
    public String getUltimateDebtor() {
        return mUltimateDebtor;
    }

    /**
     * @param pUltimateDebtor
     *            the UltimateDebtor to set
     */
    public void setUltimateDebtor(String pUltimateDebtor) {
        this.mUltimateDebtor = pUltimateDebtor;
    }

    /**
     * @return the UltimateCreditor
     */
    @JacksonXmlProperty(localName = "ultimateCreditor")
    public String getUltimateCreditor() {
        return mUltimateCreditor;
    }

    /**
     * @param pUltimateCreditor
     *            the UltimateCreditor to set
     */
    public void setUltimateCreditor(String pUltimateCreditor) {
        this.mUltimateCreditor = pUltimateCreditor;
    }

    /**
     * @return the ExchangedToAmount
     */
    @JacksonXmlProperty(localName = "exchangedToAmount")
    public BigDecimal getExchangedToAmount() {
        return mExchangedToAmount;
    }

    /**
     * @param pExchangedToAmount
     *            the ExchangedToAmount to set
     */
    public void setExchangedToAmount(BigDecimal pExchangedToAmount) {
        this.mExchangedToAmount = pExchangedToAmount;
    }

    /**
     * @return the mExchangedToCurrency
     */
    @JacksonXmlProperty(localName = "exchangedToCurrency")
    public String getExchangedToCurrency() {
        return mExchangedToCurrency;
    }

    /**
     * @param pExchangedToCurrency
     *            the ExchangedToCurrency to set
     */
    public void setExchangedToCurrency(String pExchangedToCurrency) {
        this.mExchangedToCurrency = pExchangedToCurrency;
    }

    /**
     * @return the ExchangeRate
     */
    @JacksonXmlProperty(localName = "exchangeRate")
    public String getExchangeRate() {
        return mExchangeRate;
    }

    /**
     * @param pExchangeRate
     *            the ExchangeRate to set
     */
    public void setExchangeRate(String pExchangeRate) {
        this.mExchangeRate = pExchangeRate;
    }

    /**
     * @return the Price
     */
    @JacksonXmlProperty(localName = "price")
    public BigDecimal getPrice() {
        return mPrice;
    }

    /**
     * @param pPrice
     *            the Price to set
     */
    public void setPrice(BigDecimal pPrice) {
        this.mPrice = pPrice;
    }

    /**
     * @return the TransactionsCardNr
     */
    public String getTransactionsCardNr() {
        return mTransactionsCardNr;
    }

    /**
     * @param pTransactionsCardNr
     *            the TransactionsCardNr to set
     */
    public void setTransactionsCardNr(String pTransactionsCardNr) {
        this.mTransactionsCardNr = pTransactionsCardNr;
    }

    /**
     * @return the DebtorIdentification
     */
    @JacksonXmlProperty(localName = "debtorIdentification")
    public String getDebtorIdentification() {
        return mDebtorIdentification;
    }

    /**
     * @param pDebtorIdentification
     *            the DebtorIdentification to set
     */
    public void setDebtorIdentification(String pDebtorIdentification) {
        this.mDebtorIdentification = pDebtorIdentification;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        GetAccountTransactionDetailsOut other = (GetAccountTransactionDetailsOut) obj;
        return super.equals(obj) 
                && ObjectUtils.equal(this.mAccountNbr, other.mAccountNbr)
                && ObjectUtils.equal(this.mAllowedToCopy, other.mAllowedToCopy)
                && ObjectUtils.equal(this.mCountryCode, other.mCountryCode)
                && ObjectUtils.equal(this.mCoverReservationTransaction, other.mCoverReservationTransaction)
                && ObjectUtils.equal(this.mDebtorIdentification, other.mDebtorIdentification)
                && ObjectUtils.equal(this.mExchangedFromAmount, other.mExchangedFromAmount)
                && ObjectUtils.equal(this.mExchangedFromCurrency, other.mExchangedFromCurrency)
                && ObjectUtils.equal(this.mExchangedToAmount, this.mExchangedToAmount)
                && ObjectUtils.equal(this.mExchangedToCurrency, other.mExchangedToCurrency)
                && ObjectUtils.equal(this.mExchangeRate, other.mExchangeRate)
                && ObjectUtils.equal(this.mFromAccountId, other.mFromAccountId)
                && ObjectUtils.equal(this.mFromAccountName, other.mFromAccountName)
                && ObjectUtils.equal(this.mLinkIndicator, other.mLinkIndicator)
                && ObjectUtils.equal(this.mMessageRow, other.mMessageRow)
                && ObjectUtils.equal(this.mOriginatorsReference, other.mOriginatorsReference)
                && ObjectUtils.equal(this.mPrice, other.mPrice)
                && ObjectUtils.equal(this.mReceivingBankBIC, other.mReceivingBankBIC)
                && ObjectUtils.equal(this.mServiceType, other.mServiceType)
                && ObjectUtils.equal(this.mToAccountId, other.mToAccountId)
                && ObjectUtils.equal(this.mToAccountName, other.mToAccountName)
                && ObjectUtils.equal(this.mTransactionAmount, other.mTransactionAmount)
                && ObjectUtils.equal(this.mTransactionArchiveId, other.mTransactionArchiveId)
                && ObjectUtils.equal(this.mTransactionCurrency, other.mTransactionCurrency)
                && ObjectUtils.equal(this.mTransactionDate, other.mTransactionDate)
                && ObjectUtils.equal(this.mTransactionEntryDate, other.mTransactionEntryDate)
                && ObjectUtils.equal(this.mTransactionEntryType, other.mTransactionEntryType)
                && ObjectUtils.equal(this.mTransactionReceiptCode, other.mTransactionReceiptCode)
                && ObjectUtils.equal(this.mTransactionReference, other.mTransactionReference)
                && ObjectUtils.equal(this.mTransactionsCardNr, other.mTransactionsCardNr)
                && ObjectUtils.equal(this.mTransactionText, other.mTransactionText)
                && ObjectUtils.equal(this.mTransactionValueDate, other.mTransactionValueDate)
                && ObjectUtils.equal(this.mUltimateCreditor, other.mUltimateCreditor)
                && ObjectUtils.equal(this.mUltimateDebtor, other.mUltimateDebtor);
    }
    
    @Override
    public int hashCode() {
        return super.hashCode() +
                ObjectUtils.hashCode(this.mAccountNbr,
                this.mAllowedToCopy,
                this.mCountryCode,
                this.mCoverReservationTransaction,
                this.mDebtorIdentification,
                this.mExchangedFromAmount,
                this.mExchangedFromCurrency,
                this.mExchangedToAmount,
                this.mExchangedToCurrency,
                this.mExchangeRate,
                this.mFromAccountId,
                this.mFromAccountName,
                this.mLinkIndicator,
                this.mMessageRow,
                this.mOriginatorsReference,
                this.mPrice,
                this.mReceivingBankBIC,
                this.mServiceType,
                this.mToAccountId,
                this.mToAccountName,
                this.mTransactionAmount,
                this.mTransactionArchiveId,
                this.mTransactionCurrency,
                this.mTransactionDate,
                this.mTransactionEntryDate,
                this.mTransactionEntryType,
                this.mTransactionReceiptCode,
                this.mTransactionReference,
                this.mTransactionsCardNr,
                this.mTransactionText,
                this.mTransactionValueDate,
                this.mUltimateCreditor,
                this.mUltimateDebtor);
    }
}
