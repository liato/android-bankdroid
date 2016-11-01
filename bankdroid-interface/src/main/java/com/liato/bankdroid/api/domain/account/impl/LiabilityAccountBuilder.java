package com.liato.bankdroid.api.domain.account.impl;

import com.liato.bankdroid.api.domain.account.LiabilityAccount;
import com.liato.bankdroid.api.domain.account.Payment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static com.liato.bankdroid.api.domain.account.impl.AccountBuilder.BasicAccount;

public class LiabilityAccountBuilder extends AbstractAccountBuilder<LiabilityAccountBuilder> {

    private double mInterest;
    private Collection<Payment> mPayments;

    public LiabilityAccountBuilder(String id, String name, String currency) {
        super(id, name, currency);
    }

    protected LiabilityAccountBuilder self() {
        return this;
    }

    public LiabilityAccountBuilder interest(double interest) {
        mInterest = interest;
        return this;
    }

    public LiabilityAccountBuilder addPayment(Payment payment) {
        if (mPayments == null) {
            mPayments = new ArrayList<>();
        }
        mPayments.add(payment);
        return this;
    }

    public LiabilityAccountBuilder payments(Collection<Payment> payments) {
        mPayments = payments;
        return this;
    }

    public LiabilityAccount build() {
        return new BasicLiabilityAccount(mId, mName, mCurrency, mBalance, mCustomAttributes,
                mInterest, mPayments);
    }

    private static class BasicLiabilityAccount extends BasicAccount implements LiabilityAccount {

        private double mInterest;
        private Collection<Payment> mPayments;

        private BasicLiabilityAccount(String id, String name, String currency, BigDecimal balance,
                Map<String, String> customAttributes, double interest, Collection<Payment> payments) {
            super(id, name, currency, balance, customAttributes);
            mInterest = interest;
            mPayments = payments;
        }

        @Override
        public double getInterest() {
            return mInterest;
        }

        @Override
        public Collection<Payment> getPayments() {
            return mPayments == null ? Collections.<Payment>emptyList() : mPayments;
        }
    }
}
