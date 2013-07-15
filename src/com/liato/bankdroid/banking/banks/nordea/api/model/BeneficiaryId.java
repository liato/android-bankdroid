package com.liato.bankdroid.banking.banks.nordea.api.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.liato.bankdroid.util.ObjectUtils;

public class BeneficiaryId {

    private String mId;

    private String mBeneficiaryId;

    /**
     * @return the Id
     */
    @JacksonXmlProperty(localName = "id", isAttribute = true)
    public String getId() {
        return mId;
    }

    /**
     * @param pId
     *            the Id to set
     */
    public void setId(String pId) {
        this.mId = pId;
    }

    /**
     * @return the BeneficiaryId
     */
    @JacksonXmlProperty(localName = "$")
    public String getBeneficiaryId() {
        return mBeneficiaryId;
    }

    /**
     * @param pBeneficiaryId
     *            the BeneficiaryId to set
     */
    public void setBeneficiaryId(String pBeneficiaryId) {
        this.mBeneficiaryId = pBeneficiaryId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BeneficiaryId other = (BeneficiaryId) obj;
        return ObjectUtils.equal(this.mBeneficiaryId, other.mBeneficiaryId) && ObjectUtils.equal(this.mId, other.mId);
    }

    @Override
    public int hashCode() {
        return ObjectUtils.hashCode(this.mBeneficiaryId, this.mId);
    }
}
