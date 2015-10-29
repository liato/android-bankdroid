package com.liato.bankdroid.api.domain.account;

import java.util.Collection;

/**
 * Represents a transaction account. Holds multiple transactions.
 */
public interface TransactionAccount extends Account {

    /**
     * Returns a collection of transaction.
     * @return All transactions.
     */
    Collection<Transaction> getTransactions();
}
