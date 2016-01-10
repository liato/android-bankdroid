package com.liato.bankdroid.repository.entities.accounts;

import java.math.BigDecimal;

public abstract class AccountEntity {
    private final String mId;
    private long mConnectionId;

    private final AccountTypeEntity mAccountType;

    private String mName;
    private BigDecimal mBalance;
    private String mCurrency;
    private boolean mHidden;
    private boolean mNotificationsEnabled;

    protected AccountEntity(
            String id,
            long connectionId,
            AccountTypeEntity accountType,
            String name,
            BigDecimal balance,
            String currency,
            boolean hidden,
            boolean notificationsEnabled) {
        mId = id;
        mConnectionId = connectionId;
        mAccountType = accountType;
        mName = name;
        mBalance = balance;
        mCurrency = currency;
        mHidden = hidden;
        mNotificationsEnabled = notificationsEnabled;
    }

    public String getId() {
        return mId;
    }

    public long getConnectionId() {
        return mConnectionId;
    }

    public AccountTypeEntity getAccountType() {
        return mAccountType;
    }

    public String getName() {
        return mName;
    }

    public BigDecimal getBalance() {
        return mBalance;
    }

    public String getCurrency() {
        return mCurrency;
    }

    public boolean isHidden() {
        return mHidden;
    }

    public boolean isNotificationsEnabled() {
        return mNotificationsEnabled;
    }
}
