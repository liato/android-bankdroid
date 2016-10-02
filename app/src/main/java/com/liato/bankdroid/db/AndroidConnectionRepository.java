package com.liato.bankdroid.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.bankdroid.core.repository.AccountEntity;
import com.bankdroid.core.repository.ConnectionEntity;
import com.bankdroid.core.repository.ConnectionRepository;
import com.liato.bankdroid.banking.LegacyProviderConfiguration;

import net.sf.andhsli.hotspotlogin.SimpleCrypto;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AndroidConnectionRepository implements ConnectionRepository {

    private final SQLiteDatabase db;
    private final ConnectionEntityTransformer connectionEntityTransformer;

    public AndroidConnectionRepository(SQLiteDatabase db) {
        this.db = db;
        connectionEntityTransformer = new ConnectionEntityTransformer();
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
        return connectionEntityTransformer.transform(values)
                .properties(propertiesFor(values.getAsLong(Database.CONNECTION_ID)))
                .build();
    }

    private ContentValues asContentValues(Cursor cursor) {
        ContentValues values = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, values);
        return values;
    }

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
                    properties.put(
                            cursor.getString(cursor.getColumnIndex(Database.PROPERTY_KEY)),
                            cursor.getString(cursor.getColumnIndex(Database.PROPERTY_VALUE))
                    );
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
        return 0;
    }

    private void saveAccounts(long connectionId, Collection<AccountEntity> accounts) {
        //TODO implementaiton
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
                db.insertOrThrow(Database.PROPERTY_TABLE_NAME, null, propertyValues);
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
