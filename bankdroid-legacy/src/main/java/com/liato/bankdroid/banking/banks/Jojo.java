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
import com.liato.bankdroid.legacy.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class Jojo extends Bank {
    private static final String TAG = "Jojo";
    private static final String NAME = "Jojo Reskassa";
    private static final String NAME_SHORT = "jojo";
    private static final String URL = "https://www.shop.skanetrafiken.se";
    private static final int BANKTYPE_ID = IBankTypes.JOJO;

    private static final String NAME_NOT_SET = "KortnamnSaknas";

    private String response = null;

    public Jojo(Context context) {
        super(context);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
        super.INPUT_TITLETEXT_USERNAME = R.string.email;
    }

    public Jojo(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException,
            IOException {
        urlopen = new Urllib(context, CertificateReader.getCertificates(context, R.raw.cert_jojo));

        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("GOTO", "/mobile/minakort.html"));
        postData.add(new BasicNameValuePair("login", username));
        postData.add(new BasicNameValuePair("password", password));
        postData.add(new BasicNameValuePair("CUSTOMER_LOGIN", "LOGGA IN"));
        return new LoginPackage(urlopen, postData, response, URL + "/mobile/customer.html");
    }

    public Urllib login() throws LoginException, BankException {
        try {
            LoginPackage lp = preLogin();
            response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
            if (!response.contains("[Logga ut]")) {
                throw new LoginException(res.getText(R.string.invalid_username_password).toString());
            }
        } catch (ClientProtocolException e) {
            throw new BankException(e.getMessage());
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

        Document d = Jsoup.parse(response);
        Elements cards = d.select(".my_cards_allinfo table tbody");
        if (!cards.isEmpty()) {
            for(Element card : cards) {
                String cardNumber = card.select("tr:first-child td").text().trim();
                BigDecimal saldo = getSaldo(cardNumber);
                String name = card.select("tr:nth-child(2) td").text().trim();
                String displayName = NAME_NOT_SET.equals(name) ? cardNumber : name;
                accounts.add(new Account(displayName, saldo, cardNumber));
                balance = balance.add(saldo);
            }
        }

        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
        super.updateComplete();
    }


    private BigDecimal getSaldo(String cardNumber) {
        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("cardno", cardNumber));
        postData.add(new BasicNameValuePair("fromlist","1"));
        postData.add(new BasicNameValuePair("ST_CHECK_SALDO", "1"));
        try {
            String saldoData = urlopen.open(URL + "/saldodata.html",postData,true);
            Document saldoDocument = Jsoup.parse(saldoData);
            Elements saldo = saldoDocument.select("td.greenrow.right h3");
            if(!saldo.isEmpty()) {
                return Helpers.parseBalance(saldo.first().text().trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }
}
