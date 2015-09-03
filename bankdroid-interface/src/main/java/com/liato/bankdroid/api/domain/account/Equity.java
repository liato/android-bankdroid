package com.liato.bankdroid.api.domain.account;

import java.math.BigDecimal;

/**
 * Represent a single equity, could be a share, fund etc.
 */
public interface Equity {

    /**
     * Returns the equity name.
     * @return the equity name.
     */
    String getName();

    /**
     * The ISO 4217 currency representation, when possible.
     * @return The equity's currency.
     */
    String getCurrency();

    /**
     * Returns the number of shares for this equity.
     * @return The quantity of equities.
     */
    double getQuantity();

    /**
     * Returns the total cost for this equity.
     * @return Cost for the entity.
     */
    BigDecimal getCost();

    /**
     * Returns the total revenue for this equity.
     * @return Equity revenue.
     */
    BigDecimal getRevenue();

    /**
     * Returns the total revenue for this equity, in percent.
     * @return Total revenue in percent, in 1/100.
     */
    double getRevenueInPercent();

    /**
     * Returns the current balance for the equity.
     * @return the equity's balance.
     */
    BigDecimal getBalance();
}
