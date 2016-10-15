package com.liato.bankdroid.api.domain.account;

import org.joda.time.DateTime;

import java.math.BigDecimal;

/**
 * Represents a Liability payment.
 */
public interface Payment {

    /**
     * Returns the date when the payment is due.
     * @return the payment's due date.
     */
    DateTime getDueDate();

    /**
     * Returns the payment amount.
     * @return the payment amount.
     */
    BigDecimal getAmount();

    /**
     * The ISO 4217 currency representation, when possible.
     * @return The main currency for the account.
     */
    String getCurrency();

    /**
     * A description of the payment.
     * @return payment description.
     */
    String getDescription();
}
