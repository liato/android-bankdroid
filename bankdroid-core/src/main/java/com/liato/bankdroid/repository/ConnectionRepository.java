package com.liato.bankdroid.repository;

import com.liato.bankdroid.repository.entities.ConnectionEntity;

import java.util.Collection;

public interface ConnectionRepository {

    long save(ConnectionEntity connection);

    ConnectionEntity findById(long bankId);

    Collection<ConnectionEntity> findAll();

    void delete(long bankId);

    void disable(long bankId);

    void toggleHiddenAccounts(long bankId);
}
