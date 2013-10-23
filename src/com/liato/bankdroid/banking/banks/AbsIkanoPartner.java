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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.text.InputType;

import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.Transaction;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public abstract class AbsIkanoPartner extends Bank {
    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;
    private static final int INPUT_TYPE_PASSWORD = InputType.TYPE_CLASS_PHONE;
    private static final String INPUT_HINT_USERNAME = "ÅÅÅÅMMDDXXXX";

    private String response = null;
    protected String structId;

    public AbsIkanoPartner(Context context) {
        super(context);
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
        super.INPUT_TYPE_PASSWORD = INPUT_TYPE_PASSWORD;
        super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
    }

    public AbsIkanoPartner(String username, String password, Context context) throws BankException, LoginException,
            BankChoiceException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException, ClientProtocolException, IOException {
        urlopen = new Urllib(context, CertificateReader.getCertificates(context, R.raw.cert_ikanopartner));
        response = urlopen.open("https://partner.ikanobank.se/web/engines/page.aspx?structid=" + structId);
        Document d = Jsoup.parse(response);
        Element e = d.getElementById("__VIEWSTATE");
        if (e == null || e.attr("value") == null) {
            throw new BankException(res.getText(R.string.unable_to_find).toString() + " ViewState.");
        }
        String viewState = e.attr("value");

        e = d.getElementById("__EVENTVALIDATION");
        if (e == null || e.attr("value") == null) {
            throw new BankException(res.getText(R.string.unable_to_find).toString() + " EventValidation.");
        }
        String eventValidation = e.attr("value");

        e = d.select("#LoginCustomerDiv > div").first();
        if (e == null || e.attr("id") == null || e.attr("id").split("_", 2).length < 2) {
            throw new BankException(res.getText(R.string.unable_to_find).toString() + " ctl.");
        }
        String ctl = e.attr("id").split("_", 2)[0];
        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("__VIEWSTATE", viewState));
        postData.add(new BasicNameValuePair("__EVENTVALIDATION", eventValidation));
        postData.add(new BasicNameValuePair(ctl
                + "$LoginWebUserControl$SSNControl$SSNSimpleValueUsercontrol$editValueTextbox", username));
        postData.add(new BasicNameValuePair(ctl
                + "$LoginWebUserControl$passwordSimpleValueControl$passwordSimpleValueControl$editValueTextbox",
                password));
        postData.add(new BasicNameValuePair(ctl + "$LoginButton", ""));
        return new LoginPackage(urlopen, postData, response,
                "https://partner.ikanobank.se/web/engines/page.aspx?structid=" + structId);
    }

    @Override
    public Urllib login() throws LoginException, BankException {
        try {
            LoginPackage lp = preLogin();
            response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
            if (response.contains("eller personnumme") || response.contains("elaktigt personnummer")
                    || response.contains("ontrollera personnummer") || response.contains("elaktig inloggningskod")
                    || response.contains("elaktig självbetjäningskod")) {
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
        Element element = d.select("#primary-nav > li:eq(1) > a").first();
        if (element != null && element.attr("href") != null) {
            String myAccountUrl = element.attr("href");
            try {
                response = urlopen.open("https://partner.ikanobank.se/" + myAccountUrl);
                d = Jsoup.parse(response);
                Elements es = d.select("#CustomerAccountInformationSpan > span > span");
                int accId = 0;
                for (Element el : es) {
                    Element name = el.select("> span > span:eq(0)").first();
                    Element balance = el.select("> span:eq(1)").first();
                    Element currency = el.select("> span:eq(2)").first();
                    if (name != null && balance != null && currency != null) {
                        Account account = new Account(name.text().trim(), Helpers.parseBalance(balance.text()),
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
                        Transaction transaction = new Transaction(el.child(0).text().trim(), el.child(1).text().trim(),
                                Helpers.parseBalance(el.child(2).text()));
                        transaction.setCurrency(Helpers.parseCurrency(el.child(3).text().trim(), "SEK"));
                        transactions.add(transaction);
                    }
                }
                accounts.get(0).setTransactions(transactions);
            }

            catch (ClientProtocolException e) {
                throw new BankException(e.getMessage());
            } catch (IOException e) {
                throw new BankException(e.getMessage());
            } finally {
                super.updateComplete();
            }
        }
        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }

    }
}