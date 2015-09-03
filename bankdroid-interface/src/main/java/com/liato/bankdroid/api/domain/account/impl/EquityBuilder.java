package com.liato.bankdroid.api.domain.account.impl;

import com.liato.bankdroid.api.domain.account.Equity;
import java.math.BigDecimal;

public class EquityBuilder {

    private BasicEquity mEquity;

    public EquityBuilder(BigDecimal cost, BigDecimal revenue, String currency) {
        mEquity = new BasicEquity(cost, revenue, currency);
    }

    /**
     *
     * @param balance Current balance of the equity.
     * @param decimalRevenue The revenue in percentage as a decimal.
     * @param currency The currency of the equity.
     */
    public EquityBuilder(BigDecimal balance, double decimalRevenue, String currency){
        mEquity = new BasicEquity(costFromBalanceAndRevenue(balance, decimalRevenue),
                revenueFromBalanceAndPercentageRevenue(balance, decimalRevenue),currency);
    }

    public EquityBuilder name(String name) {
        mEquity.mName = name;
        return this;
    }

    public EquityBuilder quantity(double quantity) {
        mEquity.mQuantity = quantity;
        return this;
    }

    private BigDecimal costFromBalanceAndRevenue(BigDecimal balance, double revenue) {
        // TODO Implementation
        return null;
    }

    private BigDecimal revenueFromBalanceAndPercentageRevenue(BigDecimal balance, double revenue) {
        //TODO implementation
        return null;
    }


    private class BasicEquity implements Equity {

        private String mName;

        private String mCurrency;

        private double mQuantity;

        private BigDecimal mCost;

        private BigDecimal mRevenue;

        private BasicEquity(BigDecimal cost, BigDecimal revenue, String currency) {
            mCost = cost;
            mRevenue = revenue;
            mCurrency = currency;
        }

        @Override
        public String getName() {
            return mName;
        }

        @Override
        public String getCurrency() {
            return mCurrency;
        }

        @Override
        public double getQuantity() {
            return mQuantity;
        }

        @Override
        public BigDecimal getCost() {
            return mCost == null ? BigDecimal.ZERO : mCost;
        }

        @Override
        public BigDecimal getRevenue() {
            return mRevenue == null ? BigDecimal.ZERO : mRevenue;
        }

        @Override
        public double getRevenueInPercent() {
            return getRevenue().equals(BigDecimal.ZERO) ? 0 : getCost().doubleValue() / getRevenue().doubleValue();
        }

        @Override
        public BigDecimal getBalance() {
            return getCost().add(getRevenue());
        }
    }
}
