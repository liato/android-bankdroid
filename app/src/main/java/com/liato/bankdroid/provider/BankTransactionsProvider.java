/*
 * Copyright (C) 2010 Magnusart <http://www.magnusart.com>
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

package com.liato.bankdroid.provider;

import com.liato.bankdroid.db.DatabaseHelper;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.preference.PreferenceManager;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

/**
 * <p>
 * This is the implementation of the BankTransactionsProvider. It provides
 * access to the transaction data for specific banks.
 * </p>
 *
 * @author Magnus Andersson
 * @see IBankTransactionsProvider
 * @since 8 jan 2011
 */
public class BankTransactionsProvider extends ContentProvider implements
        IBankTransactionsProvider {

    private static final String CONTENT_PROVIDER_ENABLED = "content_provider_enabled";

    private static final String CONTENT_PROVIDER_API_KEY = "content_provider_api_key";

    private final static int TRANSACTIONS = 0;

    private final static int BANK_ACCOUNTS = 1;

    private static final String WILD_CARD = "*";

    private static final String BANK_TABLE = "banks";

    private static final String ACCOUNT_TABLE = "accounts";

    private static final String BANK_ACCOUNT_TABLES = BANK_TABLE + " LEFT JOIN " + ACCOUNT_TABLE
            + " ON banks."
            + BANK_ID + " = accounts.bankid";

    private static final String TRANSACTIONS_TABLE = "transactions";

    private final static UriMatcher URI_MATCHER;

    private final static Map<String, String> BANK_ACCOUNT_PROJECTION_MAP;

    private final static Map<String, String> TRANS_PROJECTION_MAP;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY, TRANSACTIONS_CAT + "/" + WILD_CARD,
                TRANSACTIONS);
        URI_MATCHER.addURI(AUTHORITY, BANK_ACCOUNTS_CAT + "/" + WILD_CARD,
                BANK_ACCOUNTS);

        // Projections are "Poor mans views" of the data.
        BANK_ACCOUNT_PROJECTION_MAP = new HashMap<String, String>();

        // Must match bankAccountProjection in
        // IBankTransactionsProvider#bankAccountProjection
        BANK_ACCOUNT_PROJECTION_MAP.put(BANK_ID, BANK_ID);
        BANK_ACCOUNT_PROJECTION_MAP.put(BANK_NAME, BANK_NAME);
        BANK_ACCOUNT_PROJECTION_MAP.put(BANK_TYPE, BANK_TYPE);
        BANK_ACCOUNT_PROJECTION_MAP.put(BANK_LAST_UPDATED, BANK_LAST_UPDATED);
        BANK_ACCOUNT_PROJECTION_MAP.put(ACC_ID, ACC_ID);
        BANK_ACCOUNT_PROJECTION_MAP.put(ACC_NAME, ACC_NAME);
        // Table name has to be explicitly included here since Banks also have a column named balance.
        BANK_ACCOUNT_PROJECTION_MAP.put(ACC_BALANCE, ACCOUNT_TABLE + "." + ACC_BALANCE);
        BANK_ACCOUNT_PROJECTION_MAP.put(ACC_TYPE, ACC_TYPE);

        TRANS_PROJECTION_MAP = new HashMap<String, String>();

        // Must match transactionProjection in
        // IBankTransactionsProvider#transactionProjection
        TRANS_PROJECTION_MAP.put(TRANS_ID, TRANS_ID);
        TRANS_PROJECTION_MAP.put(TRANS_DATE, TRANS_DATE);
        TRANS_PROJECTION_MAP.put(TRANS_DESC, TRANS_DESC);
        TRANS_PROJECTION_MAP.put(TRANS_AMT, TRANS_AMT);
        TRANS_PROJECTION_MAP.put(TRANS_CUR, TRANS_CUR);
        TRANS_PROJECTION_MAP.put(TRANS_ACCNT, TRANS_ACCNT);
    }

    private DatabaseHelper dbHelper;

    public static String getApiKey(final Context ctx) {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(ctx);
        if (!prefs.getBoolean(CONTENT_PROVIDER_ENABLED, false)) {
            throw new IllegalStateException(
                    "Access to Content Provider is not enabled.");
        }

        final String apiKey = prefs.getString(CONTENT_PROVIDER_API_KEY, "");

        if (apiKey.equals("")) {
            throw new IllegalArgumentException("The API-Key must be set.");
        }

        return apiKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int delete(final Uri uri, final String selection,
            final String[] selectionArgs) {
        throw new UnsupportedOperationException(
                "This provider does not implement the delete method");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType(final Uri uri) {
        Timber.d("Got URI: %s", uri.toString());

        switch (URI_MATCHER.match(uri)) {
            case BANK_ACCOUNTS:
                return BANK_ACCOUNTS_MIME;
            case TRANSACTIONS:
                return TRANSACTIONS_MIME;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Uri insert(final Uri uri, final ContentValues values) {
        throw new UnsupportedOperationException(
                "This provider does not implement the insert method");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreate() {
        dbHelper = DatabaseHelper.getHelper(getContext());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor query(final Uri uri, final String[] projection,
            final String selection, final String[] selectionArgs,
            final String sortOrder) {

        if (!isApiKeyEnabled(getContext())) {
            return null;
        }

        final String apiKey = uri.getPathSegments().get(1);

        Timber.v("Trying to access database with %s", apiKey);

        if (!apiKey.startsWith(API_KEY, 0)) {
            return null;
            // throw new IllegalArgumentException(API_KEY +
            // "<API-KEY> must be a part of the URI!");
        }

        final String key = apiKey.replace(API_KEY, "");

        if (!key.equals(getApiKey(getContext()))) {
            return null;
            // throw new
            // IllegalAccessError("The supplied API_KEY does not exist");
        }

        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        SQLiteQueryBuilder qb;

        if (BANK_ACCOUNTS_MIME.equals(getType(uri))) {
            qb = new SQLiteQueryBuilder();
            qb.setTables(BANK_ACCOUNT_TABLES);
            qb.setProjectionMap(BANK_ACCOUNT_PROJECTION_MAP);
            qb.setDistinct(true);
        } else if (TRANSACTIONS_MIME.equals(getType(uri))) {
            qb = new SQLiteQueryBuilder();
            qb.setTables(TRANSACTIONS_TABLE);
            qb.setProjectionMap(TRANS_PROJECTION_MAP);
        } else {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        final Cursor cur = qb.query(db, projection, selection, selectionArgs,
                null, null, sortOrder);

        cur.setNotificationUri(getContext().getContentResolver(), uri);
        return cur;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int update(final Uri uri, final ContentValues values,
            final String selection, final String[] selectionArgs) {
        throw new UnsupportedOperationException(
                "This provider does not implement the update method");
    }

    private boolean isApiKeyEnabled(final Context ctx) {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(ctx);
        return prefs.getBoolean(CONTENT_PROVIDER_ENABLED, false);
    }
}
