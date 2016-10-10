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

import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.legacy.R;

import android.content.Context;

import java.io.IOException;


public class SparbankenOresund extends MobilbankenBase {

    private static final String TAG = "SparbankenOresund";

    private static final String NAME = "Sparbanken Öresund";

    private static final String NAME_SHORT = "sparbanken_oresund";

    private static final String URL = "https://mobil-banken.se/0003/login.html";

    private static final int BANKTYPE_ID = Bank.SPARBANKEN_ORESUND;

    public SparbankenOresund(Context context) {
        super(context, R.drawable.logo_sparbanken_oresund);

        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
        super.BROKEN = true;
        this.targetId = "0003";
    }

    public SparbankenOresund(String username, String password, Context context)
            throws BankException, LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }
}
