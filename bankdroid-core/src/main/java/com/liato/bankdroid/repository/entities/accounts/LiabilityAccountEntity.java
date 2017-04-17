package com.liato.bankdroid.repository.entities.accounts;

import java.math.BigDecimal;

public class LiabilityAccountEntity extends AccountEntity {
    private double mInterest;

    private LiabilityAccountEntity(
            String id,
            long connectionId,
            String name,
            BigDecimal balance,
            String currency,
            boolean hidden,
            boolean notificationsEnabled,
            double interest
    ) {
        super(
                id,
                connectionId,
                AccountTypeEntity.LIABILITY,
                name,
                balance,
                currency,
                hidden,
                notificationsEnabled
        );
        mInterest = interest;
    }

    public double getInterest() {
        return mInterest;
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

        private double mInterest;

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

        public Builder interest(double interest) {
            mInterest = interest;
            return this;
        }

        public LiabilityAccountEntity build() {
            return new LiabilityAccountEntity(
                    mId,
                    mConnectionId,
                    mName,
                    mBalance,
                    mCurrency,
                    mHidden,
                    mNotificationsEnabled,
                    mInterest
            );
        }
    }
}
