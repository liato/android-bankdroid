package com.liato.bankdroid.api.domain.account;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * Represent an Equity Account. An equity account is a collection of shares, funds etc.
 */
public interface EquityAccount extends Account {

    /**
     * Returns the total cost for all equities included in the account.
     * @return Total cost.
     */
    BigDecimal getCost();

    /**
     * Returns the total revenue for all equities in the account.
     * @return Total revenue.
     */
    BigDecimal getRevenue();

    /**
     * Returns the total revenue for all equities in the account, in percent.
     * @return Total revenue in percent, in 1/100.
     */
    double getRevenueInPercent();

    /**
     * Returns all equities for the account.
     * @return All account equities.
     */
    Collection<Equity> getEquities();
}
