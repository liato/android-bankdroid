/*
 * Copyright (C) 2013 Nullbyte <http://nullbyte.eu>
 * Contributors: mhagander
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
package com.liato.bankdroid.banking.banks;

import android.content.Context;

import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.banks.sebkort.SEBKortBase;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;


public class EurobonusMastercardDk extends SEBKortBase {
	private static final String TAG = "EurobonusMastercardDk";
	private static final String NAME = "SAS EuroBonus MasterCard (Denmark)";
	private static final String NAME_SHORT = "ebmaster_dk";
	private static final int BANKTYPE_ID = Bank.SASEUROBONUSMASTERCARD_DK;

	public EurobonusMastercardDk(Context context) {
		super(context, "sadk", "0101");
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
	}

	public EurobonusMastercardDk(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
		super(username, password, context, "sadk", "0101");
	}

}
