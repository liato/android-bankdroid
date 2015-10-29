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
     * @param revenue The revenue in percentage. A value less than 1 is a loss
     *                       and a value greater than one is a profit. e.g. {@code 0.75} represents
     *                       a 25 % loss, while {@code 1.5} is 50 % profit.
     * @param currency The currency of the equity.
     */
    public EquityBuilder(BigDecimal balance, double revenue, String currency){
        mEquity = new BasicEquity(costFromBalanceAndRevenue(balance, revenue),
                revenueFromBalanceAndRevenueAsPerecntage(balance, revenue),currency);
    }

    public EquityBuilder name(String name) {
        mEquity.mName = name;
        return this;
    }

    public EquityBuilder quantity(double quantity) {
        mEquity.mQuantity = quantity;
        return this;
    }

    public Equity build() {
        return mEquity;
    }

    private BigDecimal costFromBalanceAndRevenue(BigDecimal balance, double revenue) {
        return balance.divide(BigDecimal.valueOf(revenue));
    }

    private BigDecimal revenueFromBalanceAndRevenueAsPerecntage(BigDecimal balance, double revenue) {
       return balance.subtract(balance.divide(BigDecimal.valueOf(revenue)));
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
        public double getRevenueAsPercentage() {
            return 1 + getRevenue().divide(getCost()).doubleValue();
        }

        @Override
        public BigDecimal getBalance() {
            return getCost().add(getRevenue());
        }
    }
}
