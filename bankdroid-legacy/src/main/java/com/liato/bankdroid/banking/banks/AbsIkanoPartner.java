/*
 *  Copyright (C) 2010 Nullbyte <http://nullbyte.eu>
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
import android.support.annotation.DrawableRes;
import android.text.InputType;
import android.text.TextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public abstract class AbsIkanoPartner extends Bank {

    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;

    private static final int INPUT_TYPE_PASSWORD = InputType.TYPE_CLASS_PHONE;

    private static final String INPUT_HINT_USERNAME = "ÅÅÅÅMMDDXXXX";

    protected String structId;

    private String response = null;

    public AbsIkanoPartner(Context context, @DrawableRes int logoResource) {
        super(context, logoResource);
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
        super.INPUT_TYPE_PASSWORD = INPUT_TYPE_PASSWORD;
        super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
        super.STATIC_BALANCE = true;
    }

    public AbsIkanoPartner(String username, String password, Context context, @DrawableRes int logoResource)
            throws BankException, LoginException,
            BankChoiceException, IOException {
        this(context, logoResource);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        urlopen = new Urllib(context,
                CertificateReader.getCertificates(context, R.raw.cert_ikanopartner));
        response = urlopen
                .open("https://partner.ikanobank.se/web/engines/page.aspx?structid=" + structId);

        Document d = Jsoup.parse(response);
        Element viewstate = d.getElementById("__VIEWSTATE");
        if (viewstate == null || TextUtils.isEmpty(viewstate.val())) {
            throw new BankException(
                    res.getText(R.string.unable_to_find).toString() + " ViewState.");
        }

        Element eventvalidation = d.getElementById("__EVENTVALIDATION");
        if (eventvalidation == null || TextUtils.isEmpty(eventvalidation.val())) {
            throw new BankException(
                    res.getText(R.string.unable_to_find).toString() + " EventValidation.");
        }

        Element userField = d.select("#LoginSpan input[type=text]").first();
        Element passField = d.select("#LoginSpan input[type=password]").first();
        Element submitField = d.select("#LoginCustomerDiv input[type=submit]").first();

        if (userField == null || passField == null || submitField == null) {
            throw new BankException(
                    res.getText(R.string.unable_to_find).toString() + " login fields.");
        }
        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("__VIEWSTATE", viewstate.val()));
        postData.add(new BasicNameValuePair("__EVENTVALIDATION", eventvalidation.val()));
        postData.add(new BasicNameValuePair(userField.attr("name"), getUsername()));
        postData.add(new BasicNameValuePair(passField.attr("name"), getPassword()));
        postData.add(new BasicNameValuePair(submitField.attr("name"), submitField.val()));
        return new LoginPackage(urlopen, postData, response,
                "https://partner.ikanobank.se/web/engines/page.aspx?structid=" + structId);

    }

    @Override
    public Urllib login() throws LoginException, BankException, IOException {
        LoginPackage lp = preLogin();
        response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
        if (response.contains("eller personnumme") || response.contains("elaktigt personnummer")
                || response.contains("ontrollera personnummer") || response
                .contains("elaktig inloggningskod")
                || response.contains("elaktig självbetjäningskod")) {
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
        Document d = Jsoup.parse(response);
        Element element = d.select("#primary-nav > li:eq(1) > a").first();
        if (element != null && element.attr("href") != null) {
            String myAccountUrl = element.attr("href");

            response = urlopen.open("https://partner.ikanobank.se/" + myAccountUrl);
            d = Jsoup.parse(response);
            Elements es = d.select("#CustomerAccountInformationSpan > span > span");
            int accId = 0;
            for (Element el : es) {
                Element name = el.select("> span > span:eq(0)").first();
                Element currency = el.select("> span:eq(2)").first();
                Element balance = el.select("> span:eq(1)").first();
                if (name != null && balance != null && currency != null) {
                    Account account = new Account(name.text().trim(),
                            Helpers.parseBalance(balance.text()),
                            Integer.toString(accId));
                    account.setCurrency(Helpers.parseCurrency(currency.text(), "SEK"));
                    if (accId > 0) {
                        account.setAliasfor("0");
                    }
                    accounts.add(account);
                    accId++;
                }
            }
            if (accounts.isEmpty()) {
                throw new BankException(res.getText(R.string.no_accounts_found).toString());
            }
            // Use the amount from "Kvar att handla för" which should be the
            // last account in the list.
            this.balance = accounts.get(accounts.size() - 1).getBalance();
            ArrayList<Transaction> transactions = new ArrayList<Transaction>();
            es = d.select("#ShowCustomerTransactionPurchasesInformationDiv table tr:has(td)");
            for (Element el : es) {
                if (el.childNodeSize() == 6) {
                    Transaction transaction = new Transaction(el.child(0).text().trim(),
                            el.child(1).text().trim(),
                            Helpers.parseBalance(el.child(2).text()));
                    transaction
                            .setCurrency(Helpers.parseCurrency(el.child(3).text().trim(), "SEK"));
                    transactions.add(transaction);
                }
            }
            accounts.get(0).setTransactions(transactions);
        }
        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
        super.updateComplete();
    }
}
