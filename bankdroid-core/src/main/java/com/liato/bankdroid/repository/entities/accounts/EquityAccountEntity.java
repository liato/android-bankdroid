package com.liato.bankdroid.repository.entities.accounts;


import com.liato.bankdroid.repository.entities.EquityEntity;
import com.liato.bankdroid.repository.entities.TransactionEntity;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

public class EquityAccountEntity extends AccountEntity {
    Collection<EquityEntity> mEquities;

    private EquityAccountEntity(
            String id,
            long connectionId,
            String name,
            BigDecimal balance,
            String currency,
            boolean hidden,
            boolean notificationsEnabled,
            Collection<EquityEntity> equities
    ) {
        super(
                id,
                connectionId,
                AccountTypeEntity.EQUITY,
                name,
                balance,
                currency,
                hidden,
                notificationsEnabled
        );
        mEquities = equities;
    }

    public Iterator<EquityEntity> getEquities() {
        return mEquities.iterator();
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

        private Collection<EquityEntity> mEquities;

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

        public Builder equities(Collection<EquityEntity> equities) {
            mEquities = equities;
            return this;
        }

        public Builder equity(EquityEntity equity) {
            if(mEquities == null) {
                mEquities = new LinkedList<>();
            }
            mEquities.add(equity);
            return this;
        }

        public EquityAccountEntity build() {
            return new EquityAccountEntity(
                    mId,
                    mConnectionId,
                    mName,
                    mBalance,
                    mCurrency,
                    mHidden,
                    mNotificationsEnabled,
                    mEquities == null ? Collections.<EquityEntity>emptyList() : mEquities
            );
        }
    }

}
