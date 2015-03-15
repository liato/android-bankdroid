/*
 * Copyright (C) 2013 Nullbyte <http://nullbyte.eu>
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

package com.liato.bankdroid.banking.banks.bitcoin;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.banks.bitcoin.model.BlockchainResponse;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.legacy.R;
import com.liato.bankdroid.provider.IBankTypes;

import android.content.Context;
import android.text.TextUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;

import eu.nullbyte.android.urllib.Urllib;

public class Bitcoin extends Bank {

    private static final String TAG = "Bitcoin";

    private static final String NAME = "Bitcoin";

    private static final String NAME_SHORT = "bitcoin";

    private static final String URL = "http://blockchain.info";

    private static final int BANKTYPE_ID = IBankTypes.BITCOIN;

    private static final boolean STATIC_BALANCE = false;

    private static final boolean INPUT_HIDDEN_PASSWORD = true;

    private static final int INPUT_TITLETEXT_USERNAME = R.string.bitcoin_address;

    private static final String API_URL = "http://blockchain.info/rawaddr/";

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00000000 ");

    public Bitcoin(Context context) {
        super(context);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
        super.STATIC_BALANCE = STATIC_BALANCE;
        super.currency = "BTC";
        super.INPUT_HIDDEN_PASSWORD = INPUT_HIDDEN_PASSWORD;
        super.INPUT_TITLETEXT_USERNAME = INPUT_TITLETEXT_USERNAME;
    }

    public Bitcoin(String username, String password, Context context)
            throws BankException, LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }

    public Urllib login() throws LoginException, BankException, IOException {
        urlopen = new Urllib(context);

        try {
            String response = urlopen.open(API_URL + username);
            if (response == null || "".equals(response)) {
                throw new LoginException(res.getText(
                        R.string.invalid_username_password).toString());
            }
            ObjectMapper vObjectMapper = new ObjectMapper();
            vObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            vObjectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
            BlockchainResponse r = vObjectMapper.readValue(
                    urlopen.open(API_URL + username), BlockchainResponse.class);
            Account a = new Account("Bitcoin",
                    new BigDecimal(r.getFinalBalance()).divide(BigDecimal.valueOf(100000000)), "1");
            a.setCurrency("BTC");
            accounts.add(a);
            setCurrency("BTC");
        } catch (JsonParseException e) {
            throw new BankException(res.getText(
                    R.string.invalid_bitcoin_address).toString());
        }

        return urlopen;
    }

    @Override
    public void update() throws BankException, LoginException,
            BankChoiceException, IOException {
        super.update();
        if (TextUtils.isEmpty(username)) {
            throw new LoginException(res.getText(
                    R.string.invalid_bitcoin_address).toString());
        }
        login();
        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
        super.updateComplete();
    }

    @Override
    public DecimalFormat getDecimalFormatter() {
        return DECIMAL_FORMAT;
    }
}
