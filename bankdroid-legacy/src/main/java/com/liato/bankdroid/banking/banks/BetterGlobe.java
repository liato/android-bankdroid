/* Copyright (C) 2012 Nullbyte <http://nullbyte.eu>
 * BetterGlobe support by Per Wigren <per.wigren@gmail.com>
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
import android.text.InputType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.nullbyte.android.urllib.Urllib;

public class BetterGlobe extends Bank {

    private static final String TAG = "BetterGlobe";

    private static final String NAME = "Better Globe";

    private static final String NAME_SHORT = "betterglobe";

    private static final String URL = "http://betterglobe.com";

    private static final int BANKTYPE_ID = IBankTypes.BETTERGLOBE;

    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_TEXT;

    private static final int INPUT_TYPE_PASSWORD = InputType.TYPE_CLASS_TEXT;

    private static final String INPUT_HINT_USERNAME = "AID code";

    private static final boolean STATIC_BALANCE = true;

    private Pattern reBalance = Pattern.compile("Totalt på BG-kontot\\s*([^<]+)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private Pattern reForecast = Pattern.compile(
            "Din totala bruttovinst:.*?€([^<]+).*Köpekostnader:.*?€([^<]+).*Din totala nettovinst:.*?€([^<]+)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private Pattern reTrees = Pattern.compile("Totalt? antal ägda träd:</td>\\s*<td.*?>(\\d+)</td>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public BetterGlobe(Context context) {
        super(context);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
        super.INPUT_TYPE_PASSWORD = INPUT_TYPE_PASSWORD;
        super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
        super.STATIC_BALANCE = STATIC_BALANCE;
        super.currency = "EUR";
    }

    public BetterGlobe(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }


    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        urlopen = new Urllib(context);
        urlopen.setAllowCircularRedirects(true);
        HashMap<String, String> headers = urlopen.getHeaders();
        headers.put("Referer", "http://betterglobe.com/login.aspx?lang=sv-SE");
        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("username", getUsername()));
        postData.add(new BasicNameValuePair("password", getPassword()));
        postData.add(new BasicNameValuePair("btnLogin", ""));
        return new LoginPackage(urlopen, postData, "",
                "http://betterglobe.com/Login.aspx?rememberMe=False");
    }

    public Urllib login() throws LoginException, BankException, IOException {
        LoginPackage lp = preLogin();
        String response = urlopen.open(lp.getLoginTarget(), lp.getPostData());

        return urlopen;
    }

    @Override
    public void update() throws BankException, LoginException, BankChoiceException, IOException {
        super.update();
        if (getUsername().isEmpty() || getPassword().isEmpty()) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }

        urlopen = login();
        Matcher matcher;
        String response = urlopen.open("http://betterglobe.com/bgaccount.aspx/report");
        matcher = reBalance.matcher(response);

        while (matcher.find()) {
            Account tillgangligt = new Account("Tillgängligt",
                    Helpers.parseBalance(matcher.group(1)), "tillgangligt");
            tillgangligt.setCurrency("EUR");
            accounts.add(tillgangligt);
            balance = balance.add(Helpers.parseBalance(matcher.group(1)));
        }

        response = urlopen.open("http://betterglobe.com/mytrees.aspx/Valueforecast");
        matcher = reForecast.matcher(response);

        while (matcher.find()) {
            Account inkop = new Account("Inköp", Helpers.parseBalance(matcher.group(2)), "inkop");
            Account netto = new Account("Beräknad vinst", Helpers.parseBalance(matcher.group(3)),
                    "netto");
            Account brutto = new Account("Beräknat slutvärde",
                    Helpers.parseBalance(matcher.group(1)), "brutto");
            inkop.setCurrency("EUR");
            brutto.setCurrency("EUR");
            netto.setCurrency("EUR");
            accounts.add(inkop);
            accounts.add(brutto);
            accounts.add(netto);
            balance = balance.add(Helpers.parseBalance(matcher.group(2)));
        }

        response = urlopen.open("http://betterglobe.com/mytrees.aspx");
        matcher = reTrees.matcher(response);

        while (matcher.find()) {
            Account trees = new Account("Innehav", Helpers.parseBalance(matcher.group(1)), "trees");
            trees.setCurrency("träd");
            accounts.add(trees);
        }

        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
    }
}
