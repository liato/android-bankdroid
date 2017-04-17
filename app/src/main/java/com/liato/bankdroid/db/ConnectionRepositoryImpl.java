package com.liato.bankdroid.db;

import static com.liato.bankdroid.db.Database.*;

import com.liato.bankdroid.repository.ConnectionRepository;
import com.liato.bankdroid.repository.entities.ConnectionEntity;
import com.liato.bankdroid.repository.entities.TransactionEntity;
import com.liato.bankdroid.repository.entities.accounts.AccountEntity;
import com.liato.bankdroid.repository.entities.accounts.AccountTypeEntity;
import com.liato.bankdroid.repository.entities.accounts.TransactionAccountEntity;

import org.joda.time.DateTime;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ConnectionRepositoryImpl implements ConnectionRepository {

    private static final String TAG = "ConnectionRepository";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private SQLiteDatabase mDb;

    public ConnectionRepositoryImpl(DatabaseHelper dbHelper) {
        mDb = dbHelper.getWritableDatabase();
    }

    @Override
    public long save(ConnectionEntity connection) {
        try {
            mDb.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(CONNECTION_PROVIDER_ID, connection.getProviderId());
            values.put(CONNECTION_ENABLED, connection.isEnabled());
            values.put(CONNECTION_NAME, connection.getName());
            values.put(CONNECTION_LAST_UPDATED, DateTime.now().toString(DATE_TIME_FORMAT));
           // values.put(BANK_HIDE_ACCOUNTS, connection.isAccountsHidden());

            if (connection.getId() == ConnectionEntity.DEFAULT_ID) {
                connection.setId(mDb.insert(CONNECTION_TABLE_NAME, null, values));
            } else {
                mDb.update(CONNECTION_TABLE_NAME, values, CONNECTION_ID + "= ?", new String[]{Long.toString(connection.getId())});
                deleteAccounts(connection.getId());
                deleteProperties(connection.getId());
            }
            if (connection.getId() != ConnectionEntity.DEFAULT_ID) {
                saveProperties(connection.getId(), connection.getProperties());
                saveAccounts(connection.getAccounts());
            }
            mDb.setTransactionSuccessful();
        } finally {
            mDb.endTransaction();
        }
        return connection.getId();
    }

    @Override
    public ConnectionEntity findById(long bankId) {
        return null;
    }

    @Override
    public Collection<ConnectionEntity> findAll() {
        List<ConnectionEntity> banks = new ArrayList<>();

        Cursor c = mDb.query(CONNECTION_TABLE_NAME, null, null, null, null, null, CONNECTION_ID + " asc");
        if (c == null) {
            return banks;
        }
        while (!c.isLast() && !c.isAfterLast()) {
            c.moveToNext();
            ConnectionEntity bank = toConnectionEntity(c);
            bank.setAccounts(findAccounts(bank.getId()));
            bank.setProperties(findProperties(bank.getId()));
            banks.add(bank);
        }
        c.close();
        return banks;
    }

    @Override
    public void delete(long connectionId) {
        mDb.delete(CONNECTION_TABLE_NAME, CONNECTION_ID + "=" + connectionId, null);
    }

    @Override
    public void disable(long connectionId) {
        if (connectionId == ConnectionEntity.DEFAULT_ID) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(CONNECTION_ENABLED, false);
        mDb.update(CONNECTION_TABLE_NAME, values, CONNECTION_ID + "= ?",
                new String[]{Long.toString(connectionId)});
    }
    @Override
    public void toggleHiddenAccounts(long bankId) {

    }

    private Collection<AccountEntity> findAccounts(long connectionId) {
        Collection<AccountEntity> accounts = new ArrayList<>();

        Cursor c = mDb.query(ACCOUNTS_TABLE_NAME, null, ACCOUNT_CONNECTION_ID + "= ?",
                new String[]{Long.toString(connectionId)}, null, null, null);
        if (c == null) {
            return accounts;
        }
        while (!c.isLast() && !c.isAfterLast()) {
            c.moveToNext();
            AccountEntity account = toAccountEntity(c);
            accounts.add(account);
        }
        c.close();

        return accounts;
    }


    private Map<String, String> findProperties(long connectionId) {
        Map<String, String> properties = new HashMap<>();

        Cursor c = mDb.query(PROPERTY_TABLE_NAME, new String[] {PROPERTY_KEY, PROPERTY_VALUE},
                PROPERTY_CONNECTION_ID + "= ?", new String[]{Long.toString(connectionId)}, null, null, null);
        if (c == null) {
            return properties;
        }
        while (!c.isLast() && !c.isAfterLast()) {
            c.moveToNext();
            properties.put(c.getString(c.getColumnIndex(PROPERTY_KEY)),
                    c.getString(c.getColumnIndex(PROPERTY_VALUE)));
        }
        c.close();

        return properties;
    }

    private void saveAccounts(Collection<AccountEntity> accounts) {
        for(AccountEntity account : accounts) {
            ContentValues values = new ContentValues();
            values.put(ACCOUNT_CONNECTION_ID, account.getConnectionId());
            values.put(ACCOUNT_BALANCE, account.getBalance().toPlainString());
            values.put(ACCOUNT_TYPE, account.getAccountType().toString());
            values.put(ACCOUNT_NAME, account.getName());
            values.put(ACCOUNT_ID, account.getId());
            values.put(ACCOUNT_HIDDEN, account.isHidden());
            values.put(ACCOUNT_NOTIFICATIONS_ENABLED, account.isNotificationsEnabled());
            values.put(ACCOUNT_CURRENCY, account.getCurrency());
            mDb.insert(ACCOUNTS_TABLE_NAME, null, values);
            if(AccountTypeEntity.TRANSACTION.equals(account.getAccountType())) {
                saveTransactions((TransactionAccountEntity) account);
            }
        }
    }

    private void saveProperties(long bankId, Map<String, String> properties) {
        for(Map.Entry<String,String> propertyEntry : properties.entrySet()) {
            ContentValues values = new ContentValues();
            values.put(PROPERTY_CONNECTION_ID, bankId);
            values.put(PROPERTY_KEY, propertyEntry.getKey());
            values.put(PROPERTY_VALUE, propertyEntry.getValue());
            mDb.insert(PROPERTY_TABLE_NAME, null, values);
        }
    }

    private void saveTransactions(TransactionAccountEntity account) {
        for(TransactionEntity entity : account.getTransactions()) {
            ContentValues values = new ContentValues();
            values.put(TRANSACTION_ID, entity.getId());
            values.put(TRANSACTION_CONNECTION_ID, entity.getConnectionId());
            values.put(TRANSACTION_ACCOUNT_ID, entity.getAccountId());
            values.put(TRANSACTION_AMOUNT, entity.getAmount().toPlainString());
            values.put(TRANSACTION_CURRENCY, entity.getCurrency());
            values.put(TRANSACTION_DESCRIPTION, entity.getDescription());
            values.put(TRANSACTION_DATE, entity.getTransactionDate().toString());
            values.put(TRANSACTION_PENDING, entity.isPending() ? 1 : 0);
            mDb.insert(TRANSACTIONS_TABLE_NAME, null, values);
        }
    }

    private void deleteAccounts(long connectionId) {
        mDb.delete(ACCOUNTS_TABLE_NAME, ACCOUNT_CONNECTION_ID + "= ?", new String[]{Long.toString(connectionId)});
    }

    private void deleteProperties(long connectionId) {
        mDb.delete(PROPERTY_TABLE_NAME, PROPERTY_CONNECTION_ID + "= ?", new String[]{Long.toString(connectionId)});
    }

    private ConnectionEntity toConnectionEntity(Cursor c) {
        String providerId = c.getString(c.getColumnIndex(CONNECTION_PROVIDER_ID));
        long id = c.getLong(c.getColumnIndex(CONNECTION_ID));
        //String balanceString = c.getString(c.getColumnIndex(CONNECTION_BALANCE));
        //BigDecimal balance = new BigDecimal(balanceString);
        boolean enabled = (c.getInt(c.getColumnIndex(CONNECTION_ENABLED)) != 0 );
        //String currency = c.getString(c.getColumnIndex(CONNECTION));
        String name = c.getString(c.getColumnIndex(CONNECTION_NAME));

        ConnectionEntity entity = new ConnectionEntity(id, providerId);
        entity.setEnabled(enabled);
        //entity.setBalance(balance);
        //entity.setCurrency(currency);
        entity.setName(name);
        //entity.setAccountsHidden(c.getInt(c.getColumnIndex(BANK_HIDE_ACCOUNTS)) != 0);
        return entity;
    }

    private AccountEntity toAccountEntity(Cursor c) {
        String id =  c.getString(c.getColumnIndex(ACCOUNT_ID));
        String name = c.getString(c.getColumnIndex(ACCOUNT_NAME));
        BigDecimal balance = new BigDecimal(c.getString(c.getColumnIndex(ACCOUNT_BALANCE)));
        long bankId = c.getLong(c.getColumnIndex(ACCOUNT_CONNECTION_ID));
        String type = c.getString(c.getColumnIndex(ACCOUNT_TYPE));
        String currency = c.getString(c.getColumnIndex(ACCOUNT_CURRENCY));

        AccountEntity entity = null;
        switch (AccountTypeEntity.valueOf(type)) {
            case TRANSACTION:
                entity = createTransactionAccountEntity(bankId, id);
                break;
            default:
                throw new IllegalArgumentException("Cannot map " + type + "to a known entity.");
        }
        entity.setName(name);
        entity.setConnectionId(bankId);
        entity.setBalance(balance);
        entity.setCurrency(currency);
        return entity;
    }

    private AccountEntity createTransactionAccountEntity(long connectionId, String id) {
        TransactionAccountEntity entity = new TransactionAccountEntity(id);
        entity.setTransactions(fetchTransactions(connectionId, id));
        return entity;
    }

    private Collection<TransactionEntity> fetchTransactions(long connectionId, String accountId) {
        Collection<TransactionEntity> entities = new LinkedList<>();
        Cursor c = mDb.query(TRANSACTIONS_TABLE_NAME, new String[] {TRANSACTION_CONNECTION_ID, TRANSACTION_ACCOUNT_ID},
                TRANSACTION_CONNECTION_ID + "= ? AND " + TRANSACTION_ACCOUNT_ID +"= ?",
                new String[]{Long.toString(connectionId), accountId}, null, null, null);
        if (c == null) {
            return entities;
        }
        while (!c.isLast() && !c.isAfterLast()) {
            c.moveToNext();
            entities.add(toTransactionEntity(c));
        }
        c.close();

        return entities;
    }

    private TransactionEntity toTransactionEntity(Cursor c) {
        TransactionEntity entity = new TransactionEntity();
        entity.setConnectionId(c.getLong(c.getColumnIndex(TRANSACTION_CONNECTION_ID)));
        entity.setAccountId(c.getString(c.getColumnIndex(TRANSACTION_ACCOUNT_ID)));
        entity.setAmount(new BigDecimal(c.getString(c.getColumnIndex(TRANSACTION_AMOUNT))));
        entity.setCurrency(c.getString(c.getColumnIndex(TRANSACTION_CURRENCY)));
        entity.setTransactionDate(new DateTime(c.getString(c.getColumnIndex(TRANSACTION_DATE))));
        entity.setDescription(c.getString(c.getColumnIndex(TRANSACTION_DESCRIPTION)));
        //c.getString(c.getColumnIndex(TRANSACTION_ID));
        entity.setPending(c.getInt(c.getColumnIndex(TRANSACTION_PENDING)) == 1 ? true : false);
        return entity;
    }
}
