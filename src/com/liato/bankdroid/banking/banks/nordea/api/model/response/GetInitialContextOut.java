package com.liato.bankdroid.banking.banks.nordea.api.model.response;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.liato.bankdroid.banking.banks.nordea.api.model.NordeaAccount;
import com.liato.bankdroid.util.ObjectUtils;

@JacksonXmlRootElement(localName = "getInitialContextOut")
public class GetInitialContextOut extends AbstractResponse {

    private List<NordeaAccount> mAccounts;

    /**
     * @return the accounts
     */
    @JacksonXmlProperty(localName = "account")
    public List<NordeaAccount> getAccounts() {
        return mAccounts;
    }

    /**
     * @param accounts
     *            the accounts to set
     */
    public void setAccounts(List<NordeaAccount> pAccounts) {
        this.mAccounts = pAccounts;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        GetInitialContextOut other = (GetInitialContextOut) obj;
        return super.equals(obj) && ObjectUtils.equal(this.mAccounts, other.mAccounts);
    }

    @Override
    public int hashCode() {
        return super.hashCode() + ObjectUtils.hashCode(this.mAccounts);
    }
}
