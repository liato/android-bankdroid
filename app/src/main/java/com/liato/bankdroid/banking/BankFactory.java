/*
 * Copyright (C) 2014 Nullbyte <http://nullbyte.eu>
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

package com.liato.bankdroid.banking;

import com.crashlytics.android.answers.CustomEvent;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.db.Crypto;
import com.liato.bankdroid.db.DBAdapter;
import com.liato.bankdroid.db.Database;
import com.liato.bankdroid.db.DatabaseHelper;
import com.liato.bankdroid.utils.LoggingUtils;

import net.sf.andhsli.hotspotlogin.SimpleCrypto;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class BankFactory {

    private static Bank fromBanktypeId(int id, Context context) throws BankException {
        return LegacyBankFactory.fromBanktypeId(id, context);
    }


    public static List<Bank> listBanks(Context context) {
        return LegacyBankFactory.listBanks(context);
    }

    @Nullable
    public static Bank bankFromDb(long id, Context context, boolean loadAccounts) {
        Bank bank = null;
        DBAdapter db = new DBAdapter(context);
        Cursor c = db.getBank(id);

        if (c != null && c.getCount() > 0) {
            try {
                bank = fromBanktypeId(c.getInt(c.getColumnIndex("banktype")), context);
                bank.setProperties(loadProperties(id, context));

                bank.setData(new BigDecimal(c.getString(c.getColumnIndex("balance"))),
                        (c.getInt(c.getColumnIndex("disabled")) != 0),
                        c.getLong(c.getColumnIndex("_id")),
                        c.getString(c.getColumnIndex("currency")),
                        c.getString(c.getColumnIndex("custname")),
                        c.getInt(c.getColumnIndex("hideAccounts")));
                if (loadAccounts) {
                    bank.setAccounts(accountsFromDb(context, bank.getDbId()));
                }
            } catch (BankException e) {
                Timber.w(e, "Failed getting bank from database");
            } finally {
                c.close();
            }
        }
        return bank;
    }

    public static ArrayList<Bank> banksFromDb(Context context, boolean loadAccounts) {
        ArrayList<Bank> banks = new ArrayList<>();
        DBAdapter db = new DBAdapter(context);
        Cursor c = db.fetchBanks();
        if (c == null) {
            return banks;
        }
        try {
            while (!c.isLast() && !c.isAfterLast()) {
                c.moveToNext();
                try {
                    Bank bank = fromBanktypeId(c.getInt(c.getColumnIndex("banktype")), context);
                    long id = c.getLong(c.getColumnIndex("_id"));
                    bank.setProperties(loadProperties(id, context));
                    bank.setData(new BigDecimal(c.getString(c.getColumnIndex("balance"))),
                            (c.getInt(c.getColumnIndex("disabled")) != 0),
                            id,
                            c.getString(c.getColumnIndex("currency")),
                            c.getString(c.getColumnIndex("custname")),
                            c.getInt(c.getColumnIndex("hideAccounts")));
                    if (loadAccounts) {
                        bank.setAccounts(accountsFromDb(context, bank.getDbId()));
                    }
                    banks.add(bank);
                } catch (BankException e) {
                    Timber.w(e, "BankFactory.banksFromDb()");
                }
            }
        } finally {
            c.close();
        }
        return banks;
    }

    @Nullable
    public static Account accountFromDb(Context context, String accountId,
            boolean loadTransactions) {
        DBAdapter db = new DBAdapter(context);
        Cursor c = db.getAccount(accountId);

        if (c == null || c.isClosed() || (c.isBeforeFirst() && c.isAfterLast())) {
            return null;
        }

        Account account = new Account(c.getString(c.getColumnIndex("name")),
                new BigDecimal(c.getString(c.getColumnIndex("balance"))),
                c.getString(c.getColumnIndex("id")).split("_", 2)[1],
                c.getLong(c.getColumnIndex("bankid")),
                c.getInt(c.getColumnIndex("acctype")));
        account.setHidden(c.getInt(c.getColumnIndex("hidden")) == 1);
        account.setNotify(c.getInt(c.getColumnIndex("notify")) == 1);
        account.setCurrency(c.getString(c.getColumnIndex("currency")));
        account.setAliasfor(c.getString(c.getColumnIndex("aliasfor")));
        c.close();
        if (loadTransactions) {
            ArrayList<Transaction> transactions = new ArrayList<>();
            String fromAccount = accountId;
            if (account.getAliasfor() != null && account.getAliasfor().length() > 0) {
                fromAccount = Long.toString(account.getBankDbId()) + "_" + account.getAliasfor();
            }
            c = db.fetchTransactions(fromAccount);
            if (!(c == null || c.isClosed() || (c.isBeforeFirst() && c.isAfterLast()))) {
                while (!c.isLast() && !c.isAfterLast()) {
                    c.moveToNext();
                    transactions.add(new Transaction(c.getString(c.getColumnIndex("transdate")),
                            c.getString(c.getColumnIndex("btransaction")),
                            new BigDecimal(c.getString(c.getColumnIndex("amount"))),
                            c.getString(c.getColumnIndex("currency"))));
                }
                c.close();
            }
            account.setTransactions(transactions);
        }
        return account;
    }

    private static ArrayList<Account> accountsFromDb(Context context, long bankId) {
        ArrayList<Account> accounts = new ArrayList<>();
        DBAdapter db = new DBAdapter(context);
        Cursor c = db.fetchAccounts(bankId);
        if (c == null) {
            return accounts;
        }
        try {
            while (!c.isLast() && !c.isAfterLast()) {
                c.moveToNext();
                try {
                    Account account = new Account(c.getString(c.getColumnIndex("name")),
                            new BigDecimal(c.getString(c.getColumnIndex("balance"))),
                            c.getString(c.getColumnIndex("id")).split("_", 2)[1],
                            c.getLong(c.getColumnIndex("bankid")),
                            c.getInt(c.getColumnIndex("acctype")));
                    account.setHidden(c.getInt(c.getColumnIndex("hidden")) == 1);
                    account.setNotify(c.getInt(c.getColumnIndex("notify")) == 1);
                    account.setCurrency(c.getString(c.getColumnIndex("currency")));
                    account.setAliasfor(c.getString(c.getColumnIndex("aliasfor")));
                    accounts.add(account);
                } catch (ArrayIndexOutOfBoundsException e) {
                    // Probably an old Avanza account
                    Timber.w(e, "Attempted to load an account without an ID: %d", bankId);
                }
            }
        } finally {
            c.close();
        }
        return accounts;
    }

    private static Map<String, String> loadProperties(long bankId, Context context) {
        Map<String, String> properties = new HashMap<>();
        Map<String, String> decryptedProperties = new HashMap<>();
        DBAdapter db = new DBAdapter(context);
        Cursor c = db.fetchProperties(Long.toString(bankId));
        if(c == null) {
            return properties;
        }
        try {
            while (!c.isLast() && !c.isAfterLast()) {
                c.moveToNext();
                String key = c.getString(c.getColumnIndex(Database.PROPERTY_KEY));
                String value = c.getString(c.getColumnIndex(Database.PROPERTY_VALUE));
                if (LegacyProviderConfiguration.PASSWORD.equals(key)) {
                    try {
                        value = SimpleCrypto.decrypt(Crypto.getKey(), value);
                        decryptedProperties.put(key, value);
                    } catch (Exception e) {
                        Timber.i("%s %s",
                                "Failed decrypting bank properties.",
                                "This usually means they are unencrypted, which is exactly what we want them to be.");
                    }
                }
                properties.put(key, value);
            }
        } finally {
            c.close();
        }

        storeDecryptedProperties(context, bankId, decryptedProperties);

        return properties;
    }

    /**
     * Stores decrypted passwords on disk.
     * <p/>
     * This is a step in removing password encryption alltogether.
     * <p/>
     * The background is that it's broken on Androin Nougat anyway, and that it
     * didn't provide any extra security before that either.
     * <p/>
     * Since Bankdroid needs to send plain text passwords to the banks, it must
     * be possible to retrieve the plain text passwords automatically. And if the
     * passwords are encrypted on disk, Bankdroid needs to have the key. And if
     * Bankdroid stores both the key and the encrypted password on the phone, a
     * determined attacker could get both anyway, and the encryption is useless.
     * <p/>
     * The only thing the encryption has protected against is a using rooting
     * their own device and retrieving their own plain text passwords. This would
     * enable the attacker to reaa their own account balance from the bank. Which
     * they likely already could even before this change...
     */
    private static void storeDecryptedProperties(
            Context context, long bankId, Map<String, String> decryptedProperties)
    {
        if (decryptedProperties.isEmpty()) {
            return;
        }

        Timber.i("Storing %d decrypted properties...", decryptedProperties.size());
        SQLiteDatabase db = DatabaseHelper.getHelper(context).getWritableDatabase();
        for (Map.Entry<String, String> property : decryptedProperties.entrySet()) {
            String value = property.getValue();
            if (value != null && !value.isEmpty()) {
                ContentValues propertyValues = new ContentValues();
                propertyValues.put(Database.PROPERTY_KEY, property.getKey());
                propertyValues.put(Database.PROPERTY_VALUE, value);
                propertyValues.put(Database.PROPERTY_CONNECTION_ID, bankId);
                db.insertWithOnConflict(
                        Database.PROPERTY_TABLE_NAME, null, propertyValues,
                        SQLiteDatabase.CONFLICT_REPLACE);
            }
        }
        Timber.i("%d decrypted properties stored", decryptedProperties.size());

        LoggingUtils.logCustom(new CustomEvent("Passwords Decrypted"));
    }
}
