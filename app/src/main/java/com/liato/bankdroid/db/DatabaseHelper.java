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

import com.liato.bankdroid.banking.LegacyProviderConfiguration;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import timber.log.Timber;

import static com.liato.bankdroid.db.Database.PROPERTY_CONNECTION_ID;
import static com.liato.bankdroid.db.Database.PROPERTY_KEY;
import static com.liato.bankdroid.db.Database.PROPERTY_TABLE_NAME;
import static com.liato.bankdroid.db.Database.PROPERTY_VALUE;

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
        db.execSQL(Database.TABLE_CONNECTION);
        db.execSQL(LegacyDatabase.TABLE_BANKS);
        db.execSQL(LegacyDatabase.TABLE_ACCOUNTS);
        db.execSQL(LegacyDatabase.TABLE_TRANSACTIONS);
        db.execSQL(Database.TABLE_CONNECTION_PROPERTIES);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
            final int newVersion) {
        Timber.d("Upgrading database from version %d to %d", newVersion, oldVersion);
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
        }
        if(oldVersion <= 12) {
            try {
                db.beginTransaction();
                db.execSQL(Database.TABLE_CONNECTION);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }

    private void migrateProperties(final SQLiteDatabase db) {
        String tempTable = LegacyDatabase.BANK_TABLE_NAME + "_temp";
        db.execSQL("ALTER TABLE " + LegacyDatabase.BANK_TABLE_NAME + " RENAME TO " + tempTable + ";");

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
}
