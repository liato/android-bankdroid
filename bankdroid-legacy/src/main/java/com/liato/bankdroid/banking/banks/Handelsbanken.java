/*
 * Copyright (C) 2010 Nullbyte <http://nullbyte.eu>
 * Contributors: DEGE
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class Handelsbanken extends Bank {

    private static final String NAME = "Handelsbanken";

    private static final String NAME_SHORT = "handelsbanken";

    private static final String URL = "https://m.handelsbanken.se/";

    private static final int BANKTYPE_ID = IBankTypes.HANDELSBANKEN;

    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_TEXT;

    private static final String INPUT_HINT_USERNAME = "ÅÅMMDDXXXX";

    private Pattern reBalance = Pattern.compile(
            "block-link\\s*\"\\s*href=\"[^\"]*?/primary/_-([^\"]+)\"><span>([^<]+)</span>.*?SEK(?:&nbsp;|\\s*)?([0-9\\s.,-ÃÂ]+)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private Pattern reAccountsUrl = Pattern
            .compile("_-([^\"]+)\">(?:<img[^>]+>)?<img[^>]+><span[^>]+>Konton<",
                    Pattern.CASE_INSENSITIVE);

    private Pattern reLoginUrl = Pattern
            .compile("_-([^\"]+)\">(?:<img[^>]+>)?<img[^>]+><span[^>]+>Logga",
                    Pattern.CASE_INSENSITIVE);

    private Pattern reTransactions = Pattern.compile(
            "padding-left\">([^<]+)</span><span[^>]*><span[^>]*>([^<]+)</span><span[^>]*>([^<]+)<",
            Pattern.CASE_INSENSITIVE);

    private ArrayList<String> accountIds = new ArrayList<String>();

    private String response = null;

    public Handelsbanken(Context context) {
        super(context, R.drawable.logo_handelsbanken);
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
        super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
    }

    public Handelsbanken(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }


    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        urlopen = new Urllib(context,
                CertificateReader.getCertificates(context, R.raw.cert_handelsbanken));
        response = urlopen.open("https://m.handelsbanken.se/primary/");
        Matcher matcher = reLoginUrl.matcher(response);
        if (!matcher.find()) {
            throw new BankException(
                    res.getText(R.string.unable_to_find).toString() + " login url.");
        }
        String strLoginUrl = "https://m.handelsbanken.se/primary/_-" + matcher.group(1);
        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("username", getUsername()));
        postData.add(new BasicNameValuePair("pin", getPassword()));
        postData.add(new BasicNameValuePair("execute", "true"));
        return new LoginPackage(urlopen, postData, response, strLoginUrl);
    }

    @Override
    public Urllib login() throws LoginException, BankException, IOException {
        LoginPackage lp = preLogin();
        response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
        if (response.contains("ontrollera dina uppgifter")) {
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

        Matcher matcher = reAccountsUrl.matcher(response);
        if (!matcher.find()) {
            throw new BankException(
                    res.getText(R.string.unable_to_find).toString() + " accounts url.");
        }
        String strAccountsUrl = "https://m.handelsbanken.se/primary/_-" + matcher.group(1);
        response = urlopen.open(strAccountsUrl);
        matcher = reBalance.matcher(response);
        Integer accountId = 0;
        while (matcher.find()) {
            accounts.add(new Account(Html.fromHtml(matcher.group(2)).toString().trim(),
                    Helpers.parseBalance(matcher.group(3).trim()), accountId.toString()));
            balance = balance.add(Helpers.parseBalance(matcher.group(3)));
            accountIds.add(matcher.group(1));
            accountId += 1;
        }
        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
        super.updateComplete();
    }


    public void updateTransactions(Account account, Urllib urlopen) throws LoginException,
            BankException, IOException {
        super.updateTransactions(account, urlopen);
        Matcher matcher;
        String accountWebId = accountIds.get(Integer.parseInt(account.getId()));
        response = urlopen.open("https://m.handelsbanken.se/primary/_-" + accountWebId);
        matcher = reTransactions.matcher(response);
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        while (matcher.find()) {
            transactions.add(new Transaction(matcher.group(1).trim(),
                    Html.fromHtml(matcher.group(2)).toString().trim(),
                    Helpers.parseBalance(matcher.group(3))));
        }

        // Sort transactions by date
        Collections.sort(transactions, new Comparator<Transaction>() {
            public int compare(Transaction t1, Transaction t2) {
                return t2.compareTo(t1);
            }
        });
        account.setTransactions(transactions);
    }
}
