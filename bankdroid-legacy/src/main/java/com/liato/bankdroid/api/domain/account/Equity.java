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
     * Returns the total revenue for this equity, as percentage. A value less than {@code 1} is a
     * loss and a value greater than one is a profit. e.g. {@code 0.75} represents a 25 % loss,
     * while {@code 1.5} is 50 % profit.
     * @return Total revenue as percentage.
     */
    double getRevenueAsPercentage();

    /**
     * Returns the current balance for the equity.
     * @return the equity's balance.
     */
    BigDecimal getBalance();
}
