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
import android.text.InputType;

import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import eu.nullbyte.android.urllib.Urllib;

public class BlekingeTrafiken extends Bank {
    private static final String TAG = "Blekingetrafiken";
    private static final String NAME = "Blekingetrafiken";
    private static final String NAME_SHORT = "blekingetrafiken";
    private static final String URL = "http://www.blekingetrafiken.se/webshop/FService/FBalanceService.svc/card/balance/";
    private static final int BANKTYPE_ID = IBankTypes.BLEKINGETRAFIKEN;
    private String response = null;

    public BlekingeTrafiken(Context context) {
        super(context);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
        super.INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;
        super.INPUT_HINT_USERNAME = "XXXXXXXXXX";
        super.INPUT_TITLETEXT_USERNAME = R.string.card_number;
        super.INPUT_HIDDEN_PASSWORD = true;

    }

    public BlekingeTrafiken(String username, String password, Context context)
            throws BankException, LoginException, BankChoiceException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException,
            IOException {
        urlopen = new Urllib(context);
        urlopen.addHeader("Content-Type","application/json;charset=UTF-8");
        urlopen.addHeader("Accept","application/json");
        return new LoginPackage(urlopen, null , null, URL);
    }

    public Urllib login() throws LoginException, BankException {
        try {
            LoginPackage lp = preLogin();
            HttpResponse httpResponse = urlopen.openAsHttpResponse(URL,
                    new StringEntity("{\"cardnr\":" + username + "}"), true);
            if(httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new LoginException(res.getText(R.string.invalid_card_number).toString());
            }
            response = EntityUtils.toString(httpResponse.getEntity());
        } catch (ClientProtocolException e) {
            throw new BankException(e.getMessage());
        } catch (IOException e) {
            throw new BankException(e.getMessage());
        }
        return urlopen;
    }

    @Override
    public void update() throws BankException, LoginException, BankChoiceException {
        super.update();
        if (username == null || username.length() == 0) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }
        urlopen = login();
        try {
            JSONObject accountJSONObject = new JSONObject(response).optJSONObject("Card");
            accountJSONObject = accountJSONObject.optJSONObject("Value");

            Account a = new Account(accountJSONObject.getString("Description"),
                    Helpers.parseBalance(accountJSONObject.getString("Remaining")),
                    "0");
            accounts.add(a);
            balance = balance.add(a.getBalance());

            accountJSONObject = accountJSONObject.optJSONObject("Autoload");
            if (accountJSONObject !=  null) {
                a = new Account(" - Kommande -",
                        Helpers.parseBalance(accountJSONObject.getString("Value")),
                        "1");
                accounts.add(a);
                balance = balance.add(a.getBalance());
            }

        } catch (JSONException e) {
            throw new BankException(e.getMessage());
        }
        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
        super.updateComplete();
    }
}
