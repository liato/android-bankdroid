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

import com.liato.bankdroid.banking.LegacyBankHelper;
import com.liato.bankdroid.banking.LegacyProviderConfiguration;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.liato.bankdroid.db.Database.*;

/**
 * @since 8 jan 2011
 */
final public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper instance;

    private DatabaseHelper(final Context context) {
        super(context, Database.DATABASE_NAME, null,
                Database.DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getHelper(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context);
        }
        return instance;
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        try {
            db.beginTransaction();
            db.execSQL(TABLE_CONNECTION);
            db.execSQL(TABLE_CONNECTION_PROPERTIES);
            db.execSQL(TABLE_ACCOUNTS);
            db.execSQL(TABLE_ACCOUNT_PROPERTIES);
            db.execSQL(TABLE_TRANSACTIONS);
            db.execSQL(TABLE_EQUITIES);
            db.execSQL(TABLE_PAYMENTS);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
            final int newVersion) {
        Log.w(DBAdapter.TAG, "Upgrading database from version " + oldVersion
                + " to " + newVersion + ", which will destroy all old data");
        // Version <= 1.7.2 
        if (oldVersion <= 9) {
            // Add an "extras" field to the bank and and "alias for" field to the account.
            db.execSQL("ALTER TABLE " + LegacyDatabase.BANK_TABLE_NAME + " ADD " +
                    LegacyDatabase.BANK_EXTRAS + " text;");
            db.execSQL("ALTER TABLE " + LegacyDatabase.ACCOUNT_TABLE_NAME + " ADD " +
                    LegacyDatabase.ACCOUNT_ALIAS_FOR + " text;");
        }
        if (oldVersion <= 10) {
            db.execSQL("ALTER TABLE " + LegacyDatabase.BANK_TABLE_NAME + " ADD " +
                    LegacyDatabase.BANK_HIDE_ACCOUNTS + " integer;");
        }
        if(oldVersion <= 11) {
            try {
                db.beginTransaction();
                db.execSQL(Database.TABLE_CONNECTION_PROPERTIES);
                migrateProperties(db);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } if(oldVersion <= 12) {
            try {
                db.beginTransaction();
                migrateBanks(db);
                migrateAccounts(db);
                migrateTransactions(db);
                migratePropertiesWithNewTableReference(db);
                db.execSQL(Database.TABLE_EQUITIES);
                db.execSQL(Database.TABLE_PAYMENTS);
                db.execSQL(TABLE_ACCOUNT_PROPERTIES);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }

    private void migrateProperties(final SQLiteDatabase db) {
        String tempTable = LegacyDatabase.BANK_TABLE_NAME + "_temp";
        db.execSQL(
                "ALTER TABLE " + LegacyDatabase.BANK_TABLE_NAME + " RENAME TO " + tempTable + ";");

        // Drop username, password and extras fields from bank table
        db.execSQL(LegacyDatabase.TABLE_BANKS);
        db.execSQL("INSERT INTO " + LegacyDatabase.BANK_TABLE_NAME + " SELECT "
            + LegacyDatabase.BANK_ID + ","
            + LegacyDatabase.BANK_BALANCE + ","
            + LegacyDatabase.BANK_TYPE + ","
            + LegacyDatabase.BANK_CUSTOM_NAME + ","
            + LegacyDatabase.BANK_UPDATED + ","
            + LegacyDatabase.BANK_SORT_ORDER + ","
            + LegacyDatabase.BANK_CURRENCY + ","
            + LegacyDatabase.BANK_DISABLED + ","
            + LegacyDatabase.BANK_HIDE_ACCOUNTS + " FROM " + tempTable);

        // Add username, password and extras fields to properties table.
        Cursor c = db.query(tempTable, null, null, null,null,null,null);
        if (!(c == null || c.isClosed() || (c.isBeforeFirst() && c.isAfterLast()))) {
            while (!c.isLast() && !c.isAfterLast()) {
                c.moveToNext();
                long id = c.getLong(c.getColumnIndex(LegacyDatabase.BANK_ID));

                ContentValues usernameProperty = new ContentValues();
                usernameProperty.put(PROPERTY_CONNECTION_ID, id);
                usernameProperty.put(PROPERTY_KEY, LegacyProviderConfiguration.USERNAME);
                usernameProperty.put(PROPERTY_VALUE, c.getString(c.getColumnIndex(LegacyDatabase.BANK_USERNAME)));
                db.insert(PROPERTY_TABLE_NAME, null, usernameProperty);

                ContentValues passwordProperty = new ContentValues();
                passwordProperty.put(PROPERTY_CONNECTION_ID, id);
                passwordProperty.put(PROPERTY_KEY, LegacyProviderConfiguration.PASSWORD);
                passwordProperty.put(PROPERTY_VALUE, c.getString(c.getColumnIndex(LegacyDatabase.BANK_PASSWORD)));
                db.insert(PROPERTY_TABLE_NAME, null, passwordProperty);

                String extras = c.getString(c.getColumnIndex(LegacyDatabase.BANK_EXTRAS));
                if(extras != null && !extras.isEmpty()) {
                    ContentValues extrasProperty = new ContentValues();
                    extrasProperty.put(PROPERTY_CONNECTION_ID, id);
                    extrasProperty.put(PROPERTY_KEY, LegacyProviderConfiguration.EXTRAS);
                    extrasProperty.put(PROPERTY_VALUE, extras);
                    db.insert(PROPERTY_TABLE_NAME, null, extrasProperty);
                }
            }
            c.close();
        }
        db.execSQL("DROP TABLE " + tempTable);
    }

    private void migrateBanks(final SQLiteDatabase db) {
        db.execSQL(Database.TABLE_CONNECTION);

        Cursor c = db.query(LegacyDatabase.BANK_TABLE_NAME, null, null, null,null,null,null);
        if (!(c == null || c.isClosed() || (c.isBeforeFirst() && c.isAfterLast()))) {
            while (!c.isLast() && !c.isAfterLast()) {
                c.moveToNext();
                long id = c.getLong(c.getColumnIndex(LegacyDatabase.BANK_ID));
                String name = c.getString(c.getColumnIndex(LegacyDatabase.BANK_CUSTOM_NAME));
                int bankId = c.getInt(c.getColumnIndex(LegacyDatabase.BANK_TYPE));
                int enabled = c.getInt(c.getColumnIndex(LegacyDatabase.BANK_DISABLED)) == 1 ? 0 : 1;
                String lastUpdated = c.getString(c.getColumnIndex(LegacyDatabase.BANK_UPDATED));
                int sortOrder = c.getInt(c.getColumnIndex(LegacyDatabase.BANK_SORT_ORDER));

                ContentValues connection = new ContentValues();
                connection.put(Database.CONNECTION_ID, id);
                connection.put(Database.CONNECTION_NAME, name);
                connection.put(Database.CONNECTION_PROVIDER_ID, LegacyBankHelper.getReferenceFromLegacyId(bankId));
                connection.put(Database.CONNECTION_ENABLED, enabled);
                connection.put(Database.CONNECTION_LAST_UPDATED, lastUpdated);
                connection.put(Database.CONNECTION_SORT_ORDER, sortOrder);
                db.insert(CONNECTION_TABLE_NAME, null, connection);

            }
            c.close();
        }
        db.execSQL("DROP TABLE " + LegacyDatabase.BANK_TABLE_NAME);
    }

    private void migrateAccounts(SQLiteDatabase db) {
        String tempTable = LegacyDatabase.ACCOUNT_TABLE_NAME + "_temp";
        db.execSQL(
                "ALTER TABLE " + LegacyDatabase.ACCOUNT_TABLE_NAME + " RENAME TO " + tempTable + ";");

        db.execSQL(Database.TABLE_ACCOUNTS);

        Cursor c = db.query(tempTable, null, null, null,null,null,null);
        if (!(c == null || c.isClosed() || (c.isBeforeFirst() && c.isAfterLast()))) {
            while (!c.isLast() && !c.isAfterLast()) {
                c.moveToNext();

                String id = c.getString(c.getColumnIndex(LegacyDatabase.ACCOUNT_ID));
                int bankId = c.getInt(c.getColumnIndex(LegacyDatabase.ACCOUNT_BANK_ID));
                int hidden = c.getInt(c.getColumnIndex(LegacyDatabase.ACCOUNT_HIDDEN));
                String balance = c.getString(c.getColumnIndex(LegacyDatabase.ACCOUNT_BALANCE));
                String currency = c.getString(c.getColumnIndex(LegacyDatabase.ACCOUNT_CURRENCY));
                String name = c.getString(c.getColumnIndex(LegacyDatabase.ACCOUNT_NAME));
                int type = c.getInt(c.getColumnIndex(LegacyDatabase.ACCOUNT_TYPE));
                String notifications = c.getString(c.getColumnIndex(LegacyDatabase.ACCOUNT_NOTIFY));

                ContentValues account = new ContentValues();
                account.put(Database.ACCOUNT_ID, id.split("_")[1]);
                account.put(Database.ACCOUNT_CONNECTION_ID, LegacyBankHelper.getReferenceFromLegacyId(bankId));
                account.put(Database.ACCOUNT_BALANCE, balance);
                account.put(Database.ACCOUNT_CURRENCY, currency);
                account.put(Database.ACCOUNT_NAME, name);
                account.put(Database.ACCOUNT_NOTIFICATIONS_ENABLED, notifications);
                account.put(Database.ACCOUNT_HIDDEN, hidden);
                account.put(Database.ACCOUNT_TYPE, LegacyBankHelper.fromLegacyAccountType(type));
                db.insert(ACCOUNTS_TABLE_NAME, null, account);
            }
            c.close();
        }
        db.execSQL("DROP TABLE " + tempTable);
    }

    private void migrateTransactions(SQLiteDatabase db) {
        String tempTable = LegacyDatabase.TRANSACTION_TABLE_NAME + "_temp";
        db.execSQL(
                "ALTER TABLE " + LegacyDatabase.TRANSACTION_TABLE_NAME + " RENAME TO " + tempTable
                        + ";");

        db.execSQL(Database.TABLE_TRANSACTIONS);

        Cursor c = db.query(tempTable, null, null, null,null,null,null);
        if (!(c == null || c.isClosed() || (c.isBeforeFirst() && c.isAfterLast()))) {
            while (!c.isLast() && !c.isAfterLast()) {
                c.moveToNext();

                int id = c.getInt(c.getColumnIndex(LegacyDatabase.TRANSACTION_ID));
                String[] parentIds = c.getString(c.getColumnIndex(LegacyDatabase.TRANSACTION_ACCOUNT_ID)).split(
                        "_");
                String amount = c.getString(c.getColumnIndex(LegacyDatabase.TRANSACTION_AMOUNT));
                String currency = c.getString(c.getColumnIndex(LegacyDatabase.TRANSACTION_CURRENCY));
                String date  = c.getString(c.getColumnIndex(LegacyDatabase.TRANSACTION_DATE));
                String description = c.getString(c.getColumnIndex(LegacyDatabase.TRANSACTION_DESCRIPTION));

                ContentValues transaction = new ContentValues();
                transaction.put(Database.TRANSACTION_ID, Integer.toString(id));
                transaction.put(Database.TRANSACTION_CONNECTION_ID, parentIds[0]);
                transaction.put(Database.TRANSACTION_ACCOUNT_ID, parentIds[1]);
                transaction.put(Database.TRANSACTION_AMOUNT, amount);
                transaction.put(Database.TRANSACTION_CURRENCY, currency);
                transaction.put(Database.TRANSACTION_DESCRIPTION, description);
                transaction.put(Database.TRANSACTION_DATE, date);
                db.insert(TRANSACTIONS_TABLE_NAME, null, transaction);

            }
            c.close();
        }
        db.execSQL("DROP TABLE " + tempTable);
    }

    private void migratePropertiesWithNewTableReference(SQLiteDatabase db) {
        String tempTable = Database.PROPERTY_TABLE_NAME + "_temp";
        db.execSQL(
                "ALTER TABLE " + Database.PROPERTY_TABLE_NAME + " RENAME TO " + tempTable
                        + ";");

        db.execSQL(Database.TABLE_CONNECTION_PROPERTIES);
        db.execSQL("INSERT INTO " + Database.TABLE_CONNECTION_PROPERTIES + " SELECT "
                + PROPERTY_CONNECTION_ID + ","
                + PROPERTY_KEY + ","
                + PROPERTY_VALUE
                + " FROM " + tempTable);
        db.execSQL("DROP TABLE " + tempTable);
    }
}
