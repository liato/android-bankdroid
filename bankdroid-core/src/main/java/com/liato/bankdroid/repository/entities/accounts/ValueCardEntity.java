package com.liato.bankdroid.repository.entities.accounts;

import com.liato.bankdroid.repository.entities.TransactionEntity;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.Date;

public class ValueCardEntity extends AccountEntity {

    private DateTime mExpirationDate;

    private DateTime mValidFrom;

    private ValueCardEntity(String id,
            long connectionId,
            String name,
            BigDecimal balance,
            String currency,
            boolean hidden,
            boolean notificationsEnabled,
            DateTime expirationDate,
            DateTime validFrom) {
        super(
                id,
                connectionId,
                AccountTypeEntity.VALUE_CARD,
                name,
                balance,
                currency,
                hidden,
                notificationsEnabled
        );
        mExpirationDate = expirationDate;
        mValidFrom = validFrom;
    }

    public DateTime getExpirationDate() {
        return mExpirationDate;
    }

    public DateTime getValidFrom() {
        return mValidFrom;
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

        private DateTime mExpirationDate;

        private DateTime mValidFrom;

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

        public Builder expirationDate(DateTime expirationDate) {
            mExpirationDate = expirationDate;
            return this;
        }

        public Builder validFrom(DateTime validFrom) {
            mValidFrom = validFrom;
            return this;
        }

        public ValueCardEntity build() {
            return new ValueCardEntity(
                    mId,
                    mConnectionId,
                    mName,
                    mBalance,
                    mCurrency,
                    mHidden,
                    mNotificationsEnabled,
                    mExpirationDate,
                    mValidFrom
            );
        }
    }
}
