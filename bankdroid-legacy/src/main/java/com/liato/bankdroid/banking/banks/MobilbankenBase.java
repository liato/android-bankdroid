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

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

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

public abstract class MobilbankenBase extends Bank {

    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;

    private static final int INPUT_TYPE_PASSWORD = InputType.TYPE_CLASS_PHONE;

    private static final String INPUT_HINT_USERNAME = "ÅÅÅÅMMDDXXXX";

    protected String targetId;

    private Pattern reTimestamp = Pattern.compile("name=\"user(\\d{1,})\"");

    private Pattern reMsisdn = Pattern.compile("name=\"msisdn\"\\s*value=\"([^\"]+)\" />");

    private Pattern reAccounts = Pattern.compile(
            "accountmovement\\.html\\?account_no=([^\"]+)\">([^<]+)</a></td>\\s*</tr>\\s*<tr>\\s*<td[^>]+>([^<]+)</td>\\s*</tr>\\s*<tr>\\s*<td[^>]+><span[^>]+>([^<]+)</span></td>\\s*</tr>\\s*<tr>\\s*<td[^>]+>[^<]+<span[^>]+>([^<]+)</");

    private Pattern reTransactions = Pattern.compile(
            "<a[^>]+>([^<]+)</a>\\s*</div>\\s*<table[^>]+>\\s*<tr>\\s*<td[^>]+>\\s*</td>\\s*<td[^>]+>\\s*<span[^>]+>([^<]+)</span>\\s*</td>\\s*</tr>\\s*</table>\\s*<div[^>]+>\\s*(\\d{4}\\.\\d{2}\\.\\d{2})\\s*</div>\\s*");

    private String response = null;


    public MobilbankenBase(Context context) {
        super(context);
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
        super.INPUT_TYPE_PASSWORD = INPUT_TYPE_PASSWORD;
        super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
    }

    public MobilbankenBase(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        urlopen = new Urllib(context,
                CertificateReader.getCertificates(context, R.raw.cert_mobilbanken));
        urlopen.setAllowCircularRedirects(true);
        urlopen.setContentCharset(HTTP.ISO_8859_1);
        String postUrl = String.format("https://mobil-banken.se/%s/login.html", targetId);
        response = urlopen.open(postUrl);
        Matcher matcher = reMsisdn.matcher(response);
        if (!matcher.find()) {
            throw new BankException(res.getText(R.string.unable_to_find).toString() + " msisdn.");
        }
        String msisdn = matcher.group(1);
        matcher = reTimestamp.matcher(response);
        if (!matcher.find()) {
            throw new BankException(
                    res.getText(R.string.unable_to_find).toString() + " timestamp.");
        }
        String timestamp = matcher.group(1);

        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("msisdn", msisdn));
        postData.add(new BasicNameValuePair("user" + timestamp, getUsername()));
        postData.add(new BasicNameValuePair("password" + timestamp, getPassword()));
        return new LoginPackage(urlopen, postData, response, postUrl);
    }

    @Override
    public Urllib login() throws LoginException, BankException, IOException {
        LoginPackage lp = preLogin();
        response = urlopen.open(lp.getLoginTarget(), lp.getPostData());

        if (response.contains("eller pinkod")) {
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
             * 1: ID                ?
             * 2: Name              Sparkonto
             * 3: Account number    9570 012.345.678-9 SEK
             * 4: ?                 xxxxxxxxxxx
             * 5: Amount            ?
             *   
             */
            accounts.add(new Account(
                    Html.fromHtml(matcher.group(2)).toString().trim(),
                    Helpers.parseBalance(matcher.group(5)),
                    matcher.group(1).trim()));
            balance = balance.add(Helpers.parseBalance(matcher.group(5)));
        }

        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
    }

    @Override
    public void updateTransactions(Account account, Urllib urlopen) throws LoginException,
            BankException, IOException {
        super.updateTransactions(account, urlopen);

        Matcher matcher;
        response = urlopen.open(String
                .format("https://mobil-banken.se/%s/accountmovement.html?account_no=%s", targetId,
                        account.getId()));
        matcher = reTransactions.matcher(response);
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        while (matcher.find()) {
            /*
             * Capture groups:
             * GROUP                    EXAMPLE DATA
             * 1: Transaction           Kortköp QPARKSTOCKHOLM,  STOCKHOLM
             * 2: Amount                -40,00
             * 3: Date                  2010.12.23
             *
             */
            transactions.add(new Transaction(
                    matcher.group(3).trim().replace(".", "-"),
                    Html.fromHtml(matcher.group(1)).toString().trim(),
                    Helpers.parseBalance(matcher.group(2))));
        }
        account.setTransactions(transactions);
    }
}
