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

import java.math.BigDecimal;

/**
 * <p>
 * This interface provides constants that can conveniently be used with the
 * BankTransactionProvider. Copy this file and use it in your
 * ContentProviderClient code.
 * </p>
 * <p>
 * Uri format for querying for all transaction of a given bank/account
 * combination:<br/>
 * <code><b>content://{{@link #AUTHORITY}}/{{@link #TRANSACTIONS_CAT}}</code>
 * </b>
 * </p>
 * 
 * <p>
 * <b>Example of reading transactions from a View that implements this
 * interface:</b>
 * 
 * <pre>
 * final Uri uri = Uri.parse(&quot;content://&quot; + AUTHORITY + &quot;/&quot; + TRANSACTIONS_CAT);
 * final Cursor cur = managedQuery(uri, TRANSACTIONS_PROJECTION,
 * 		ACCOUNT_SELECTION_FILTER, new String[] { currentAccountId }, null);
 * startManagingCursor(cur);
 * </pre>
 * 
 * Where <code>currentAccountId</code> is the account ID you wish to filter on.
 * </p>
 * 
 * @author Magnus Andersson
 * @since 8 jan 2011
 * @version 1.0-RC1
 * @see BankTransactionsProvider
 */
public interface IBankTransactionsProvider {
	/**
	 * <p>
	 * The authority part of the URI.
	 * </p>
	 */
	String AUTHORITY = "com.liato.bankdroid.provider.BankTransactionsProvider";

	// ====================================MIME-TYPES======================================
	/**
	 * <p>
	 * MIME-type for Transactions. Not use today (no inserts) added for clarity.
	 * </p>
	 */
	String TRANSACTIONS_MIME = "vnd.android.cursor.dir/vnd.bankdroid.transactions";

	/**
	 * <p>
	 * MIME-type for Bank/Account. Not use today (no inserts) added for clarity.
	 * </p>
	 */
	String BANK_ACCOUNTS_MIME = "vnd.android.cursor.dir/vnd.bankdroid.bankaccounts";

	// ===================================CATEGORIES=======================================
	/**
	 * <p>
	 * A category part of the URI.
	 * </p>
	 */
	String BANK_ACCOUNTS_CAT = "bankaccounts";

	/**
	 * <p>
	 * A category part of the URI.
	 * </p>
	 */
	String TRANSACTIONS_CAT = "transactions";

	// ===================================BANK/ACCOUNT_FIELDS==============================
	/**
	 * <p>
	 * ID for Bank.
	 * </p>
	 * <b>PLEASE NOTE</b><br>
	 * This is an BankDroid internal id, do not depend on this id for keeping
	 * track between sessions.</p>
	 */
	String BANK_ID = "_id";

	/**
	 * <p>
	 * User defined custom name for the Bank
	 * </p>
	 */
	String BANK_NAME = "custname";

	/**
	 * <p>
	 * Type of Bank.
	 * </p>
	 * 
	 * @see IBankTypes
	 */
	String BANK_TYPE = "banktype";

	/**
	 * <p>
	 * Last time the bank and the bank accounts were synchronized.
	 * </p>
	 */
	String BANK_LAST_UPDATED = "updated";

	/**
	 * <p>
	 * Account id, this is a composite key that
	 * </p>
	 * <b>PLEASE NOTE</b><br>
	 * This is an BankDroid internal id, do not depend on this id for keeping
	 * track between sessions.</p>
	 * 
	 * @See {@link #TRANS_ACCNT}, {@link #ACCOUNT_SELECTION_FILTER}
	 * 
	 */
	String ACC_ID = "id";

	/**
	 * <p>
	 * Name of the account.
	 * </p>
	 */
	String ACC_NAME = "name";

	/**
	 * <p>
	 * The account Type.
	 * </p>
	 * 
	 * @See {@link IAccountTypes}
	 */
	String ACC_TYPE = "acctype";

	/**
	 * Defines if an account is hidden.
	 */
	String ACC_HIDDEN = "hidden";

	/**
	 * <p>
	 * The projection (ie. db view) that works with the
	 * {@link #BANK_ACCOUNTS_CAT} category.
	 * </p>
	 */
	String[] BANK_ACCOUNT_PROJECTION = { BANK_ID, BANK_NAME, BANK_TYPE,
			BANK_LAST_UPDATED, ACC_ID, ACC_NAME, ACC_TYPE };

	/**
	 * <p>
	 * Use this filter to ignore hidden accounts (Hidden accounts only used for
	 * debug purposes). Always use this filter or incorporate it into your own
	 * filters.
	 * </p>
	 */
	String NO_HIDDEN_ACCOUNTS_FILTER = ACC_HIDDEN + " = 0";

	// ===================================TRANSACTION_FIELDS===============================
	/**
	 * <p>
	 * Transaction ID.
	 * </p>
	 * <p>
	 * <b>PLEASE NOTE</b><br>
	 * This is an BankDroid internal id, not the actual bank transaction id.
	 * </p>
	 */
	String TRANS_ID = "_id";

	/**
	 * <p>
	 * Date of Transaction.
	 * </p>
	 */
	String TRANS_DATE = "transdate";

	/**
	 * <p>
	 * Description text of Transaction.
	 * </p>
	 */
	String TRANS_DESC = "btransaction";

	/**
	 * <p>
	 * Amount of Transaction.
	 * </p>
	 * <p>
	 * String representation of a {@link BigDecimal}. Positive for <i>Income</i>
	 * and negative for <i>Expenses</i>.
	 * </p>
	 */
	String TRANS_AMT = "amount";

	/**
	 * <p>
	 * Currency of the Transaction. (Currently Only SEK)
	 * </p>
	 */
	String TRANS_CUR = "currency";

	/**
	 * <p>
	 * The account a transaction belongs to.
	 * </p>
	 */
	String TRANS_ACCNT = "account";

	/**
	 * <p>
	 * The projection (ie. db view) that works with the
	 * {@link #TRANSACTIONS_CAT} category.
	 * </p>
	 */
	String[] TRANSACTIONS_PROJECTION = { TRANS_ID, TRANS_DATE, TRANS_DESC,
			TRANS_AMT, TRANS_CUR, TRANS_ACCNT };

	/**
	 * <p>
	 * Use this filter to only return transactions belonging to a certain
	 * account.
	 * </p>
	 * 
	 * <p>
	 * The format for the composite bank/account ID is {BANK_ID}_{ACCOUNT_ID}.<br>
	 * <b>Example IDs:</b> 1_1 or 1_0
	 * </p>
	 * 
	 * @See {@link #ACC_ID}
	 */
	String ACCOUNT_SELECTION_FILTER = TRANS_ACCNT + " = ?";
}
