package com.liato.bankdroid.repository.entities.accounts;

import com.liato.bankdroid.repository.entities.TransactionEntity;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

public class TransactionAccountEntity extends AccountEntity {

    private Collection<TransactionEntity> mTransactions;

    private TransactionAccountEntity(
            String id,
            long connectionId,
            String name,
            BigDecimal balance,
            String currency,
            boolean hidden,
            boolean notificationsEnabled,
            Collection<TransactionEntity> transactions
    ) {
        super(
                id,
                connectionId,
                AccountTypeEntity.TRANSACTION,
                name,
                balance,
                currency,
                hidden,
                notificationsEnabled
        );
        mTransactions = transactions;
    }

    public Iterator<TransactionEntity> getTransactions() {
        return mTransactions.iterator();
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

        public TransactionAccountEntity build() {
            return new TransactionAccountEntity(
                    mId,
                    mConnectionId,
                    mName,
                    mBalance,
                    mCurrency,
                    mHidden,
                    mNotificationsEnabled,
                    mTransactions == null ? Collections.<TransactionEntity>emptyList() : mTransactions
            );
        }
    }
}
