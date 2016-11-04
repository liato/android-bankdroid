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
import com.liato.bankdroid.banking.Transaction;
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

public class FirstCard extends Bank {

    private static final String NAME = "First Card";

    private static final String URL = "https://www.firstcard.se/login.jsp";

    private static final int BANKTYPE_ID = IBankTypes.FIRSTCARD;

    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;

    private static final String INPUT_HINT_USERNAME = "ÅÅMMDDXXXX";

    private Pattern reAccounts = Pattern.compile(
            "translist\\.jsp\\?p=a&(?:amp;)?cardID=([^\"]+)\">([^<]+)</a>\\s*</td>\\s*<td[^>]+>([^<]+)</td>",
            Pattern.CASE_INSENSITIVE);

    private Pattern reTransactions = Pattern.compile(
            "pagecolumns\">(\\d{6})</td>\\s*<td>\\s*</td>\\s*<td>([^<]+)</td>\\s*<td[^>]+>([^<]+)</td>\\s*<td[^>]+>([^<]+)</td>\\s*<td[^>]+>([^<]+)<",
            Pattern.CASE_INSENSITIVE);

    private String response = null;

    public FirstCard(Context context) {
        super(context, R.drawable.logo_firstcard);
        super.url = URL;
        super.inputTypeUsername = INPUT_TYPE_USERNAME;
        super.inputHintUsername = INPUT_HINT_USERNAME;
    }

    @Override
    public int getBanktypeId() {
        return BANKTYPE_ID;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public FirstCard(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }


    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        urlopen = new Urllib(context,
                CertificateReader.getCertificates(context, R.raw.cert_firstcard));
        response = urlopen.open("https://www.firstcard.se/login.jsp");
        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("op", "login"));
        postData.add(new BasicNameValuePair("errorpage", "login.jsp"));
        postData.add(new BasicNameValuePair("pnr", getUsername()));
        postData.add(new BasicNameValuePair("intpwd", getPassword()));
        return new LoginPackage(urlopen, postData, null, "https://www.firstcard.se/login.jsp");
    }

    @Override
    public Urllib login() throws LoginException, BankException, IOException {
        LoginPackage lp = preLogin();
        response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
        if (response.contains("Logga in med din kod")) {
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

        response = urlopen.open("https://www.firstcard.se/mkol/index.jsp");
        Matcher matcher = reAccounts.matcher(response);
        while (matcher.find()) {
            /*
             * Capture groups:
             * GROUP                EXAMPLE DATA
             * 1: id                kdKPq4ghlcy9wpXymSzzS46wWQcS_0OT
             * 2: account number    1111 3333 7777 9999
             * 3: amount            9 824,08
             *
             */
            accounts.add(new Account(Html.fromHtml(matcher.group(2)).toString().trim(),
                    Helpers.parseBalance(matcher.group(3)), matcher.group(1).trim()));
            balance = balance.add(Helpers.parseBalance(matcher.group(3)));
        }

        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }

        super.updateComplete();
    }

    @Override
    public void updateTransactions(Account account, Urllib urlopen) throws LoginException,
            BankException, IOException {
        super.updateTransactions(account, urlopen);

        response = urlopen.open(
                "https://www.firstcard.se/mkol/translist.jsp?p=a&cardID=" + account.getId());
        Matcher matcher = reTransactions.matcher(response);
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        while (matcher.find()) {
            /*
             * Capture groups:
             * GROUP                        EXAMPLE DATA
             * 1: date                      101006
             * 2: specification             GOOGLE *RealArcade
             * 3: currency                  USD
             * 4: amount                    3,49
             * 5: amount in local currency  24,08
             *
             */
            String strDate = Html.fromHtml(matcher.group(1)).toString().trim();
            strDate = "20" + strDate.charAt(0) + strDate.charAt(1) + "-" + strDate.charAt(2)
                    + strDate.charAt(3) + "-" + strDate.charAt(4) + strDate.charAt(5);
            transactions.add(new Transaction(strDate,
                    Html.fromHtml(matcher.group(2)).toString().trim(),
                    Helpers.parseBalance(matcher.group(5)).negate()));
        }
        account.setTransactions(transactions);
    }
}
