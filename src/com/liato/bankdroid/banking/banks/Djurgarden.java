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
package com.liato.bankdroid.banking.banks;

import android.content.Context;

import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.banks.sebkort.SEBKortBase;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;


public class Djurgarden extends SEBKortBase {
	private static final String TAG = "Djurgarden";
	private static final String NAME = "Djurgårdskortet MasterCard";
	private static final String NAME_SHORT = "djurgarden";
	private static final int BANKTYPE_ID = Bank.DJURGARDEN;

	public Djurgarden(Context context) {
		super(context, "djse", "0116");
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
	}
	
	public Djurgarden(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
		super(username, password, context, "djse", "0116");
	}

}
