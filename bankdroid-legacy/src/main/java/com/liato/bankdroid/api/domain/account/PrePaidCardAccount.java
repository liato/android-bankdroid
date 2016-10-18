package com.liato.bankdroid.api.domain.account;

import org.joda.time.DateTime;

/**
 * Represents a prepaid card. e.g. a bus card, cash card etc.
 */
public interface PrePaidCardAccount extends Account {

    /**
     * Returns the date when the card expired.
     * @return the expiration date.
     */
    DateTime getExpirationDate();

    /**
     * Returns the date from when the card is valid.
     * @return the valid from date.
     */
    DateTime getValidFrom();
}
