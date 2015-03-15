/*
 * Copyright (C) 2010 Nullbyte <http://nullbyte.eu>
 * Contributors: firetech
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.text.InputType;
import android.text.TextUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class Hemkop extends Bank {

    private static final String TAG = "Hemkop";

    private static final String NAME = "Hemköp Kundkort";

    private static final String NAME_SHORT = "hemkop";

    private static final String URL = "https://www.hemkop.se/Mina-sidor/Logga-in/";

    private static final int BANKTYPE_ID = IBankTypes.HEMKOP;

    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;

    private static final String INPUT_HINT_USERNAME = "ÅÅÅÅMMDDXXXX";

    private String response = null;

    public Hemkop(Context context) {
        super(context);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
        super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
    }

    public Hemkop(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }


    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        urlopen = new Urllib(context,
                CertificateReader.getCertificates(context, R.raw.cert_hemkop));
        urlopen.setAllowCircularRedirects(true);
        response = urlopen.open("https://www.hemkop.se/Mina-sidor/Logga-in/");

        Document d = Jsoup.parse(response);
        Element e = d.getElementById("__VIEWSTATE");
        if (e == null || e.attr("value") == null) {
            throw new BankException(
                    res.getText(R.string.unable_to_find).toString() + " ViewState.");
        }
        String viewState = e.attr("value");

        e = d.getElementById("__EVENTVALIDATION");
        if (e == null || e.attr("value") == null) {
            throw new BankException(
                    res.getText(R.string.unable_to_find).toString() + " EventValidation.");
        }
        String eventValidation = e.attr("value");

        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("__EVENTTARGET", "ctl00$MainContent$BtnLogin"));
        postData.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
        postData.add(new BasicNameValuePair("__VIEWSTATE", viewState));
        postData.add(new BasicNameValuePair("__SCROLLPOSITIONX", "0"));
        postData.add(new BasicNameValuePair("__SCROLLPOSITIONY", "266"));
        postData.add(new BasicNameValuePair("__EVENTVALIDATION", eventValidation));
        postData.add(new BasicNameValuePair("ctl00$uiTopMenu$Search", ""));
        postData.add(new BasicNameValuePair("ctl00$MainContent$tbUsername", username));
        postData.add(new BasicNameValuePair("ctl00$MainContent$tbPassword", password));
        return new LoginPackage(urlopen, postData, response,
                "https://www.hemkop.se/Mina-sidor/Logga-in/");
    }

    public Urllib login() throws LoginException, BankException, IOException {
        LoginPackage lp = preLogin();
        response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
        if (!response.contains("Inloggad som")) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }
        response = urlopen.open("https://www.hemkop.se/Mina-sidor/Bonussaldo/");
        return urlopen;
    }

    @Override
    public void update() throws BankException, LoginException, BankChoiceException, IOException {
        super.update();
        if (username == null || password == null || username.length() == 0
                || password.length() == 0) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }

        urlopen = login();

        Document d = Jsoup.parse(response);
        Elements amounts = d.select(".bonusStatement .amount");
        Elements names = d.select(".bonusStatement .label");
        for (int i = 0; i < Math.min(amounts.size(), names.size()); i++) {
            Element amount = amounts.get(i);
            Element name = names.get(i);
            BigDecimal accountBalance = Helpers.parseBalance(amount.ownText());
            Account account = new Account(name.ownText().replace(":", "").trim(), accountBalance,
                    String.format("acc_%d", i));
            if (i > 0) {
                account.setAliasfor("acc_0");
            }
            accounts.add(account);
            balance = balance.add(accountBalance);
        }

        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }

        Account account = accounts.get(0);

        response = urlopen.open("https://www.hemkop.se/Mina-sidor/Kontoutdrag/");
        d = Jsoup.parse(response);
        Elements es = d.select(".transactions tbody tr");
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        for (Element e : es) {
            Transaction t = new Transaction(e.child(1).ownText().trim(),
                    e.child(0).ownText().trim(),
                    Helpers.parseBalance(e.child(3).ownText()));
            if (!TextUtils.isEmpty(e.child(2).ownText())) {
                t.setCurrency(Helpers.parseCurrency(e.child(2).ownText().trim(), "SEK"));
            }
            transactions.add(t);
        }
        account.setTransactions(transactions);

        es = d.select(".currentBalance,.disposable");
        int i = 0;
        for (Element e : es) {
            Account a = new Account(e.child(0).ownText().trim(),
                    Helpers.parseBalance(e.child(1).ownText()), String.format("acc_cc_%d", i));
            a.setAliasfor("acc_0");
            accounts.add(a);
            i++;
        }

        super.updateComplete();
    }

    @Override
    public void updateTransactions(Account account, Urllib urlopen) throws LoginException,
            BankException, IOException {
        super.updateTransactions(account, urlopen);
        /*
        if (!"acc_0".equals(account.getId())) return;
        try {
            response = urlopen.open("https://www.hemkop.se/Mina-sidor/Kontoutdrag/");
            Document d = Jsoup.parse(response);
        	Elements es = d.select(".transactions tbody tr");
            ArrayList<Transaction> transactions = new ArrayList<Transaction>();
            for (Element e : es) {
                Transaction t = new Transaction(e.child(1).ownText().trim(),
                					e.child(0).ownText().trim(),
                        Helpers.parseBalance(e.child(3).ownText()));
                if (!TextUtils.isEmpty(e.child(2).ownText())) {
                    t.setCurrency(Helpers.parseCurrency(e.child(2).ownText().trim(), "SEK"));
                }
                transactions.add(t);
        	}
            account.setTransactions(transactions);
        } catch (ClientProtocolException e) {
        	e.printStackTrace();
            Log.e(TAG, e.getMessage() != null ? e.getMessage() : "");
        } catch (IOException e) {
        	e.printStackTrace();
            Log.e(TAG,  e.getMessage() != null ? e.getMessage() : "");
        }
        finally {
            super.updateComplete();
        }
        */
    }
}
