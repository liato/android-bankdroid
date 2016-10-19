package com.liato.bankdroid.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.bankdroid.core.repository.AccountEntity;
import com.bankdroid.core.repository.ConnectionEntity;
import com.bankdroid.core.repository.ConnectionRepository;
import com.bankdroid.core.repository.TransactionEntity;
import com.liato.bankdroid.banking.LegacyProviderConfiguration;

import net.sf.andhsli.hotspotlogin.SimpleCrypto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AndroidConnectionRepository implements ConnectionRepository {

    private final SQLiteDatabase db;
    private final ConnectionEntityTransformer connectionEntityTransformer;
    private final AccountEntityTransformer accountEntityTransformer;
    private final TransactionEntityTransformer transactionEntityTransformer;

    public AndroidConnectionRepository(SQLiteDatabase db) {
        this.db = db;
        connectionEntityTransformer = new ConnectionEntityTransformer();
        accountEntityTransformer = new AccountEntityTransformer();
        transactionEntityTransformer = new TransactionEntityTransformer();
    }

    @Override
    public ConnectionEntity findById(long connectionId) {
        Cursor cursor = null;
        try {
            cursor = db.query(Database.CONNECTION_TABLE_NAME, null,
                    Database.CONNECTION_ID + "= ?",
                    new String[]{Long.toString(connectionId)}, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                return asConnectionEntity(cursor);
            }
        } finally {
            if(cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    private ConnectionEntity asConnectionEntity(Cursor cursor) {
        ContentValues values = asContentValues(cursor);
        long connectionId = values.getAsLong(Database.CONNECTION_ID);
        return connectionEntityTransformer.transform(values)
                .properties(propertiesFor(connectionId))
                .accounts(accountsFor(connectionId))
                .build();
    }

    private ContentValues asContentValues(Cursor cursor) {
        ContentValues values = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, values);
        return values;
    }

    // TODO decrypt password
    private Map<String, String> propertiesFor(long connectionId) {
        Cursor cursor = null;
        try {
            cursor = db.query(Database.PROPERTY_TABLE_NAME, null,
                    Database.PROPERTY_CONNECTION_ID + "= ?",
                    new String[]{Long.toString(connectionId)}, null, null, null);
            if (cursor != null) {
                Map<String, String> properties = new HashMap<>();
                while (!cursor.isLast() && !cursor.isAfterLast()) {
                    cursor.moveToNext();
                    String key = cursor.getString(cursor.getColumnIndex(Database.PROPERTY_KEY));
                    String value = cursor.getString(cursor.getColumnIndex(Database.PROPERTY_VALUE));
                    try {
                        properties.put(key,
                                LegacyProviderConfiguration.PASSWORD.equals(key) ?
                                        SimpleCrypto.decrypt(Crypto.getKey(), value) :
                                        value);
                    } catch (Exception e) {
                        // TODO log exception
                    }
                }
                return properties;
            }
        } finally {
            if(cursor != null) {
                cursor.close();
            }
        }
        return Collections.emptyMap();
    }

    private Collection<AccountEntity> accountsFor(long connectionId) {
        Cursor cursor = null;
        try {
            cursor = db.query(Database.ACCOUNTS_TABLE_NAME, null,
                    Database.ACCOUNT_CONNECTION_ID + "= ?",
                    new String[]{Long.toString(connectionId)}, null, null, null);
            if (cursor != null) {
                Collection<AccountEntity> accounts = new ArrayList<>();
                while (!cursor.isLast() && !cursor.isAfterLast()) {
                    cursor.moveToNext();
                    ContentValues values = asContentValues(cursor);
                    String accountId = values.getAsString(Database.ACCOUNT_ID);
                    AccountEntity account = accountEntityTransformer.transform(values)
                            .transactions(transactionsFor(connectionId, accountId))
                            .build();
                    accounts.add(account);
                }
                return accounts;
            }
        } finally {
            if(cursor != null) {
                cursor.close();
            }
        }
        return Collections.emptyList();
    }

    private Collection<TransactionEntity> transactionsFor(long connectionId, String accountId) {
        Cursor cursor = null;
        try {
            cursor = db.query(Database.TRANSACTIONS_TABLE_NAME, null,
                    Database.TRANSACTION_CONNECTION_ID + "= ? AND " +
                            Database.TRANSACTION_ACCOUNT_ID + "= ?",
                    new String[]{Long.toString(connectionId), accountId}, null, null, null);
            if (cursor != null) {
                Collection<TransactionEntity> transactions = new ArrayList<>();
                while (!cursor.isLast() && !cursor.isAfterLast()) {
                    cursor.moveToNext();
                    ContentValues values = asContentValues(cursor);
                    transactions.add(transactionEntityTransformer.transform(values).build());
                }
                return transactions;
            }
        } finally {
            if(cursor != null) {
                cursor.close();
            }
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<ConnectionEntity> findAll() {
        Cursor cursor = null;
        try {
            cursor = db.query(Database.CONNECTION_TABLE_NAME, null,
                    null, null, null, null, null);
            if (!(cursor == null || cursor.isClosed() || (cursor.isBeforeFirst() && cursor.isAfterLast()))) {
                Collection<ConnectionEntity> connections = new ArrayList<>();
                while (!cursor.isLast() && !cursor.isAfterLast()) {
                    cursor.moveToNext();
                    connections.add(asConnectionEntity(cursor));
                }
                return connections;
            }
        } finally {
            if(cursor != null) {
                cursor.close();
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void delete(long connectionId) {
        db.delete(Database.CONNECTION_TABLE_NAME, Database.CONNECTION_ID + " = ?",
                new String[]{Long.toString(connectionId)});
    }

    @Override
    public void disable(long connectionId) {
        if (connectionId == ConnectionEntity.DEFAULT_ID) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(Database.CONNECTION_ENABLED, 0);
        db.update(
                Database.CONNECTION_TABLE_NAME,
                values,
                Database.CONNECTION_ID + " = ?",
                new String[]{Long.toString(connectionId)}
        );
    }

    @Override
    public void toggleHiddenAccounts(long connectionId) {

    }

    @Override
    public long save(ConnectionEntity connection) {
        long connectionId;
        try {
            db.beginTransaction();
            connectionId = saveConnection(connection);
            saveProperties(connectionId, connection.properties());
            saveAccounts(connectionId, connection.accounts());
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return connectionId;
    }

    private void saveAccounts(long connectionId, Collection<AccountEntity> accounts) {
        for(AccountEntity account : accounts) {
            ContentValues values = accountEntityTransformer.transform(account);
            values.put(Database.ACCOUNT_CONNECTION_ID, connectionId);
            db.insertWithOnConflict(Database.ACCOUNTS_TABLE_NAME, null, values,
                    SQLiteDatabase.CONFLICT_REPLACE);
            saveTransactions(connectionId, account.id(), account.transactions());
        }
    }

    private void saveTransactions(long connectionId,
                                  String accountId,
                                  Collection<TransactionEntity> transactions) {
        for(TransactionEntity transaction : transactions) {
            ContentValues values = transactionEntityTransformer.transform(transaction);
            values.put(Database.TRANSACTION_ACCOUNT_ID, accountId);
            values.put(Database.TRANSACTION_CONNECTION_ID, connectionId);
            db.insertOrThrow(Database.TRANSACTIONS_TABLE_NAME, null, values);
        }
    }

    private long saveConnection(ConnectionEntity connection) {
        long connectionId = connection.id();
        ContentValues values = connectionEntityTransformer.transform(connection);
        if(ConnectionEntity.DEFAULT_ID == connectionId) {
            connectionId = db.insertOrThrow(Database.CONNECTION_TABLE_NAME, null, values);
        } else {
            db.update(
                    Database.CONNECTION_TABLE_NAME,
                    values,
                    Database.CONNECTION_ID + " = ?",
                    new String[]{Long.toString(connection.id())}
            );
        }
        return connectionId;
    }

    private void saveProperties(long connectionId, Map<String, String> properties) {
        for (Map.Entry<String, String> property : properties.entrySet()) {
            String value = property.getValue();
            if (value != null && !value.isEmpty()) {
                if (LegacyProviderConfiguration.PASSWORD.equals(property.getKey())) {
                    try {
                        value = SimpleCrypto.encrypt(Crypto.getKey(), value);
                    } catch (Exception e) {
                       //TODO log exception
                    }
                }
                ContentValues propertyValues = new ContentValues();
                propertyValues.put(Database.PROPERTY_KEY, property.getKey());
                propertyValues.put(Database.PROPERTY_VALUE, value);
                propertyValues.put(Database.PROPERTY_CONNECTION_ID, connectionId);
                db.insertWithOnConflict(
                        Database.PROPERTY_TABLE_NAME,
                        null,
                        propertyValues,
                        SQLiteDatabase.CONFLICT_REPLACE);
            }
        }
    }

/*    private long old() {

        if (bankId == -1) {
            bankId = mDb.insert("banks", null, initialValues);
        } else {
            mDb.update("banks", initialValues, "_id=" + bankId, null);
            deleteAccounts(bankId);
            deleteProperties(bankId);
        }
        if (bankId != -1) {
            Map<String, String> properties = bank.getProperties();
            for (Map.Entry<String, String> property : properties.entrySet()) {
                String value = property.getValue();
                if (value != null && !value.isEmpty()) {
                    if (LegacyProviderConfiguration.PASSWORD.equals(property.getKey())) {
                        try {
                            value = SimpleCrypto.encrypt(Crypto.getKey(), bank.getPassword());
                        } catch (Exception e) {
                            Log.w(TAG, "SimpleCrypto error: " + e.getMessage());
                        }
                    }
                    ContentValues propertyValues = new ContentValues();
                    propertyValues.put(Database.PROPERTY_KEY, property.getKey());
                    propertyValues.put(Database.PROPERTY_VALUE, value);
                    propertyValues.put(Database.PROPERTY_CONNECTION_ID, bankId);
                    mDb.insert(Database.PROPERTY_TABLE_NAME, null, propertyValues);
                }
            }

            ArrayList<Account> accounts = bank.getAccounts();
            for (Account acc : accounts) {
                String accountId = legacyAccountIdOf(bankId, acc.getId());
                ContentValues vals = new ContentValues();
                vals.put("bankid", bankId);
                vals.put("balance", acc.getBalance().toPlainString());
                vals.put("name", acc.getName());
                vals.put("id", accountId);
                vals.put("hidden", acc.isHidden() ? 1 : 0);
                vals.put("notify", acc.isNotify() ? 1 : 0);
                vals.put("currency", acc.getCurrency());
                vals.put("acctype", acc.getType());
                vals.put("aliasfor", acc.getAliasfor());
                mDb.insert("accounts", null, vals);
                if (acc.getAliasfor() == null || acc.getAliasfor().length() == 0) {
                    List<Transaction> transactions = acc.getTransactions();
                    if (transactions != null && !transactions.isEmpty()) {
                        deleteTransactions(accountId);
                        for (Transaction transaction : transactions) {
                            ContentValues transvals = new ContentValues();
                            transvals.put("transdate", transaction.getDate());
                            transvals.put("btransaction", transaction.getTransaction());
                            transvals.put("amount", transaction.getAmount().toPlainString());
                            transvals.put("account",
                                    accountId);
                            transvals.put("currency", transaction.getCurrency());
                            mDb.insert("transactions", null, transvals);
                        }
                    }
                }
            }
        }
        return bankId;
    }*/
}
