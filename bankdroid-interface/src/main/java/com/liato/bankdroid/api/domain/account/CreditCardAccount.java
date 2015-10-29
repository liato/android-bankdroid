package com.liato.bankdroid.api.domain.account;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * Represents a Credit Card Account.
 */
public interface CreditCardAccount extends Account {

    /**
     * Returns the credit limit for the account.
     * @return the credit limit.
     */
    BigDecimal getCreditLimit();

    /**
     * Returns all transactions for the account.
     * @return All transactions for the account.
     */
    Collection<Transaction> getTransactions();
}
