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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.text.Html;

import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.legacy.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.Transaction;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class ResursBank extends Bank {
    private Pattern reAccounts = Pattern.compile("kontonummer</td>\\s*<td>([^<]+)</td>\\s*</tr>\\s*<tr>\\s*<td[^>]+>Beviljad\\s*kredit</td>\\s*<td>([^<]+)</td>\\s*</tr>\\s*<tr>\\s*<td[^>]+>Utnyttjad\\s*kredit</td>\\s*<td>([^<]+)</td>\\s*</tr>\\s*<tr>\\s*<td[^>]+>Reserverat\\s*belopp</td>\\s*<td>([^<]+)</td>\\s*</tr>\\s*<tr>\\s*<td[^>]+>Kvar\\s*att\\s*utnyttja</td>\\s*<td>([^<]+)</td>", Pattern.CASE_INSENSITIVE);
    private Pattern reTransactions = Pattern.compile("<td>(\\d{4}-\\d{2}-\\d{2})</td>\\s*<td>([^<]+)</td>\\s*<td>([^<]*)</td>\\s*<td>([^<]+)</", Pattern.CASE_INSENSITIVE);

    private String response = null;

    public ResursBank(Context context) {
        super(context);
        super.TAG = "ResursBank";
        super.NAME = "Resurs Bank";
        super.NAME_SHORT = "resursbank";
        super.BANKTYPE_ID = IBankTypes.RESURSBANK;
        super.URL = "https://secure.resurs.se/internetbank/default.jsp";
    }

    public ResursBank(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
        urlopen = new Urllib(context, CertificateReader.getCertificates(context, R.raw.cert_resursbank));
        response = urlopen.open("https://secure.resurs.se/internetbank/default.jsp");
        List <NameValuePair> postData = new ArrayList <NameValuePair>();
        postData.add(new BasicNameValuePair("kontonummer", username));
        postData.add(new BasicNameValuePair("password", password));
        postData.add(new BasicNameValuePair("page", "privat"));
        return new LoginPackage(urlopen, postData, response, "https://secure.resurs.se/internetbank/login.jsp");
    }

    @Override
    public Urllib login() throws LoginException, BankException {
        try {
            LoginPackage lp = preLogin();
            response = urlopen.open(lp.getLoginTarget(), lp.getPostData());

            if (response.contains("vid inloggningen")) {
                throw new LoginException(res.getText(R.string.invalid_username_password).toString());
            }
        } catch (ClientProtocolException e) {
            throw new BankException(e.getMessage(), e);
        } catch (IOException e) {
            throw new BankException(e.getMessage(), e);
        }
        return urlopen;
    }

    @Override
    public void update() throws BankException, LoginException, BankChoiceException {
        super.update();
        if (username == null || password == null || username.length() == 0 || password.length() == 0) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }

        urlopen = login();
        Matcher matcher = reAccounts.matcher(response);
        while (matcher.find()) {
            /*
             * Capture groups:
             * GROUP                    EXAMPLE DATA
             * 1: Account number        0000000000000000
             * 2: Beviljad kredit       0,00 kr
             * 3: Utnyttjad kredit      0,00 kr
             * 4: Reserverat  belopp    0,00 kr
             * 5: Kvar att utnyttja     0,00 kr
             * 
             */
            String accountId = Html.fromHtml(matcher.group(1)).toString().trim().replaceAll("[^0-9]*", "");
            accounts.add(new Account("Beviljad kredit",
                    Helpers.parseBalance(matcher.group(2)),
                    "b_"+accountId));

            BigDecimal utnyttjad = Helpers.parseBalance(matcher.group(3));
            utnyttjad = utnyttjad.add(Helpers.parseBalance(matcher.group(4)));
            utnyttjad = utnyttjad.negate();
            accounts.add(new Account("Utnyttjad kredit",
                    utnyttjad,
                    "u_"+accountId));

            balance = balance.add(Helpers.parseBalance(matcher.group(3)));
            balance = balance.add(utnyttjad);
            accounts.add(new Account("Reserverat belopp",
                    Helpers.parseBalance(matcher.group(4)),
                    "r_"+accountId));
            accounts.add(new Account("Disponibelt",
                    Helpers.parseBalance(matcher.group(5)),
                    "k_"+accountId));
        }

        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
        super.updateComplete();
    }

    @Override
    public void updateTransactions(Account account, Urllib urlopen) throws LoginException, BankException {
        super.updateTransactions(account, urlopen);
        // Only update transactions for the main account
        if (!account.getId().startsWith("b_")) return;

        try {
            response = urlopen.open("https://secure.resurs.se/internetbank/kontoutdrag.jsp");
            Matcher matcher = reTransactions.matcher(response);
            ArrayList<Transaction> transactions = new ArrayList<Transaction>();
            while (matcher.find()) {
                /*
                 * Capture groups:
                 * GROUP                    EXAMPLE DATA
                 * 1: Date                  2010-04-17
                 * 2: Transaction           ONOFF L+äNNA
                 * 3: Currency              always null?
                 * 4: Amount                -95,00 kr 
                 * 
                 */
                transactions.add(new Transaction(matcher.group(1),
                        Html.fromHtml(matcher.group(2)).toString().trim(),
                        Helpers.parseBalance(matcher.group(4))));
            }
            account.setTransactions(transactions);
        } catch (ClientProtocolException e) {
            throw new BankException(e.getMessage(), e);
        } catch (IOException e) {
            throw new BankException(e.getMessage(), e);
        }
    }
}