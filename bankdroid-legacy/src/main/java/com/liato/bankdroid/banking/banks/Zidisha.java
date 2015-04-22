/* Copyright (C) 2012 Nullbyte <http://nullbyte.eu>
 * Zidisha support by Per Wigren <per.wigren@gmail.com>
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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class Zidisha extends Bank {

    private static final String TAG = "Zidisha";

    private static final String NAME = "Zidisha";

    private static final String NAME_SHORT = "zidisha";

    private static final String URL = "https://www.zidisha.org/";

    private static final int BANKTYPE_ID = IBankTypes.ZIDISHA;

    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_TEXT;

    private static final int INPUT_TYPE_PASSWORD = InputType.TYPE_CLASS_TEXT;

    private static final String INPUT_HINT_USERNAME = "Username";

    private static final boolean STATIC_BALANCE = true;

    String response;

    private Pattern reUserGuess = Pattern
            .compile("<input.*?name=\"user_guess\" value=\"([0-9a-f]+)\"", Pattern.DOTALL);

    private Pattern reAccounts = Pattern.compile(
            "Funds uploaded:</td>.*?USD ([^<]+).*Credit Available:.*?USD ([^<]+).*Amount Lent By Me:.*?USD ([^<]+).*Total Impact:.*?USD ([^<]+)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public Zidisha(Context context) {
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
        super.currency = "USD";
    }

    public Zidisha(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }


    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        urlopen = new Urllib(context,
                CertificateReader.getCertificates(context, R.raw.cert_zidisha));
        urlopen.setAllowCircularRedirects(true);
        response = urlopen.open("https://www.zidisha.org/");
        Matcher mUserGuess = reUserGuess.matcher(response);
        if (!mUserGuess.find()) {
            throw new BankException(
                    res.getText(R.string.unable_to_find).toString() + " user_guess.");
        }
        String user_guess = mUserGuess.group(1);

        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("username", getUsername()));
        postData.add(new BasicNameValuePair("password", getPassword()));
        postData.add(new BasicNameValuePair("textpassword", getUsername()));
        postData.add(new BasicNameValuePair("userlogin", ""));
        postData.add(new BasicNameValuePair("user_guess", user_guess));
        return new LoginPackage(urlopen, postData, response, "https://www.zidisha.org/process.php");
    }

    public Urllib login() throws LoginException, BankException, IOException {
        LoginPackage lp = preLogin();
        String response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
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
        Matcher matcher;
        response = urlopen.open("https://www.zidisha.org/index.php?p=19");
        matcher = reAccounts.matcher(response);

        while (matcher.find()) {
                        /*
             * 1: Funds uploaded
             * 2: Available for withdrawal
             * 3: Lent by me
             * 4: Total Impact
             */
            Account insattningar = new Account("Insättningar",
                    Helpers.parseBalance(matcher.group(1)), "insattningar");
            Account tillgangligt = new Account("Tillgängligt",
                    Helpers.parseBalance(matcher.group(2)), "tillgangligt");
            Account utlanat = new Account("Utlånat", Helpers.parseBalance(matcher.group(3)),
                    "utlanat");
            Account balans = new Account("Påverkan", Helpers.parseBalance(matcher.group(4)),
                    "impact");

            insattningar.setCurrency("USD");
            tillgangligt.setCurrency("USD");
            utlanat.setCurrency("USD");
            balans.setCurrency("USD");

            accounts.add(insattningar);
            accounts.add(tillgangligt);
            accounts.add(utlanat);
            accounts.add(balans);

            balance = balance.add(Helpers.parseBalance(matcher.group(4)));
        }
        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
    }
}
