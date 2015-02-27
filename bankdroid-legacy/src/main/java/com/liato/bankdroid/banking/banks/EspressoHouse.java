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

import android.content.Context;

import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.Transaction;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.legacy.R;
import com.liato.bankdroid.provider.IBankTypes;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import eu.nullbyte.android.urllib.Urllib;

public class EspressoHouse extends Bank {
    private static final String API_URL = "http://www.espressohouse.com/coffee-card/min-sida/";

    public EspressoHouse(Context context) {
        super(context);
        TAG = "EspressoHouse";
        NAME = "Espresso House";
        NAME_SHORT = "espressohouse";
        BANKTYPE_ID = IBankTypes.ESPRESSOHOUSE;
    }

    public EspressoHouse(String username, String password, Context context)
            throws BankException, LoginException, BankChoiceException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException,
            IOException {
        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        urlopen = new Urllib(context);
        urlopen.setFollowRedirects(false);
        postData.add(new BasicNameValuePair("__EVENTTARGET",
                "ctl00$ctl00$ctl00$ContentPlaceHolderDefault$ContentPlaceHolderDefault$LoginView1$LoginUser$LoginButton"));
        postData.add(new BasicNameValuePair("ctl00$ctl00$ctl00$ContentPlaceHolderDefault$ContentPlaceHolderDefault$LoginView1$LoginUser$UserName",
                username));
        postData.add(new BasicNameValuePair("ctl00$ctl00$ctl00$ContentPlaceHolderDefault$ContentPlaceHolderDefault$LoginView1$LoginUser$Password",
                password));
        HttpResponse httpResponse = urlopen.openAsHttpResponse("http://www.espressohouse.com/coffee-card/logga-inladda/", postData, true);
        LoginPackage lp = new LoginPackage(urlopen, postData, null, API_URL);
        if (httpResponse.getStatusLine().getStatusCode() == 302) {
            lp.setIsLoggedIn(true);
        }
        return lp;
    }

    @Override
    public Urllib login() throws LoginException, BankException {
        try {
            LoginPackage lp = preLogin();
            if (!lp.isLoggedIn()) {
                throw new LoginException(res.getText(R.string.invalid_username_password).toString());
            }
        } catch (IOException e) {
            throw new BankException(e.getMessage());
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
            String response = urlopen.open(API_URL);
            Document dResponse = Jsoup.parse(response);
            String card = dResponse.select(".transactionCardNumber").first().text().trim();
            String cardNo = card.split(":")[1].trim();
            String balance = dResponse.select(".balanceAmount").first().text();
            balance = balance.substring(0, balance.length() - 2);
            Account a = new Account(card,
                    Helpers.parseBalance(balance),
                    cardNo, Account.REGULAR, "SEK");

            List<Element> eBalance = dResponse.select(".lineTotalAmount");
            List<Element> eTransaction = dResponse.select(".lineTime");
            List<Transaction> transactions = new ArrayList<>();
            for (int i = 0; i < eTransaction.size(); i++) {
                String s = eTransaction.get(i).text();
                balance = eBalance.get(i).text();
                balance = balance.substring(2, balance.length() - 2);
                transactions.add(new Transaction(s.substring(s.length() - 16, s.length() - 6),
                        s.substring(0, s.length() - 16), Helpers.parseBalance(balance)));
            }
            a.setTransactions(transactions);
            accounts.add(a);
        } catch (Exception e) {
            throw new BankException(e.getMessage());
        }
        super.updateComplete();
    }
}
