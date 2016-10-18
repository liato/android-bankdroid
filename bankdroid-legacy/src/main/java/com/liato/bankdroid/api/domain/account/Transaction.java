package com.liato.bankdroid.api.domain.account;

import org.joda.time.DateTime;

import java.math.BigDecimal;

/**
 * Represents a single transaction.
 */
public interface Transaction {

    /**
     * Returns the transaction amount.
     * @return the transaction amount.
     */
    BigDecimal getAmount();

    /**
     * Returns the currency for the transaction, conforming to ISO 4217 when possible.
     * @return the transaction currency.
     */
    String getCurrency();

    /**
     * Returns a short description of the transaction.
     * @return the transaction description.
     */
    String getDescription();

    /**
     * Returns the date when the transaction was initiated.
     * @return the transaction date.
     */
    DateTime getTransactionDate();

    /**
     * Returns {@code true} if, and only if, the transaction is still pending.
     * @return {@code true} if the transaction is pending, otherwise {@code false}.
     */
    boolean isPending();
}
