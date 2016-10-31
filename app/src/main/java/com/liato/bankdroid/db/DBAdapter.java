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

import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.Transaction;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;


public class DBAdapter {

    private SQLiteDatabase mDb;

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param ctx the Context within which to work
     */
    public DBAdapter(Context ctx) {
        DatabaseHelper dbHelper = DatabaseHelper.getHelper(ctx);
        mDb = dbHelper.getWritableDatabase();
    }

    /**
     * @deprecated Only used during refactoring. Should be removed before next major version (2.0)
     */
    @Deprecated
    public static void save(Bank bank, Context context) {
        DBAdapter db = new DBAdapter(context);
        long id = db.updateBank(bank);

        bank.setDbid(id);
    }

    /**
     * @deprecated Only used during refactoring. Should be removed before next major version (2.0)
     */
    @Deprecated
    public static void disable(Bank bank, Context context) {
        DBAdapter db = new DBAdapter(context);
        db.disableBank(bank.getDbId());
    }

    public long createBank(Bank bank) {
        return updateBank(bank);
    }

    /**
     * Delete the bank with the given bankId
     *
     * @param bankId id of bank to delete
     */
    public int deleteBank(long bankId) {
        int c = mDb.delete("banks", "_id=" + bankId, null);
        c += this.deleteAccounts(bankId);
        return c;
    }

    /**
     * Delete the accounts for the given bankIdbank with the given rowId
     *
     * @param bankId id of bank to delete
     */
    public int deleteAccounts(long bankId) {
        int c = mDb.delete("accounts", "bankid=" + bankId, null);
        return c;
    }

    public int deleteTransactions(String account) {
        int c = mDb.delete("transactions", "account='" + account + "'", null);
        return c;
    }

    private int deleteProperties(long bankId) {
        return mDb.delete(Database.PROPERTY_TABLE_NAME, Database.PROPERTY_CONNECTION_ID + "="
                + bankId, null);
    }

    /**
     * Return a Cursor over the list of all banks in the database
     *
     * @return Cursor over all banks
     */
    public Cursor fetchBanks() {
        return mDb.query("banks",
                new String[]{"_id", "balance", "banktype", "disabled",
                        "custname", "updated", "sortorder", "currency", "hideAccounts"},
                null, null, null, null, "_id asc");
    }

    /**
     * Return a Cursor over the list of all accounts belonging to a bank
     *
     * @return Cursor over all accounts belonging to a bank
     */
    public Cursor fetchAccounts(long bankId) {
        return mDb.query("accounts",
                new String[]{"bankid", "balance", "name", "id", "acctype", "hidden", "notify",
                        "currency", "aliasfor"}, "bankid=" + bankId, null, null, null, null);
    }

    public Cursor fetchTransactions(String account) {
        return mDb.query("transactions",
                new String[]{"transdate", "btransaction", "amount", "currency"},
                "account='" + account + "'", null, null, null, null);
    }

    public Cursor fetchProperties(String bankId) {
        return mDb.query(Database.PROPERTY_TABLE_NAME, null,
                Database.PROPERTY_CONNECTION_ID + "='" + bankId + "'", null, null, null, null);
    }

    public long updateBank(Bank bank) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        ContentValues initialValues = new ContentValues();
        initialValues.put("banktype", bank.getBanktypeId());
        initialValues.put("disabled", 0);
        initialValues.put("balance", bank.getBalance().toPlainString());
        initialValues.put("currency", bank.getCurrency());
        initialValues.put("custname", bank.getCustomName());
        initialValues.put("updated", sdf.format(cal.getTime()));
        initialValues.put("hideAccounts", bank.getHideAccounts() ? 1 : 0);

        long bankId = bank.getDbId();
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
                    ContentValues propertyValues = new ContentValues();
                    propertyValues.put(Database.PROPERTY_KEY, property.getKey());
                    propertyValues.put(Database.PROPERTY_VALUE, value);
                    propertyValues.put(Database.PROPERTY_CONNECTION_ID, bankId);
                    mDb.insert(Database.PROPERTY_TABLE_NAME, null, propertyValues);
                }
            }

            ArrayList<Account> accounts = bank.getAccounts();
            for (Account acc : accounts) {
                ContentValues vals = new ContentValues();
                vals.put("bankid", bankId);
                vals.put("balance", acc.getBalance().toPlainString());
                vals.put("name", acc.getName());
                vals.put("id", bankId + "_" + acc.getId());
                vals.put("hidden", acc.isHidden() ? 1 : 0);
                vals.put("notify", acc.isNotify() ? 1 : 0);
                vals.put("currency", acc.getCurrency());
                vals.put("acctype", acc.getType());
                vals.put("aliasfor", acc.getAliasfor());
                mDb.insert("accounts", null, vals);
                if (acc.getAliasfor() == null || acc.getAliasfor().length() == 0) {
                    List<Transaction> transactions = acc.getTransactions();
                    if (transactions != null && !transactions.isEmpty()) {
                        deleteTransactions(bankId + "_" + acc.getId());
                        for (Transaction transaction : transactions) {
                            ContentValues transvals = new ContentValues();
                            transvals.put("transdate", transaction.getDate());
                            transvals.put("btransaction", transaction.getTransaction());
                            transvals.put("amount", transaction.getAmount().toPlainString());
                            transvals.put("account",
                                    bankId + "_" + acc.getId());
                            transvals.put("currency", transaction.getCurrency());
                            mDb.insert("transactions", null, transvals);
                        }
                    }
                }
            }
        }
        return bankId;
    }

    public void disableBank(long bankId) {
        if (bankId == -1) {
            return;
        }
        ContentValues initialValues = new ContentValues();
        initialValues.put("disabled", 1);
        mDb.update("banks", initialValues, "_id=" + bankId, null);
    }

    @Nullable
    public Cursor getBank(String bankId) {
        Cursor c = mDb.query("banks",
                new String[]{"_id", "balance", "banktype", "disabled",
                        "custname", "updated", "sortorder", "currency", "hideAccounts"},
                "_id=" + bankId, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    @Nullable
    public Cursor getBank(long bankId) {
        return getBank(Long.toString(bankId));
    }

    @Nullable
    public Cursor getAccount(String id) {
        Cursor c = mDb.query("accounts",
                new String[]{"id", "balance", "name", "bankid", "acctype", "hidden", "notify",
                        "currency", "aliasfor"}, "id='" + id + "'", null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }
}
