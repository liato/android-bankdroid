/*
 * Copyright (C) 2010 Nullbyte <http://nullbyte.eu>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liato.bankdroid.db;

import com.bankdroid.core.repository.ConnectionRepository;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.compatibility.ConnectionTransformer;

import android.content.Context;
import android.database.Cursor;
import java.util.Collection;

public class DBAdapter {

    private final ConnectionRepository repository;

    private final ConnectionTransformer legacyTransformer;

    DBAdapter(ConnectionRepository repository, ConnectionTransformer legacyTransformer) {
        this.repository = repository;
        this.legacyTransformer = legacyTransformer;
    }

    /**
     * @deprecated Only used during refactoring. Should be removed before next major version (2.0)
     */
    @Deprecated
    public static void save(Bank bank, Context context) {
        DBAdapter db = DBAdapter.create(context);
        long id = db.updateBank(bank);
        bank.setDbid(id);
    }

    /**
     * @deprecated Only used during refactoring. Should be removed before next major version (2.0)
     */
    @Deprecated
    public static void disable(Bank bank, Context context) {
        DBAdapter db = DBAdapter.create(context);
        db.disableBank(bank.getDbId());
    }

    public void deleteConnection(long connectionId) {
        repository.delete(connectionId);
    }

    public Collection<Bank> fetchBanks() throws BankException {
        return legacyTransformer.asBanks(repository.findAll());
    }

    public long updateBank(Bank bank) {
        return repository.save(legacyTransformer.asConnection(bank));
    }

    public void disableBank(long bankId) {
        repository.disable(bankId);
    }

    public Bank getBank(long bankId) throws BankException {
        return legacyTransformer.asBank(
                repository.findById(bankId)
        );
    }

    public static DBAdapter create(Context context) {
        return new DBAdapter(new AndroidConnectionRepository(
                DatabaseHelper.getHelper(context).getWritableDatabase()),
                new ConnectionTransformer(context)
                );
    }
}
