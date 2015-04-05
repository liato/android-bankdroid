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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @since 8 jan 2011
 */
final public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper instance;

    private DatabaseHelper(final Context context) {
        super(context, LegacyDatabase.DATABASE_NAME, null,
                LegacyDatabase.DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getHelper(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context);
        }
        return instance;
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL(LegacyDatabase.TABLE_BANKS);
        db.execSQL(LegacyDatabase.TABLE_ACCOUNTS);
        db.execSQL(LegacyDatabase.TABLE_TRANSACTIONS);
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
    }
}
