package com.liato.bankdroid.api.domain.account.impl;


import com.liato.bankdroid.api.domain.account.Transaction;

import org.joda.time.DateTime;

import java.math.BigDecimal;

public class TransactionBuilder {

    private BasicTransaction mTransaction;

    public TransactionBuilder(BigDecimal amount, String currency, DateTime transactionDate) {
        this.mTransaction = new BasicTransaction(amount, currency, transactionDate);
    }

    public TransactionBuilder description(String description) {
        mTransaction.description = description;
        return this;
    }

    public TransactionBuilder pending(boolean pending) {
        mTransaction.pending = pending;
        return this;
    }

    public Transaction build() {
        return mTransaction;
    }

    private class BasicTransaction implements Transaction {

        private BigDecimal amount;
        private String description;
        private String currency;
        private DateTime transactionDate;
        private boolean pending;

        private BasicTransaction(BigDecimal amount, String currency, DateTime transactionDate) {
            this.amount = amount;
            this.currency = currency;
            this.transactionDate = transactionDate;
        }

        @Override
        public BigDecimal getAmount() {
            return amount;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getCurrency() {
            return currency;
        }

        @Override
        public DateTime getTransactionDate() {
            return transactionDate;
        }

        @Override
        public boolean isPending() {
            return pending;
        }
    }
}
