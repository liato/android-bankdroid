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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.text.InputType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import eu.nullbyte.android.urllib.Urllib;

public class Hors extends Bank {

    private static final String TAG = "Hors";

    private static final String NAME = "Hörs";

    private static final String NAME_SHORT = "hors";

    private static final String URL = "http://www.dittkort.se/hors/";

    private static final int BANKTYPE_ID = IBankTypes.HORS;

    private static final boolean DISPLAY_DECIMALS = false;

    private String response = null;

    public Hors(Context context) {
        super(context, R.drawable.logo_hors);

        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
        super.DISPLAY_DECIMALS = DISPLAY_DECIMALS;
        super.INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_TEXT
                | +InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
        super.INPUT_HINT_USERNAME = context.getString(R.string.card_id);
        super.INPUT_HIDDEN_PASSWORD = true;
    }

    public Hors(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        urlopen = new Urllib(context);
        urlopen.setAllowCircularRedirects(true);
        response = urlopen.open("https://www.dittkort.se//q/?p=7EB4F129-0A41-417F-8FEA-51B2B75B9D24");

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
        postData.add(new BasicNameValuePair("__EVENTTARGET", ""));
        postData.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
        postData.add(new BasicNameValuePair("__VIEWSTATE", viewState));
        postData.add(new BasicNameValuePair("__VIEWSTATEGENERATOR", "D7823E0B"));
        postData.add(new BasicNameValuePair("__EVENTVALIDATION", eventValidation));
        postData.add(new BasicNameValuePair("_ctl0:cphMain:SmartpassTextbox", getUsername()));
        postData.add(new BasicNameValuePair("_ctl0:cphMain:SubmitButton", "OK"));
        postData.add(new BasicNameValuePair("_ctl0:cphMain:chbRememberSmartPassCode", "on"));
        postData.add(new BasicNameValuePair("_ctl0:cphMain:cookieEnabled", "true"));
        return new LoginPackage(urlopen, postData, response, "https://www.dittkort.se//q/?p=7EB4F129-0A41-417F-8FEA-51B2B75B9D24");
    }

    public Urllib login() throws LoginException, BankException, IOException {
        LoginPackage lp = preLogin();
        response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
        if (response.contains("cphMain_txtGeneralCardError")) {
            throw new LoginException(res.getText(R.string.invalid_card_number).toString());
        }
        return urlopen;
    }

    @Override
    public void update() throws BankException, LoginException, BankChoiceException, IOException {
        super.update();
        urlopen = login();
        Document document = Jsoup.parse(response);
        Element balanceElement = document.getElementById("cphMain_lblAmount");
        if (balanceElement == null) {
            throw new BankException(
                    res.getText(R.string.unable_to_find).toString() + res.getText(R.string.balance).toString());
        }

        Element nameElement = document.getElementById("lblCardName");
        String accountName = nameElement == null ? NAME.toUpperCase() : nameElement.text();
        if (this.getCustomName().isEmpty()) {
            this.setCustomName(accountName);
        }

        Account account = new Account(accountName, Helpers.parseBalance(balanceElement.text()), "0");
        accounts.add(account);
        balance = balance.add(account.getBalance());

        document = Jsoup.parse(urlopen.open("https://www.dittkort.se/q/Partial/Transactions.aspx?cnt=20"));
        Elements transactionElements = document.select("tr");
        List<Transaction> transactions = new ArrayList<Transaction>();
        if (transactionElements != null) {
            for (Element element : transactionElements) {
                transactions.add(asTransaction(element));
            }
        }
        account.setTransactions(transactions);
        super.updateComplete();
    }

    private Transaction asTransaction(Element element) {
        return new Transaction(element.child(0).text().trim(), element.child(1).text().trim(),
                Helpers.parseBalance(element.child(2).text()));
    }
}
