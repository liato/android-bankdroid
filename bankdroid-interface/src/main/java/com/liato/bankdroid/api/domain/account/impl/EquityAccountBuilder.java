package com.liato.bankdroid.api.domain.account.impl;

import com.liato.bankdroid.api.domain.account.Equity;
import com.liato.bankdroid.api.domain.account.EquityAccount;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static com.liato.bankdroid.api.domain.account.impl.AccountBuilder.BasicAccount;

public class EquityAccountBuilder extends AbstractAccountBuilder<EquityAccountBuilder> {

    private BigDecimal mCost;

    private BigDecimal mRevenue;

    private Collection<Equity> mEquities;

    public EquityAccountBuilder(String id, String name, String currency) {
        super(id, name, currency);
    }

    protected EquityAccountBuilder self() {
        return this;
    }

    public EquityAccountBuilder cost(BigDecimal cost) {
        mCost = cost;
        return this;
    }

    public EquityAccountBuilder revenue(BigDecimal revenue) {
        mRevenue = revenue;
        return this;
    }

    public EquityAccountBuilder addEquity(Equity equity) {
        if (mEquities == null) {
            mEquities = new ArrayList<>();
        }
        mEquities.add(equity);
        return this;
    }

    public EquityAccountBuilder equities(Collection<Equity> equities) {
        mEquities = equities;
        return this;
    }

    public EquityAccount build() {
        return new BasicEquityAccount(mId, mName, mCurrency, mBalance, mCustomAttributes, mCost,
                mRevenue, mEquities);
    }

    private class BasicEquityAccount extends BasicAccount implements EquityAccount {

        private BigDecimal mCost;
        private BigDecimal mRevenue;
        private Collection<Equity> mEquities;

        private BasicEquityAccount(String id, String name, String currency, BigDecimal balance,
                Map<String, String> customAttributes, BigDecimal cost, BigDecimal revenue,
                Collection<Equity> equities) {
            super(id, name, currency, balance, customAttributes);
            mCost = cost;
            mRevenue = revenue;
            mEquities = equities;
        }

        @Override
        public BigDecimal getBalance() {
            //TODO calculate balance from getCost() and getRevenue()
            return super.getBalance();
        }

        @Override
        public BigDecimal getCost() {
            //TODO calculate cost by summarizing cost of equities collection.
            return mCost == null ? BigDecimal.ZERO : mCost;
        }

        @Override
        public BigDecimal getRevenue() {
            //TODO calculate revenue by summarizing revenue of equities collection.
            return mRevenue == null ? BigDecimal.ZERO : mRevenue;
        }

        @Override
        public double getRevenueAsPercentage() {
            return 1 + getRevenue().divide(getCost()).doubleValue();
        }

        @Override
        public Collection<Equity> getEquities() {
            return mEquities == null ? Collections.<Equity>emptyList() : mEquities;
        }
    }
}
