package com.liato.bankdroid.api.domain.account;

import java.math.BigDecimal;
import java.util.Map;

/**
 * A parent interface for all account types.
 */
public interface Account {

    /**
     * Returns the account id.
     * @return Returns the account id.
     */
    String getId();

    /**
     * Returns the account's name.
     * @return the account name.
     */
    String getName();

    /**
     * Returns the total balance for the account.
     * @return The total balance for the account.
     */
    BigDecimal getBalance();

    /**
     * The ISO 4217 currency representation, when possible.
     * @return The main currency for the account.
     */
    String getCurrency();

    /**
     * Returns a collection of extra attributes that can be set on an account.
     * @return A collection of custom attributes, or an empty collection if no custom
     * attributes exist.
     */
    Map<String, String> getCustomAttributes();
}
