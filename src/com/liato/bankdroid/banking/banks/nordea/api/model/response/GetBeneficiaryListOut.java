package com.liato.bankdroid.banking.banks.nordea.api.model.response;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.liato.bankdroid.banking.banks.nordea.api.model.Beneficiary;
import com.liato.bankdroid.util.ObjectUtils;

@JacksonXmlRootElement(localName = "getBeneficiaryListOut")
public class GetBeneficiaryListOut extends AbstractResponse {

    private List<Beneficiary> mBeneficiaries;

    /**
     * @return the beneficiary list
     */
    @JacksonXmlProperty(localName = "beneficiary")
    public List<Beneficiary> getmBeneficiaries() {
        return mBeneficiaries;
    }

    /**
     * @param pBeneficiary
     *            the beneficiary to set
     */
    public void setBeneficiary(List<Beneficiary> pBeneficiaries) {
        this.mBeneficiaries = pBeneficiaries;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        GetBeneficiaryListOut other = (GetBeneficiaryListOut) obj;
        return super.equals(obj) && ObjectUtils.equal(this.mBeneficiaries, other.mBeneficiaries);
    }
    
    @Override
    public int hashCode() {
        return super.hashCode() + ObjectUtils.hashCode(mBeneficiaries);
    }
}
