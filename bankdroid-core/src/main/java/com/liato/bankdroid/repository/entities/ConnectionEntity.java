package com.liato.bankdroid.repository.entities;

import com.liato.bankdroid.repository.entities.accounts.AccountEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ConnectionEntity {
    public static final long DEFAULT_ID  = -1;

    private long mId;
    private String mProviderId;
    private String mName;
    private boolean mEnabled;
    private BigDecimal mBalance;
    private String mCurrency;
    private Map<String,String> mProperties;

    private Collection<AccountEntity> mAccounts;

    private ConnectionEntity(
            long id,
            String providerId,
            String name,
            boolean enabled,
            BigDecimal balance,
            String currency,
            Map<String, String> properties,
            Collection<AccountEntity> accounts) {
        mId = id;
        mProviderId = providerId;
        mName = name;
        mEnabled = enabled;
        mBalance = balance;
        mCurrency = currency;
        mProperties = properties;
        mAccounts = accounts;
    }


    public long getId() {
        return mId;
    }

    public String getProviderId() {
        return mProviderId;
    }

    public String getName() {
        return mName;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public BigDecimal getBalance() {
        return mBalance;
    }

    public String getCurrency() {
        return mCurrency;
    }

    public Map<String, String> getProperties() {
        return mProperties;
    }

    public Collection<AccountEntity> getAccounts() {
        return mAccounts;
    }

    public static Builder builder(long id, String providerId) {
        return new Builder(id, providerId);
    }

    public static class Builder {

        private long mId;
        private String mProviderId;
        private String mName;
        private boolean mEnabled;
        private BigDecimal mBalance;
        private String mCurrency;
        private Map<String,String> mProperties;

        private Collection<AccountEntity> mAccounts;

        public Builder(long id, String providerId) {
            mId = id;
            mProviderId = providerId;
        }

        public Builder name(String name) {
            mName = name;
            return this;
        }

        public Builder enabled(boolean enabled) {
            mEnabled = enabled;
            return this;
        }

        public Builder balance(BigDecimal balance) {
            mBalance = balance;
            return this;
        }

        public Builder currency(String currency) {
            mCurrency = currency;
            return this;
        }

        public Builder properties(Map<String, String> properties) {
            mProperties = properties;
            return this;
        }

        public Builder property(String key, String value) {
            if(mProperties == null) {
                mProperties = new HashMap<>();
            }
            mProperties.put(key, value);
            return this;
        }

        public Builder accounts(Collection<AccountEntity> accounts) {
            mAccounts = accounts;
            return this;
        }

        public Builder account(AccountEntity account) {
            if(mAccounts == null) {
                mAccounts = new LinkedList<>();
            }
            mAccounts.add(account);
            return this;
        }

        public ConnectionEntity build() {
            return new ConnectionEntity(
                    mId,
                    mProviderId,
                    mName,
                    mEnabled,
                    mBalance,
                    mCurrency,
                    mProperties == null ? Collections.<String,String>emptyMap() : mProperties,
                    mAccounts == null ? Collections.<AccountEntity>emptyList() : mAccounts
            );
        }
    }
}
