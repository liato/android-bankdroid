package com.liato.bankdroid.api.domain.account.impl;

import com.liato.bankdroid.api.domain.account.PrePaidCardAccount;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.Map;

import static com.liato.bankdroid.api.domain.account.impl.AccountBuilder.BasicAccount;

public class PrePaidCardAccountBuilder extends AbstractAccountBuilder<PrePaidCardAccountBuilder> {

    private DateTime mValidFrom;
    private DateTime mExpirationDate;

    public PrePaidCardAccountBuilder(String id, String name, String currency) {
        super(id, name, currency);
    }

    protected PrePaidCardAccountBuilder self() {
        return this;
    }

    public PrePaidCardAccountBuilder validFrom(DateTime validFrom) {
        mValidFrom = validFrom;
        return this;
    }
    public PrePaidCardAccountBuilder expirationDate(DateTime expirationDate) {
        mExpirationDate = expirationDate;
        return this;
    }

    public PrePaidCardAccount build() {
        return new BasicPrePaidCardAccount(mId, mName, mCurrency, mBalance, mCustomAttributes,
                mExpirationDate, mValidFrom);
    }

    private static class BasicPrePaidCardAccount extends BasicAccount implements PrePaidCardAccount {

        private DateTime mExpirationDate;
        private DateTime mValidFrom;

        private BasicPrePaidCardAccount(String id, String name, String currency, BigDecimal balance,
                Map<String, String> customAttributes, DateTime expirationDate, DateTime validFrom) {
            super(id, name, currency, balance, customAttributes);
            mExpirationDate = expirationDate;
            mValidFrom = validFrom;
        }

        @Override
        public DateTime getExpirationDate() {
            return mExpirationDate;
        }

        @Override
        public DateTime getValidFrom() {
            return mValidFrom;
        }
    }
}
