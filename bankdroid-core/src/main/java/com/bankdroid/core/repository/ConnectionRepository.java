package com.bankdroid.core.repository;

import java.util.Collection;

public interface ConnectionRepository {

    long save(ConnectionEntity connection);

    ConnectionEntity findById(long connectionId);

    Collection<ConnectionEntity> findAll();

    void delete(long connectionId);

    void disable(long connectionId);

    void toggleHiddenAccounts(long connectionId);

}