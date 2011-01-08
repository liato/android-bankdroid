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
 * <code><b>content://{{@link #AUTHORITY}}/{{@link #TRANSACTIONS_CAT}}/BANK_ACCOUNT_ID</code>
 * </b>
 * </p>
 * 
 * @author Magnus Andersson
 * @since 8 jan 2011
 * @see BankTransactionsProvider
 */
public interface IBankTransactionsProvider {
	/**
	 * <p>
	 * The authority part of the URI.
	 * </p>
	 */
	String AUTHORITY = "com.liato.bankdroid.provider.BankTransactionsProvider";

	/**
	 * <p>
	 * MIME-type for Transactions. Not use today (no inserts) added for clarity.
	 * </p>
	 */
	String TRANSACTIONS_MIME = "vnd.android.cursor.dir/vnd.bankdroid.transactions";

	/**
	 * <p>
	 * A category part of the URI.
	 * </p>
	 */
	String TRANSACTIONS_CAT = "transactions";

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
	 * The projection (ie. 'view') that works with the {@link #TRANSACTIONS_CAT}
	 * category.
	 * </p>
	 */
	String[] TRANSACTIONS_PROJECTION = { TRANS_ID, TRANS_DATE, TRANS_DESC,
			TRANS_AMT, TRANS_CUR };
}
