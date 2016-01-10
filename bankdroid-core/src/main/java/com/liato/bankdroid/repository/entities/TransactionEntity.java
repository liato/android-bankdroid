package com.liato.bankdroid.repository.entities;

import org.joda.time.DateTime;

import java.math.BigDecimal;

public class TransactionEntity {
    private String mId;
    private long mConnectionId;
    private String mAccountId;
    private String mDescription;
    private DateTime mTransactionDate;
    private BigDecimal mAmount;
    private String mCurrency;
    private boolean mPending;

    private TransactionEntity(
            String id,
            long connectionId,
            String accountId,
            String description,
            DateTime transactionDate,
            BigDecimal amount,
            String currency,
            boolean pending) {
        mId = id;
        mConnectionId = connectionId;
        mAccountId = accountId;
        mDescription = description;
        mTransactionDate = transactionDate;
        mAmount = amount;
        mCurrency = currency;
        mPending = pending;
    }

    public String getId() {
        return mId;
    }

    public long getConnectionId() {
        return mConnectionId;
    }

    public String getAccountId() {
        return mAccountId;
    }

    public String getDescription() {
        return mDescription;
    }

    public DateTime getTransactionDate() {
        return mTransactionDate;
    }

    public BigDecimal getAmount() {
        return mAmount;
    }

    public String getCurrency() {
        return mCurrency;
    }

    public boolean isPending() {
        return mPending;
    }

    public static Builder builder(String id, long connectionId, String accountId) {
        return new Builder(id, connectionId, accountId);
    }

    public static class Builder {
        private String mId;
        private long mConnectionId;
        private String mAccountId;
        private String mDescription;
        private DateTime mTransactionDate;
        private BigDecimal mAmount;
        private String mCurrency;
        private boolean mPending;

        public Builder(String id, long connectionId, String accountId) {
            mId = id;
            mConnectionId = connectionId;
            mAccountId = accountId;
        }

        public Builder description(String description) {
            mDescription = description;
            return this;
        }

        public Builder date(DateTime dateTime) {
            mTransactionDate = dateTime;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            mAmount = amount;
            return this;
        }

        public Builder currency(String currency) {
            mCurrency = currency;
            return this;
        }

        public Builder pending(boolean pending) {
            mPending = pending;
            return this;
        }

        public TransactionEntity build() {
            return new TransactionEntity(
                    mId,
                    mConnectionId,
                    mAccountId,
                    mDescription,
                    mTransactionDate,
                    mAmount,
                    mCurrency,
                    mPending
            );
        }


    }
}
