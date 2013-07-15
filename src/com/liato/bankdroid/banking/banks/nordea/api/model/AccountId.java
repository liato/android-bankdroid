package com.liato.bankdroid.banking.banks.nordea.api.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.liato.bankdroid.util.ObjectUtils;


@JacksonXmlRootElement(localName="accountId")
public class AccountId {

	private String mId;

	private boolean mView;
	
	private boolean mPay;
	
	private boolean mDeposit;
	
	private boolean mOwnTransferFrom;
	
	private boolean mOwnTransferTo;
	
	private boolean mThirdParty;
	
	private String mAccountId;
	
	/**
	 * @return the mId
	 */
	@JacksonXmlProperty(localName="id",isAttribute=true)
	public String getId() {
		return mId;
	}

	/**
	 * @param mId the mId to set
	 */
	public void setId(String pId) {
		this.mId = pId;
	}

	/**
	 * @return the mView
	 */
	@JacksonXmlProperty(localName="view",isAttribute=true)
	public boolean isView() {
		return mView;
	}

	/**
	 * @param mView the mView to set
	 */
	public void setView(boolean pView) {
		this.mView = pView;
	}

	/**
	 * @return the mPay
	 */
	@JacksonXmlProperty(localName="pay",isAttribute=true)
	public boolean isPay() {
		return mPay;
	}

	/**
	 * @param mPay the mPay to set
	 */
	public void setPay(boolean pPay) {
		this.mPay = pPay;
	}

	/**
	 * @return the deposit
	 */
	@JacksonXmlProperty(localName="deposit",isAttribute=true)
	public boolean isDeposit() {
		return mDeposit;
	}

	/**
	 * @param deposit the deposit to set
	 */
	public void setDeposit(boolean pDeposit) {
		this.mDeposit = pDeposit;
	}

	/**
	 * @return the mOwnTransferFrom
	 */
	@JacksonXmlProperty(localName="ownTransferFrom",isAttribute=true)
	public boolean isOwnTransferFrom() {
		return mOwnTransferFrom;
	}

	/**
	 * @param mOwnTransferFrom the mOwnTransferFrom to set
	 */
	public void setOwnTransferFrom(boolean pOwnTransferFrom) {
		this.mOwnTransferFrom = pOwnTransferFrom;
	}

	/**
	 * @return the mOwnTransferTo
	 */
	@JacksonXmlProperty(localName="ownTransferTo",isAttribute=true)
	public boolean isOwnTransferTo() {
		return mOwnTransferTo;
	}

	/**
	 * @param mOwnTransferTo the mOwnTransferTo to set
	 */
	public void setOwnTransferTo(boolean pOwnTransferTo) {
		this.mOwnTransferTo = pOwnTransferTo;
	}

	/**
	 * @return the mThirdParty
	 */
	@JacksonXmlProperty(localName="thirdParty",isAttribute=true)
	public boolean isThirdParty() {
		return mThirdParty;
	}

	/**
	 * @param mThirdParty the mThirdParty to set
	 */
	public void setThirdParty(boolean pThirdParty) {
		this.mThirdParty = pThirdParty;
	}

	/**
	 * @return the mAccountId
	 */
	@JacksonXmlProperty(localName="$")
	public String getAccountId() {
		return mAccountId;
	}

	/**
	 * @param mAccountId the mAccountId to set
	 */
	public void setAccountId(String pAccountId) {
		this.mAccountId = pAccountId;
	}	
	
	@Override
	public boolean equals(Object obj) {
	    if(obj == null) {
	        return false;
	    }
	    if(getClass() != obj.getClass()) {
	        return false;
	    }
	    final AccountId other = (AccountId) obj;
	    return ObjectUtils.equal(this.mAccountId, other.mAccountId)
	            && ObjectUtils.equal(this.mDeposit, other.mDeposit)
	            && ObjectUtils.equal(this.mId, other.mId)
	            && ObjectUtils.equal(this.mOwnTransferFrom, other.mOwnTransferFrom)
	            && ObjectUtils.equal(this.mOwnTransferTo, other.mOwnTransferTo)
	            && ObjectUtils.equal(this.mPay,other.mPay)
	            && ObjectUtils.equal(this.mThirdParty, other.mThirdParty)
	            && ObjectUtils.equal(this.mView,other.mView);
	}
	
	@Override
	public int hashCode() {
	    return ObjectUtils.hashCode(this.mAccountId,
	            this.mDeposit,
	            this.mId,
	            this.mOwnTransferFrom,
	            this.mOwnTransferTo,
	            this.mPay,
	            this.mThirdParty,
	            this.mView);
	}
	
}
