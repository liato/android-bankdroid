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

import android.content.Context;
import android.text.Html;
import android.text.InputType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class SevenDay extends Bank {

    private static final String TAG = "SevenDay";

    private static final String NAME = "SevenDay";

    private static final String NAME_SHORT = "sevenday";

    private static final String URL = "https://www.sevenday.se/mina-sidor/mina-sidor.htm";

    private static final int BANKTYPE_ID = IBankTypes.SEVENDAY;

    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;

    private static final String INPUT_HINT_USERNAME = "ÅÅMMDD-XXXX";


    private Pattern reViewState = Pattern.compile("ViewState\"\\s+value=\"([^\"]+)\"",
            Pattern.CASE_INSENSITIVE);

    private Pattern reAccounts = Pattern.compile(
            "'depositAccountNum':'([^=]+)=='[^>]+>([^<]+)</a></td>\\s*<td[^>]+>\\s*<span[^>]+>\\s*([0-9,]+)[^<]+</span>\\s*</td>\\s*<td[^>]+>\\s*<span[^>]+>\\s*([^<]+)<");

    private String response = null;

    public SevenDay(Context context) {
        super(context, R.drawable.logo_sevenday);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
        super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
    }

    public SevenDay(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }


    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        urlopen = new Urllib(context,
                CertificateReader.getCertificates(context, R.raw.cert_sevenday));
        response = urlopen.open("https://www.sevenday.se/mina-sidor/mina-sidor.htm");

        Matcher matcher = reViewState.matcher(response);
        if (!matcher.find()) {
            throw new BankException(
                    res.getText(R.string.unable_to_find).toString() + " ViewState.");
        }
        String viewState = matcher.group(1);

        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("loginForm", "loginForm"));
        postData.add(new BasicNameValuePair("login", "login"));
        postData.add(new BasicNameValuePair("javax.faces.ViewState", viewState));
        postData.add(new BasicNameValuePair("ssn", getUsername()));
        postData.add(new BasicNameValuePair("password", getPassword()));

        return new LoginPackage(urlopen, postData, response,
                "https://www.sevenday.se/mina-sidor/mina-sidor.htm");
    }

    @Override
    public Urllib login() throws LoginException, BankException, IOException {
        LoginPackage lp = preLogin();
        response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
        if (response.contains("Logga in med personnummer") || response.contains(
                "kommer automatiskt till startsidan")) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }
        return urlopen;
    }

    @Override
    public void update() throws BankException, LoginException, BankChoiceException, IOException {
        super.update();
        if (getUsername().isEmpty() || getPassword().isEmpty()) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }
        urlopen = login();

        Matcher matcher = reAccounts.matcher(response);
        while (matcher.find()) {
            /*
             * Capture groups:
             * GROUP                EXAMPLE DATA
             * 1: Account id        JigBFAUETrrqVKY+V4Dm3tcoY1n6Usa21IuHxa1BV7MnJT3T6rrGChDcDK0RSuM731uAeB/f9rvPRXRFYCCBcQ
             * 2: Account name      Sparkonto: XXX
             * 3: Interest          2,55
             * 4: Amount            10&nbsp;kr
             *
             */
            accounts.add(new Account(Html.fromHtml(matcher.group(2)).toString().trim(),
                    Helpers.parseBalance(matcher.group(4)),
                    Html.fromHtml(matcher.group(1)).toString().trim()));
            balance = balance.add(Helpers.parseBalance(matcher.group(4)));
        }

        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
        super.updateComplete();
    }
}
