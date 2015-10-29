package com.liato.bankdroid.api.domain.account.impl;

import com.liato.bankdroid.api.domain.account.Transaction;
import com.liato.bankdroid.api.domain.account.TransactionAccount;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static com.liato.bankdroid.api.domain.account.impl.AccountBuilder.BasicAccount;

public class TransactionAccountBuilder extends AbstractAccountBuilder<TransactionAccountBuilder> {

    private Collection<Transaction> mTransactions;

    public TransactionAccountBuilder(String id, String name, String currency) {
        super(id, name, currency);
    }

    protected TransactionAccountBuilder self() {
        return this;
    }

    public TransactionAccountBuilder addTransaction(Transaction transaction) {
        if(mTransactions == null) {
            mTransactions = new ArrayList<>();
        }
        mTransactions.add(transaction);
        return this;
    }

    public TransactionAccountBuilder transactions(Collection<Transaction> transactions) {
        mTransactions = transactions;
        return this;
    }

    public TransactionAccount build() {
        return new BasicTransactionAccount(mId, mName, mCurrency, mBalance, mCustomAttributes,
                mTransactions);
    }

    private class BasicTransactionAccount extends BasicAccount implements TransactionAccount {

        private Collection<Transaction> mTransactions;

        private BasicTransactionAccount(String id, String name, String currency, BigDecimal balance,
                Map<String, String> customAttributes, Collection<Transaction> transactions) {
            super(id, name, currency, balance, customAttributes);
            mTransactions = transactions;
        }

        @Override
        public Collection<Transaction> getTransactions() {
            return mTransactions == null ? Collections.<Transaction>emptyList() : mTransactions;
        }
    }
}
