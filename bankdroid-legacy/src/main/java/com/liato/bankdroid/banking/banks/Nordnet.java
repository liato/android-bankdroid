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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.content.Context;
import android.text.Html;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class Nordnet extends Bank {

    private static final String NAME = "Nordnet";

    private static final String URL = "https://www.nordnet.se/mux/login/startSE.html";

    private static final int BANKTYPE_ID = IBankTypes.NORDNET;

    private Pattern reAccounts =
            Pattern.compile(
                    "<span class=\"bullet\">·<\\/span>\\n\\t\\t\\t\\t\\t\\t<span>(.*?)<\\/span>");

    private Pattern reBalance = Pattern.compile(
            "<div class=\\\"value\\\">\\n(.*?)\\n\\t\\t<\\/div>");

    private String response = null;

    public Nordnet(Context context) {
        super(context, R.drawable.logo_nordnet);

        super.url = URL;
    }

    @Override
    public int getBanktypeId() {
        return BANKTYPE_ID;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public Nordnet(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }


    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        urlopen = new Urllib(context,
                CertificateReader.getCertificates(context, R.raw.cert_nordnet));
        urlopen.setContentCharset(HTTP.ISO_8859_1);
        response = urlopen.open("https://www.nordnet.se/mux/login/startSE.html");

        Document d = Jsoup.parse(response);
        Element e = d.getElementById("input1");
        if (e == null || "".equals(e.attr("name"))) {
            throw new BankException(
                    res.getText(R.string.unable_to_find).toString() + " username field.");
        }
        String loginFieldName = e.attr("name");
        e = d.getElementById("pContHidden");
        if (e == null || "".equals(e.attr("name"))) {
            throw new BankException(
                    res.getText(R.string.unable_to_find).toString() + " password field.");
        }
        String loginFieldPassword = e.attr("name");

        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("checksum", ""));
        postData.add(new BasicNameValuePair("referer", ""));
        postData.add(new BasicNameValuePair("encryption", "0"));
        postData.add(new BasicNameValuePair(loginFieldName, getUsername()));
        postData.add(new BasicNameValuePair(loginFieldPassword, getPassword()));

        return new LoginPackage(urlopen, postData, response,
                "https://www.nordnet.se/mux/login/login.html");
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
        if (getUsername().isEmpty() || getPassword().isEmpty()) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }
        urlopen = login();
        Matcher accountMatcher = reAccounts.matcher(response);
        Matcher balanceMatcher = reBalance.matcher(response);
        while (accountMatcher.find()) {
            /*
             * Capture groups:
             * GROUP                EXAMPLE DATA
             * 1: Account name and number      Investeringssparkonto 1234567   | Sparkonto 1234 567890 1
             *
             */
            if (balanceMatcher.find()) {
                /*
                * Capture groups:
                * GROUP                EXAMPLE DATA
                * 1: Account balance     62 356 | 0
                *
                */
                Account account = new Account(Html.fromHtml(accountMatcher.group(1)).toString().trim(),
                        Helpers.parseBalance(balanceMatcher.group(1)),
                        Html.fromHtml(accountMatcher.group(1)).toString().trim().replaceAll(" ", ""));

                // Saving accounts contain white space characters in the account number
                if (!accountMatcher.group(1).trim().contains(" ")) {
                    account.setType(Account.FUNDS);
                }
                accounts.add(account);
                balance = balance.add(Helpers.parseBalance(balanceMatcher.group(1)));
            }
        }

        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
        super.updateComplete();
    }
}
