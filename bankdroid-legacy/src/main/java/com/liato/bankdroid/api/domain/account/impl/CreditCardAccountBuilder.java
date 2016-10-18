package com.liato.bankdroid.api.domain.account.impl;

import com.liato.bankdroid.api.domain.account.CreditCardAccount;
import com.liato.bankdroid.api.domain.account.Transaction;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static com.liato.bankdroid.api.domain.account.impl.AccountBuilder.BasicAccount;

public class CreditCardAccountBuilder extends AbstractAccountBuilder<CreditCardAccountBuilder>{

    private BigDecimal mCreditLimit;

    private Collection<Transaction> mTransactions;

    public CreditCardAccountBuilder(String id, String name, String currency) {
       super(id, name, currency);
    }

    protected CreditCardAccountBuilder self() {
        return this;
    }

    public CreditCardAccountBuilder creditLimit(BigDecimal creditLimit) {
        mCreditLimit = creditLimit;
        return this;
    }

    public CreditCardAccountBuilder transactions(Collection<Transaction> transactions) {
        mTransactions = transactions;
        return this;
    }

    public CreditCardAccount build()   {
        return new BasicCreditCardAccount(mId, mName, mCurrency, mBalance, mCustomAttributes,
                mCreditLimit, mTransactions);
    }

    private class BasicCreditCardAccount extends BasicAccount implements CreditCardAccount {

        private BigDecimal mCreditLimit;
        private Collection<Transaction> mTransactions;

        BasicCreditCardAccount(String id, String name, String currency, BigDecimal balance,
                Map<String, String> customAttributes, BigDecimal creditLimit,
                Collection<Transaction> transactions) {
            super(id, name, currency, balance, customAttributes);
            mCreditLimit = creditLimit;
            mTransactions = transactions;
        }

        @Override
        public BigDecimal getCreditLimit() {
            return mCreditLimit == null ? BigDecimal.ZERO : mCreditLimit;
        }

        public Collection<Transaction> getTransactions() {
            return mTransactions == null ? Collections.<Transaction>emptyList() : mTransactions;
        }
    }
}
