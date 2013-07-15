package com.liato.bankdroid.banking.banks.nordea.api.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.liato.bankdroid.util.ObjectUtils;


public class Beneficiary {

	private BeneficiaryId mBeneficiaryId;
	
	private String mNickname;
	
	private String mBeneficiaryBankId;
	
	private String mPaymentSubType;
	
	private String mPaymentSubtypeExtension;
	
	private String mPaymentType;
	
	private long mToAccountId;
	
	private String mCategory;
	
	private String mBranchId;
	
	private String mGiroNumber;
	
	private String mInvoicePaymentType;
	
	private String mInvoicePaymentId;
	
	/**
	 * @return the BeneficiaryId
	 */
	@JacksonXmlProperty(localName="beneficiaryId")
	public BeneficiaryId getBeneficiaryId() {
		return mBeneficiaryId;
	}

	/**
	 * @param pBeneficiaryId the BeneficiaryId to set
	 */
	public void setBeneficiaryId(BeneficiaryId pBeneficiaryId) {
		this.mBeneficiaryId = pBeneficiaryId;
	}

	/**
	 * @return the mNickname
	 */
	@JacksonXmlProperty(localName="beneficiaryNickName")
	public String getNickname() {
		return mNickname;
	}

	/**
	 * @param pNickname the Nickname to set
	 */
	public void setNickname(String pNickname) {
		this.mNickname = pNickname;
	}

	/**
	 * @return the BeneficiaryBankId
	 */
	@JacksonXmlProperty(localName="beneficiaryBankId")
	public String getBeneficiaryBankId() {
		return mBeneficiaryBankId;
	}

	/**
	 * @param pBeneficiaryBankId the BeneficiaryBankId to set
	 */
	public void setBeneficiaryBankId(String pBeneficiaryBankId) {
		this.mBeneficiaryBankId = pBeneficiaryBankId;
	}

	/**
	 * @return the PaymentSubType
	 */
	@JacksonXmlProperty(localName="paymentSubType")
	public String getPaymentSubType() {
		return mPaymentSubType;
	}

	/**
	 * @param pPaymentSubType the PaymentSubType to set
	 */
	public void setPaymentSubType(String pPaymentSubType) {
		this.mPaymentSubType = pPaymentSubType;
	}

	/**
	 * @return the PaymentSubtypeExtension
	 */
	@JacksonXmlProperty(localName="paymentSubtypeExtension")
	public String getPaymentSubtypeExtension() {
		return mPaymentSubtypeExtension;
	}

	/**
	 * @param pPaymentSubtypeExtension the PaymentSubtypeExtension to set
	 */
	public void setPaymentSubtypeExtension(String pPaymentSubtypeExtension) {
		this.mPaymentSubtypeExtension = pPaymentSubtypeExtension;
	}

	/**
	 * @return the PaymentType
	 */
	@JacksonXmlProperty(localName="paymentType")
	public String getPaymentType() {
		return mPaymentType;
	}

	/**
	 * @param pPaymentType the PaymentType to set
	 */
	public void setPaymentType(String pPaymentType) {
		this.mPaymentType = pPaymentType;
	}

	/**
	 * @return the ToAccountId
	 */
	@JacksonXmlProperty(localName="toAccountId")
	public long getToAccountId() {
		return mToAccountId;
	}

	/**
	 * @param pToAccountId the ToAccountId to set
	 */
	public void setToAccountId(long pToAccountId) {
		this.mToAccountId = pToAccountId;
	}

	/**
	 * @return the Category
	 */
	@JacksonXmlProperty(localName="category")
	public String getCategory() {
		return mCategory;
	}

	/**
	 * @param pCategory the Category to set
	 */
	public void setCategory(String pCategory) {
		this.mCategory = pCategory;
	}

	/**
	 * @return the BranchId
	 */
	@JacksonXmlProperty(localName="branchId")
	public String getBranchId() {
		return mBranchId;
	}

	/**
	 * @param pBranchId the BranchId to set
	 */
	public void setBranchId(String pBranchId) {
		this.mBranchId = pBranchId;
	}

	/**
	 * @return the GiroNumber
	 */
	@JacksonXmlProperty(localName="giroNumber")
	public String getGiroNumber() {
		return mGiroNumber;
	}

	/**
	 * @param pGiroNumber the GiroNumber to set
	 */
	public void setGiroNumber(String pGiroNumber) {
		this.mGiroNumber = pGiroNumber;
	}

	/**
	 * @return the InvoicePaymentType
	 */
	@JacksonXmlProperty(localName="invoicePaymentType")
	public String getInvoicePaymentType() {
		return mInvoicePaymentType;
	}

	/**
	 * @param pInvoicePaymentType the InvoicePaymentType to set
	 */
	public void setInvoicePaymentType(String pInvoicePaymentType) {
		this.mInvoicePaymentType = pInvoicePaymentType;
	}

	/**
	 * @return the InvoicePaymentId
	 */
	@JacksonXmlProperty(localName="invoicePaymentId")
	public String getInvoicePaymentId() {
		return mInvoicePaymentId;
	}

	/**
	 * @param pInvoicePaymentId the InvoicePaymentId to set
	 */
	public void setInvoicePaymentId(String pInvoicePaymentId) {
		this.mInvoicePaymentId = pInvoicePaymentId;
	}
	
	
@Override
public boolean equals(Object obj) {
    if(obj == null) {
        return false;
    }
    if(getClass() != obj.getClass()) {
        return false;
    }
    Beneficiary other = (Beneficiary) obj;
    return ObjectUtils.equal(this.mBeneficiaryBankId, other.mBeneficiaryBankId)
            && ObjectUtils.equal(this.mBeneficiaryId, other.mBeneficiaryId)
            && ObjectUtils.equal(this.mBranchId, other.mBranchId)
            && ObjectUtils.equal(this.mCategory, other.mCategory)
            && ObjectUtils.equal(this.mGiroNumber, other.mGiroNumber)
            && ObjectUtils.equal(this.mInvoicePaymentId, other.mInvoicePaymentId)
            && ObjectUtils.equal(this.mInvoicePaymentType, other.mInvoicePaymentType)
            && ObjectUtils.equal(this.mNickname, other.mNickname)
            && ObjectUtils.equal(this.mPaymentSubType, other.mPaymentSubType)
            && ObjectUtils.equal(this.mPaymentSubtypeExtension, other.mPaymentSubtypeExtension)
            && ObjectUtils.equal(this.mPaymentType, other.mPaymentType)
            && ObjectUtils.equal(this.mToAccountId, other.mToAccountId);
    }
    public int hashCode() {
        return ObjectUtils.hashCode(this.mBeneficiaryBankId,
                this.mBeneficiaryId,
                this.mBranchId,
                this.mCategory,
                this.mGiroNumber,
                this.mInvoicePaymentId,
                this.mInvoicePaymentType,
                this.mNickname,
                this.mPaymentSubType,
                this.mPaymentSubtypeExtension,
                this.mPaymentType,
                this.mToAccountId);
    }
}
