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

import java.util.HashMap;
import java.util.Map;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.liato.bankdroid.db.DatabaseHelper;

/**
 * <p>
 * This is the implementation of the BankTransactionsProvider. It provides
 * access to the transaction data for specific banks.
 * </p>
 * 
 * @author Magnus Andersson
 * @since 8 jan 2011
 * @see IBankTransactionsProvider
 */
public class BankTransactionsProvider extends ContentProvider implements
		IBankTransactionsProvider {

	private final static int TRANSACTIONS = 0;
	private static final int BANK_ACCOUNTS = 1;

	private static final String BANK_ACCOUNT_TABLES = "banks, accounts";
	private static final String TRANSACTIONS_TABLE = "transactions";

	private DatabaseHelper dbHelper;
	private final static UriMatcher uriMatcher;
	private final static Map<String, String> bankAccountProjectionMap;
	private final static Map<String, String> transProjectionMap;

	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, TRANSACTIONS_CAT, TRANSACTIONS);
		uriMatcher.addURI(AUTHORITY, BANK_ACCOUNTS_CAT, BANK_ACCOUNTS);

		// Projections are "Poor mans views" of the data.
		bankAccountProjectionMap = new HashMap<String, String>();

		// Must match bankAccountProjection in
		// IBankTransactionsProvider#bankAccountProjection
		bankAccountProjectionMap.put(BANK_ID, BANK_ID);
		bankAccountProjectionMap.put(BANK_NAME, BANK_NAME);
		bankAccountProjectionMap.put(BANK_TYPE, BANK_TYPE);
		bankAccountProjectionMap.put(BANK_LAST_UPDATED, BANK_LAST_UPDATED);
		bankAccountProjectionMap.put(ACC_ID, ACC_ID);
		bankAccountProjectionMap.put(ACC_NAME, ACC_NAME);
		bankAccountProjectionMap.put(ACC_TYPE, ACC_TYPE);

		transProjectionMap = new HashMap<String, String>();

		// Must match transactionProjection in
		// IBankTransactionsProvider#transactionProjection
		transProjectionMap.put(TRANS_ID, TRANS_ID);
		transProjectionMap.put(TRANS_DATE, TRANS_DATE);
		transProjectionMap.put(TRANS_DESC, TRANS_DESC);
		transProjectionMap.put(TRANS_AMT, TRANS_AMT);
		transProjectionMap.put(TRANS_CUR, TRANS_CUR);
		transProjectionMap.put(TRANS_ACCNT, TRANS_ACCNT);
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
		switch (uriMatcher.match(uri)) {
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
		dbHelper = new DatabaseHelper(getContext());
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Cursor query(final Uri uri, final String[] projection,
			final String selection, final String[] selectionArgs,
			final String sortOrder) {

		final SQLiteDatabase db = dbHelper.getReadableDatabase();
		SQLiteQueryBuilder qb;
		if (BANK_ACCOUNTS_MIME.equals(getType(uri))) {
			qb = new SQLiteQueryBuilder();
			qb.setTables(BANK_ACCOUNT_TABLES);
			qb.setProjectionMap(bankAccountProjectionMap);
		} else if (TRANSACTIONS_MIME.equals(getType(uri))) {
			qb = new SQLiteQueryBuilder();
			qb.setTables(TRANSACTIONS_TABLE);
			qb.setProjectionMap(transProjectionMap);
		} else {
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		/*
		 * Select Statement to build: SELECT banks._id, banks.custname,
		 * banks.banktype, banks.updated, accounts.id, accounts.name,
		 * accounts.acctype FROM banks, accounts WHERE banks._id =
		 * accounts.bankid AND accounts.hidden = 0;
		 */
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

}
