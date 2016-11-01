package com.liato.bankdroid.api.domain.account.impl;

import com.liato.bankdroid.api.domain.account.Account;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

public class AccountBuilder extends AbstractAccountBuilder<AccountBuilder> {

    public AccountBuilder(String id, String name, String currency) {
       super(id, name, currency);
    }

    protected AccountBuilder self() {
        return this;
    }

    public Account build() {
        return new BasicAccount(mId, mName, mCurrency, mBalance, mCustomAttributes);
    }

    static class BasicAccount implements Account {

        private String mId;
        private String mName;
        private BigDecimal mBalance;
        private String mCurrency;
        private Map<String, String> mCustomAttributes;

        protected BasicAccount(String id, String name, String currency, BigDecimal balance, Map<String, String> customAttributes) {
            mId = id;
            mCurrency = currency;
            mName = name;
            mBalance = balance;
            mCustomAttributes = customAttributes;
        }

        @Override
        public String getId() {
            return mId;
        }

        @Override
        public String getName() {
            return mName;
        }

        @Override
        public BigDecimal getBalance() {
            return mBalance == null ? BigDecimal.ZERO : mBalance;
        }

        @Override
        public String getCurrency() {
            return mCurrency;
        }

        @Override
        public Map<String, String> getCustomAttributes() {
            return mCustomAttributes == null ? Collections.<String, String>emptyMap() : mCustomAttributes;
        }
    }
}
