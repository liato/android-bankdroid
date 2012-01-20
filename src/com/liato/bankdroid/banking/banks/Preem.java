/*
 *  Copyright (C) 2010 Nullbyte <http://nullbyte.eu>
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

import java.util.regex.Pattern;

import android.content.Context;

import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;


public class Preem extends IkanoPartnerBase {
	private static final String TAG = "Preem";
	private static final String NAME = "Preem Privatkort";
	private static final String NAME_SHORT = "preem";
	private static final String URL = "https://partner.ikanobank.se/web/engines/page.aspx?structid=1437";
	private static final int BANKTYPE_ID = Bank.PREEM;
	private Pattern reTransactionsUrl = Pattern.compile("(page___\\d{1,}\\.aspx)\">(?:<span[^>]+>)?Mitt konto</", Pattern.CASE_INSENSITIVE);

    public Preem(Context context) {
        super(context);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
        super.reTransactionsUrl = reTransactionsUrl;
        this.structId = "1437";
    }

    public Preem(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
        this(context);
        this.update(username, password);
    }


}