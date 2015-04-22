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

import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.legacy.R;
import com.liato.bankdroid.provider.IBankTypes;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.text.InputType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class Nordnetdirekt extends Bank {

    private static final String TAG = "Nordnetdirekt";

    private static final String NAME = "Nordnetdirekt";

    private static final String NAME_SHORT = "nordnetdirekt";

    private static final String URL
            = "https://www.nordnetdirekt.se/mux/oinloggad/startsida/index.html";

    private static final int BANKTYPE_ID = IBankTypes.NORDNETDIREKT;

    private static final int INPUT_TITLETEXT_EXTRAS = R.string.nordnetdirekt_extras_title;

    private static final int INPUT_TYPE_EXTRAS = InputType.TYPE_CLASS_TEXT
            | InputType.TYPE_TEXT_VARIATION_PASSWORD;

    private static final boolean INPUT_HIDDEN_EXTRAS = false;

    private Pattern reBalance = Pattern.compile(
            "left\">\\s*<table[^>]+>\\s*<caption[^>]+>([^<]+)</caption>\\s*<tr[^>]+>\\s*<td[^>]+>[^<]+</td>\\s*<td>([^<]+)</td>\\s*</tr>\\s*<tr[^>]+>\\s*<td[^>]+>[^<]+</td>\\s*<td>([^<]+)</td>");

    private String response = null;

    public Nordnetdirekt(Context context) {
        super(context);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
        super.INPUT_TITLETEXT_EXTRAS = INPUT_TITLETEXT_EXTRAS;
        super.INPUT_TYPE_EXTRAS = INPUT_TYPE_EXTRAS;
        super.INPUT_HIDDEN_EXTRAS = INPUT_HIDDEN_EXTRAS;
    }

    public Nordnetdirekt(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }


    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        urlopen = new Urllib(context,
                CertificateReader.getCertificates(context, R.raw.cert_nordnetdirekt));
        urlopen.setContentCharset(HTTP.ISO_8859_1);
        response = urlopen.open("https://www.nordnetdirekt.se/mux/oinloggad/startsida/index.html");

        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("a4", "sv"));
        postData.add(new BasicNameValuePair("a3", "ADSE"));
        postData.add(new BasicNameValuePair("usa", "7"));
        postData.add(new BasicNameValuePair("a1", getUsername()));
        postData.add(new BasicNameValuePair("a2", getPassword()));
        postData.add(new BasicNameValuePair("nyckel", getExtras()));
        return new LoginPackage(urlopen, postData, response,
                "https://www.nordnetdirekt.se/mux/inloggad/lib/login.html");
    }

    @Override
    public Urllib login() throws LoginException, BankException, IOException {
        LoginPackage lp = preLogin();
        response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
        if (response.contains("fel vid inloggningen")) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }

        return urlopen;
    }

    @Override
    public void update() throws BankException, LoginException, BankChoiceException, IOException {
        super.update();
        if (getUsername() == null || getPassword() == null || getUsername().length() == 0
                || getPassword().length() == 0) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }
        urlopen = login();
        Matcher matcher = reBalance.matcher(response);
        if (matcher.find()) {
            /*
             * Capture groups:
             * GROUP                EXAMPLE DATA
             * 1: Currency          Dep&aring;v&auml;rde - SEK
             * 2: Kontantsaldo      13 264,53
             * 3: Värdepapper       111 909,05
             *
             */
            accounts.add(new Account(
                    "Kontosaldo",
                    Helpers.parseBalance(matcher.group(2)),
                    "1"));
            accounts.add(new Account(
                    "Värdepapper",
                    Helpers.parseBalance(matcher.group(3)),
                    "2"));
            balance = balance.add(Helpers.parseBalance(matcher.group(2)));
            balance = balance.add(Helpers.parseBalance(matcher.group(3)));
        }
        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
        super.updateComplete();
    }
}
