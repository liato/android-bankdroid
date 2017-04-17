package com.liato.bankdroid.repository.entities.accounts;

import com.liato.bankdroid.repository.entities.TransactionEntity;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class CreditCardEntity extends AccountEntity {

    private BigDecimal mCreditLimit;
    private Collection<TransactionEntity> mTransactions;

    private CreditCardEntity(
            String id,
            long connectionId,
            String name,
            BigDecimal balance,
            String currency,
            boolean hidden,
            boolean notificationsEnabled,
            BigDecimal creditLimit,
            Collection<TransactionEntity> transactions) {
        super(
                id,
                connectionId,
                AccountTypeEntity.CREDIT_CARD,
                name,
                balance,
                currency,
                hidden,
                notificationsEnabled
        );
        mCreditLimit = creditLimit;
        mTransactions = transactions;
    }

    public static Builder builder(String id, long connectionId, String name) {
        return new Builder(id, connectionId, name);
    }

    public static class Builder {

        private String mId;

        private long mConnectionId;

        private String mName;

        private BigDecimal mBalance;

        private String mCurrency;

        private boolean mHidden;

        private boolean mNotificationsEnabled;

        private BigDecimal mCreditLimit;

        private Collection<TransactionEntity> mTransactions;

        public Builder(String id, long connectionId, String name) {
            mId = id;
            mConnectionId = connectionId;
            mName = name;
        }

        public Builder balance(BigDecimal balance) {
            mBalance = balance;
            return this;
        }

        public Builder currency(String currency) {
            mCurrency = currency;
            return this;
        }

        public Builder hidden(boolean hidden) {
            mHidden = hidden;
            return this;
        }

        public Builder notifications(boolean notifications) {
            mNotificationsEnabled = notifications;
            return this;
        }

        public Builder creditLimit(BigDecimal creditLimit) {
            mCreditLimit = creditLimit;
            return this;
        }

        public Builder transactions(Collection<TransactionEntity> transactions) {
            mTransactions = transactions;
            return this;
        }

        public Builder transaction(TransactionEntity transaction) {
            if(mTransactions == null) {
                mTransactions = new LinkedList<>();
            }
            mTransactions.add(transaction);
            return this;
        }

        public CreditCardEntity build() {
            return new CreditCardEntity(
                    mId,
                    mConnectionId,
                    mName,
                    mBalance,
                    mCurrency,
                    mHidden,
                    mNotificationsEnabled,
                    mCreditLimit,
                    mTransactions == null ? Collections.<TransactionEntity>emptyList() : mTransactions
            );
        }
    }
}
