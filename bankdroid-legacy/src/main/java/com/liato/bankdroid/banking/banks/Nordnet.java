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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.content.Context;
import android.text.Html;

import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.legacy.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class Nordnet extends Bank {
    private static final String TAG = "Nordnet";
    private static final String NAME = "Nordnet";
    private static final String NAME_SHORT = "nordnet";
    private static final String URL = "https://www.nordnet.se/mux/login/startSE.html";
    private static final int BANKTYPE_ID = IBankTypes.NORDNET;

    
    private Pattern reBalance = Pattern.compile("<a[^>]+>([^<]+)</a>\\s*<span\\s*class=\"bullet\">.*?</span>\\s*<span>([^\\d]+)([0-9 ]{1,})</span>\\s*</div>\\s*</div>\\s*<div\\s*class=\"value\">\\s*([0-9][^<]+)<");
    private String response = null;

    public Nordnet(Context context) {
        super(context);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
    }

    public Nordnet(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
        this(context);
        this.update(username, password);
    }

    
    @Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
        urlopen = new Urllib(context, CertificateReader.getCertificates(context, R.raw.cert_nordnet));
        urlopen.setContentCharset(HTTP.ISO_8859_1);
        response = urlopen.open("https://www.nordnet.se/mux/login/startSE.html");

        Document d = Jsoup.parse(response);
        Element e = d.getElementById("input1");
        if (e == null || "".equals(e.attr("name"))) {
            throw new BankException(res.getText(R.string.unable_to_find).toString()+" username field.");
        }
        String loginFieldName = e.attr("name");
        e = d.getElementById("pContHidden");
        if (e == null || "".equals(e.attr("name"))) {
            throw new BankException(res.getText(R.string.unable_to_find).toString()+" password field.");
        }
        String loginFieldPassword = e.attr("name");

        List <NameValuePair> postData = new ArrayList <NameValuePair>();
        postData.add(new BasicNameValuePair("checksum", ""));
        postData.add(new BasicNameValuePair("referer", ""));
        postData.add(new BasicNameValuePair("encryption", "0"));
        postData.add(new BasicNameValuePair(loginFieldName, username));
        postData.add(new BasicNameValuePair(loginFieldPassword, password));
        
        return new LoginPackage(urlopen, postData, response, "https://www.nordnet.se/mux/login/login.html");
    }

    @Override
    public Urllib login() throws LoginException, BankException {
        try {
            LoginPackage lp = preLogin();
            response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
            if (response.contains("fel vid inloggningen")) {
                throw new LoginException(res.getText(R.string.invalid_username_password).toString());
            }
        }
        catch (ClientProtocolException e) {
            throw new BankException(e.getMessage(), e);
        }
        catch (IOException e) {
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
        try {
            Matcher matcher;
            matcher = reBalance.matcher(response);
            while (matcher.find()) {
                /*
                 * Capture groups:
                 * GROUP                EXAMPLE DATA
                 * 1: Name              Efternamnet Förnamnet | Sparkonto
                 * 2: Account name      Aktie- och fonddepå   | Sparkonto
                 * 3: Account number    1234567               | 1234 567890 1
                 * 4: Amount            31 337                | 123
                 *  
                 */
                Account account = new Account(Html.fromHtml(matcher.group(2)).toString().trim() + " "
                        + Html.fromHtml(matcher.group(3)).toString().trim(),
                        Helpers.parseBalance(matcher.group(4)),
                        Html.fromHtml(matcher.group(3)).toString().trim().replaceAll(" ", ""));

                // Saving accounts contain white space characters in the account number
                if (!matcher.group(3).trim().contains(" ")) {
                    account.setType(Account.FUNDS);
                }
                accounts.add(account);
                balance = balance.add(Helpers.parseBalance(matcher.group(4)));
            }

            if (accounts.isEmpty()) {
                throw new BankException(res.getText(R.string.no_accounts_found).toString());
            }
        }
        finally {
            super.updateComplete();
        }
    }
}
