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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.text.InputType;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import eu.nullbyte.android.urllib.Urllib;

public class Bioklubben extends Bank {

    private static final String NAME = "Bioklubben";

    private static final String NAME_SHORT = "bioklubben";

    private static final String URL = "http://bioklubben.sf.se/Start.aspx";

    private static final int BANKTYPE_ID = Bank.BIOKLUBBEN;

    private static final boolean DISPLAY_DECIMALS = false;

    private String response = null;

    public Bioklubben(Context context) {
        super(context, R.drawable.logo_bioklubben);
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
        super.DISPLAY_DECIMALS = DISPLAY_DECIMALS;
        super.INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_TEXT
                | +InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
        super.INPUT_HINT_USERNAME = context.getString(R.string.email);
        currency = context.getString(R.string.points);
    }

    public Bioklubben(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        urlopen = new Urllib(context);
        urlopen.setAllowCircularRedirects(true);
        response = urlopen.open("http://bioklubben.sf.se/Start.aspx");

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
        postData.add(new BasicNameValuePair("__EVENTTARGET",
                "ctl00$ContentPlaceHolder1$LoginUserControl$LogonButton"));
        postData.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
        postData.add(new BasicNameValuePair("__VIEWSTATE", viewState));
        postData.add(new BasicNameValuePair("__EVENTVALIDATION", eventValidation));
        postData.add(new BasicNameValuePair("ctl00_toolkitscriptmanager_HiddenField", ""));
        postData.add(new BasicNameValuePair("ctl00$toolkitscriptmanager",
                "ctl00$UpdatePanel|ctl00$ContentPlaceHolder1$LoginUserControl$LogonButton"));
        postData.add(new BasicNameValuePair(
                "ctl00$ContentPlaceHolder1$LoginUserControl$LoginNameTextBox", getUsername()));
        postData.add(
                new BasicNameValuePair("ctl00$ContentPlaceHolder1$LoginUserControl$PasswordTextBox",
                        getPassword()));
        return new LoginPackage(urlopen, postData, response, "http://bioklubben.sf.se/Start.aspx");
    }

    public Urllib login() throws LoginException, BankException, IOException {
        LoginPackage lp = preLogin();
        response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
        if (response.contains("Felaktigt anv")) {
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
        Document d = Jsoup.parse(urlopen.open(
                "http://bioklubben.sf.se/MyPurchases.aspx?ParentTreeID=1&TreeID=1"));
        Element e = d.getElementById("ctl00_ContentPlaceHolder1_BonusPointsLabel");
        if (e == null) {
            throw new BankException(
                    res.getText(R.string.unable_to_find).toString() + " points element.");
        }
        BigDecimal b = Helpers.parseBalance(e.text());
        Account a = new Account("Poäng", b, "1");
        a.setCurrency(context.getString(R.string.points));
        accounts.add(a);
        balance = balance.add(a.getBalance());

        Elements es = d.select(".GridViewStd_Item,.GridViewStd_ItemAlt");
        List<Transaction> transactions = new ArrayList<Transaction>();
        if (es != null) {
            for (Element el : es) {
                transactions.add(
                        new Transaction(el.child(0).text().trim(), el.child(1).text().trim(),
                                Helpers.parseBalance(el.child(2).text())));
            }
        }
        a.setTransactions(transactions);

        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
        super.updateComplete();
    }
}
