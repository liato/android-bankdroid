package com.liato.bankdroid.banking.banks.nordea.api.model;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.liato.bankdroid.util.ObjectUtils;

public class NordeaTransaction {

	private String mTransactionKey;

	private Date mTransactionDate;
	
	private String mTransactionText;
	
	private String mTransactionCounterpartyName;
	
	private String mTransactionCurrency;
	
	private BigDecimal mTransactionAmount;
	
	private boolean mCoverReservationTransaction;
	
	/**
	 * @return the TransactionKey
	 */
	@JacksonXmlProperty(localName="transactionKey")
	public String getTransactionKey() {
		return mTransactionKey;
	}

	/**
	 * @param pTransactionKey the TransactionKey to set
	 */
	public void setTransactionKey(String pTransactionKey) {
		this.mTransactionKey = pTransactionKey;
	}

	/**
	 * @return the TransactionDate
	 */
	@JacksonXmlProperty(localName="transactionDate")
	public Date getTransactionDate() {
		return mTransactionDate;
	}

	/**
	 * @param pTransactionDate the TransactionDate to set
	 */
	public void setTransactionDate(Date pTransactionDate) {
		this.mTransactionDate = pTransactionDate;
	}

	/**
	 * @return the mTransactionText
	 */
	@JacksonXmlProperty(localName="transactionText")
	public String getTransactionText() {
		return mTransactionText;
	}

	/**
	 * @param pTransactionText the TransactionText to set
	 */
	public void setTransactionText(String pTransactionText) {
		this.mTransactionText = pTransactionText;
	}

	/**
	 * @return the TransactionCounterpartyName
	 */
	public String getTransactionCounterpartyName() {
		return mTransactionCounterpartyName;
	}

	/**
	 * @param pTransactionCounterpartyName the TransactionCounterpartyName to set
	 */
	public void setTransactionCounterpartyName(String pTransactionCounterpartyName) {
		this.mTransactionCounterpartyName = pTransactionCounterpartyName;
	}

	/**
	 * @return the TransactionCurrency
	 */
	@JacksonXmlProperty(localName="transactionCurrency")
	public String getTransactionCurrency() {
		return mTransactionCurrency;
	}

	/**
	 * @param pTransactionCurrency the TransactionCurrency to set
	 */
	public void setTransactionCurrency(String pTransactionCurrency) {
		this.mTransactionCurrency = pTransactionCurrency;
	}

	/**
	 * @return the mTransactionAmount
	 */
	@JacksonXmlProperty(localName="transactionAmount")
	public BigDecimal getTransactionAmount() {
		return mTransactionAmount;
	}

	/**
	 * @param pTransactionAmount the TransactionAmount to set
	 */
	public void setTransactionAmount(BigDecimal pTransactionAmount) {
		this.mTransactionAmount = pTransactionAmount;
	}

	/**
	 * @return the mCoverReservationTransaction
	 */
	@JacksonXmlProperty(localName="isCoverReservationTransaction")
	public boolean isCoverReservationTransaction() {
		return mCoverReservationTransaction;
	}

	/**
	 * @param pCoverReservationTransaction the CoverReservationTransaction to set
	 */
	public void setCoverReservationTransaction(boolean pCoverReservationTransaction) {
		this.mCoverReservationTransaction = pCoverReservationTransaction;
	}
	
	public boolean equals(Object obj) {
	    if(obj == null) {
	        return false;
	    }
	    if(getClass() != obj.getClass()) {
	        return false;
	    }
	    NordeaTransaction other = (NordeaTransaction) obj;
	    return ObjectUtils.equal(this.mCoverReservationTransaction, other.mCoverReservationTransaction) 
	            && ObjectUtils.equal(this.mTransactionAmount, other.mTransactionAmount)
	            && ObjectUtils.equal(this.mTransactionCounterpartyName, other.mTransactionCounterpartyName)
	            && ObjectUtils.equal(this.mTransactionCurrency, other.mTransactionCurrency)
	            && ObjectUtils.equal(this.mTransactionDate, other.mTransactionDate)
	            && ObjectUtils.equal(this.mTransactionKey, other.mTransactionKey)
	            && ObjectUtils.equal(this.mTransactionText, other.mTransactionText);
	}
	public int hashCode() {
	    return ObjectUtils.hashCode(this.mCoverReservationTransaction,
	            this.mTransactionAmount,
	            this.mTransactionCounterpartyName,
	            this.mTransactionCurrency,
	            this.mTransactionDate,
	            this.mTransactionKey,
	            this.mTransactionText);
	}
}
