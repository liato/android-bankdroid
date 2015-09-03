package com.liato.bankdroid.api.domain.account;

import java.util.Collection;

/**
 * Represents a liability account. A liability can for example be a loan.
 */
public interface LiabilityAccount extends Account {

    /**
     * Returns the interest as a number less than 1, representing the interest in percentage.
     * @return the interest in percent.
     */
    double getInterest();

    /**
     * Returns a collection of payments, could be both historical and upcoming payments.
     * @return A collection of payments.
     */
    Collection<Payment> getPayments();
}
