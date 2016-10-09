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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class Osuuspankki extends Bank {

    private static final String TAG = "Osuuspankki";

    private static final String NAME = "Osuuspankki";

    private static final String NAME_SHORT = "osuuspankki";

    private static final String URL = "https://www.op.fi/op?kielikoodi=sv";

    private static final int BANKTYPE_ID = IBankTypes.OSUUSPANKKI;

    private Pattern reAccounts = Pattern.compile(
            "href=\"\\?id=(\\d{1,})&(?:amp;)?tilinro=([^&]+)&[^>]+>([^<]+)</a>\\s*<br\\s?/>\\s*<span[^>]+>\\s*<b>([^<]+)</b>([^<]+)</span>");

    private Pattern reTransactions = Pattern.compile(
            "<tr[^>]*>\\s*<td>\\s*<div\\s*class=\"Ensimmainen\">\\s*(\\d{2}\\.\\d{2})\\.<br.?/>\\s*\\s*(\\d{2}\\.\\d{2})\\.\\s*</div>\\s*</td>\\s*<td>\\s*<div>([^<]+)<br.?/>.*?</div>\\s*</td>\\s*<td>\\s*<div\\s*class=\"Nowrap\">\\s*<a[^>]+>([^<]+)<br.?/>\\s*</a>.*?</div>\\s*</td>\\s*<td[^>]+>\\s*<div[^>]*>([^<]+)</div>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private String response = null;

    public Osuuspankki(Context context) {
        super(context, R.drawable.logo_osuuspankki);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
    }

    public Osuuspankki(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        urlopen = new Urllib(context, CertificateReader.getCertificates(context,
                R.raw.cert_osuuspankki, R.raw.cert_osuuspankki_mobile));
        response = urlopen.open("https://www.op.fi/op?kielikoodi=sv");
        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("REQUEST_LOGIN_ATTEMPTED", "true"));
        postData.add(new BasicNameValuePair("REQUEST_PREVIOUS_QUERYSTRING", ""));
        postData.add(new BasicNameValuePair("x", "24"));
        postData.add(new BasicNameValuePair("y", "5"));
        postData.add(new BasicNameValuePair("USERNAME", getUsername()));
        postData.add(new BasicNameValuePair("PWD", getPassword()));

        return new LoginPackage(urlopen, postData, response, "https://www.op.fi/op?kielikoodi=sv");
    }

    @Override
    public Urllib login() throws LoginException, BankException, IOException {
        LoginPackage lp = preLogin();
        response = urlopen.open(lp.getLoginTarget(), lp.getPostData());

        if (response.contains("du nya koder genom att bes")) {
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
        Matcher matcher;
        matcher = reAccounts.matcher(response);
        while (matcher.find()) {
            /*
             * Capture groups:
             * GROUP                    EXAMPLE DATA
             * 1: Account type          12401 | 12701
             * 2: Account id            ecb_5f0e0dfcbc1e8aabe4f5ab85e3382266
             * 3: Account name          FI91 5553 5140 0165 27
             * 4: Amount                +882,35
             * 5: Currency              &nbsp;&euro;
             *
             */
            String currency = Helpers.parseCurrency(
                    Html.fromHtml(matcher.group(5)).toString().trim(), "EUR");
            Account account = new Account(Html.fromHtml(matcher.group(3)).toString().trim(),
                    Helpers.parseBalance(matcher.group(4)), matcher.group(2).trim());
            account.setCurrency(currency);
            //Bonuskonto
            if ("12701".equals(matcher.group(1))) {
                account.setType(Account.OTHER);
            }
            this.setCurrency(currency);
            accounts.add(account);
            balance = balance.add(Helpers.parseBalance(matcher.group(4)));
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

        Matcher matcher;

        response = urlopen.open(String.format("https://www.op.fi/?id=%s&tilinro=%s&ecb=1&srcpl=4",
                (account.getType() == Account.OTHER ? "12701" : "12401"), account.getId()));
        matcher = reTransactions.matcher(response);
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        while (matcher.find()) {
            /*
             * Capture groups:
             * GROUP                    EXAMPLE DATA
             * 1: Book. date            21.01
             * 2: Trans. date           20.01
             * 3: Description           ITUNES-EURO LUXEMBOURG
             * 4: Transaction type      BANKKORTSBET.
             * 5: Amount in EUR         -3,99
             *
             */
            String[] date = Html.fromHtml(matcher.group(2)).toString().trim().split("\\.");
            Transaction transaction = new Transaction(Helpers.getTransactionDate(date[1], date[0]),
                    Html.fromHtml(matcher.group(3)).toString().trim(),
                    Helpers.parseBalance(matcher.group(5)));
            transaction.setCurrency(account.getCurrency());
            transactions.add(transaction);
        }
        account.setTransactions(transactions);
    }
}
