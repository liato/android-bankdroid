package com.liato.bankdroid.banking.banks.nordea.api.model.response;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.liato.bankdroid.banking.banks.nordea.api.model.NordeaTransaction;
import com.liato.bankdroid.util.ObjectUtils;

@JacksonXmlRootElement(localName = "getAccountTransactionsOut")
public class GetAccountTransactionsOut extends AbstractResponse {

    private String mAccountId;

    private int mContinueKey;

    private List<NordeaTransaction> mAccountTransactions;

    /**
     * @return the AccountId
     */
    @JacksonXmlProperty(localName = "accountId")
    public String getAccountId() {
        return mAccountId;
    }

    /**
     * @param pAccountId
     *            the AccountId to set
     */
    public void setAccountId(String pAccountId) {
        this.mAccountId = pAccountId;
    }

    /**
     * @return the ContinueKey
     */
    @JacksonXmlProperty(localName = "continueKey")
    public int getContinueKey() {
        return mContinueKey;
    }

    /**
     * @param pContinueKey
     *            the ContinueKey to set
     */
    public void setContinueKey(int pContinueKey) {
        this.mContinueKey = pContinueKey;
    }

    /**
     * @return the AccountTransactions
     */
    @JacksonXmlProperty(localName = "accountTransaction")
    public List<NordeaTransaction> getAccountTransactions() {
        return mAccountTransactions;
    }

    /**
     * @param pAccountTransactions
     *            the AccountTransactions to set
     */
    public void setAccountTransactions(List<NordeaTransaction> pAccountTransactions) {
        this.mAccountTransactions = pAccountTransactions;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        GetAccountTransactionsOut other = (GetAccountTransactionsOut) obj;
        return super.equals(obj) && ObjectUtils.equal(this.mAccountId, other.mAccountId)
                && ObjectUtils.equal(this.mAccountTransactions, other.mAccountTransactions)
                && ObjectUtils.equal(this.mContinueKey, other.mContinueKey);
    }

    @Override
    public int hashCode() {
        return super.hashCode() + ObjectUtils.hashCode(mAccountTransactions, mAccountTransactions,mContinueKey);
    }
}
