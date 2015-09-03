package com.liato.bankdroid.api.domain;

import com.liato.bankdroid.api.domain.account.Account;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * Represents a single connection to the {@link com.liato.bankdroid.api.Provider}.
 */
public interface ProviderConnection {

    /**
     * Returns the name of the connection.
     * @return The connection name.
     */
    String getName();

    /**
     * Returns the total balance for all accounts associated with the connection. In the case the
     * accounts have multiple currencies, these will be converted to the currency returned by the
     * {@link #getDefaultCurrency()}.
     *
     * @return The total balance for the connection.
     */
    BigDecimal getTotalBalance();

    /**
     * Returns the default currency for the connection.
     * @return The default currency for the connection.
     */
    String getDefaultCurrency();

    /**
     * Returns a collection of all accounts associated with the connection.
     * @return All accounts for the connection.
     */
    Collection<Account> getAccounts();
}
