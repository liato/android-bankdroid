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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class MinPension extends Bank {

    public MinPension(Context context) {
        super(context);
        TAG = "MinPension";
        NAME = "Min Pension.se";
        NAME_SHORT = "minpension";
        BANKTYPE_ID = IBankTypes.MINPENSION;
        INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;
        INPUT_TYPE_PASSWORD = InputType.TYPE_CLASS_PHONE | InputType.TYPE_TEXT_VARIATION_PASSWORD;
        ;
        INPUT_HINT_USERNAME = res.getText(R.string.pno).toString();
    }

    public MinPension(String username, String password, Context context)
            throws BankException, LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException,
            IOException {
        List<NameValuePair> postData = new ArrayList<>();
        urlopen = new Urllib(context,
                CertificateReader.getCertificates(context, R.raw.cert_minpension));
        String response = urlopen.open("https://www.minpension.se/AjaxifyContent/795");
        Document jDoc = Jsoup.parse(response);
        Element el = jDoc.select("input[name=__RequestVerificationToken]").first();
        if (el == null) {
            throw new BankException(res.getText(R.string.unable_to_find).toString() + " token.");
        }
        postData.add(new BasicNameValuePair("__RequestVerificationToken", el.val()));
        postData.add(new BasicNameValuePair("viewModel.Personnummer", getUsername()));
        postData.add(new BasicNameValuePair("viewModel.Kod", getPassword()));
        LoginPackage lp = new LoginPackage(urlopen, postData, null,
                "https://www.minpension.se/inloggning/personlig-kod");
        return lp;
    }

    @Override
    public Urllib login() throws LoginException, BankException, IOException {
        LoginPackage lp = preLogin();

        String response = urlopen.open(lp.getLoginTarget(), lp.getPostData(), true);
        if (!response.contains("LoggaUt.aspx")) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }
        response = urlopen.open(
                "https://www.minpension.se/mina-sidor/redirect?path=MinPension%2FDefault.aspx&bodyMargin=0");
        Document document = Jsoup.parse(response);
        Element e = document.select("#authenticationResult").first();
        if (e == null) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }
        List<NameValuePair> postData = new ArrayList<>();
        postData.add(new BasicNameValuePair("authenticationResult", e.val()));
        urlopen.open("https://minasidor.minpension.se/MinPension/Default.aspx", postData, true);

        return urlopen;
    }

    @Override
    public void update() throws BankException, LoginException, BankChoiceException, IOException {
        super.update();
        if (getUsername() == null || getPassword() == null || getUsername().length() == 0
                || getPassword().length() == 0) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }
        urlopen = login();
//Allmän pension
        accounts.add(updateAccount("https://minasidor.minpension.se/MinPension/AllmanPension.aspx",
                "#AllmänPensionTable tr",
                res.getText(R.string.public_pension).toString()));
//Tjänstepension
        accounts.add(updateAccount("https://minasidor.minpension.se/MinPension/Tjanstepension.aspx",
                "#TjänstepensionTable tr",
                res.getText(R.string.occupational_pension).toString()));
//Privat pension
        accounts.add(updateAccount("https://minasidor.minpension.se/MinPension/PrivatPension.aspx",
                "#PrivatPensionTable tr",
                res.getText(R.string.private_pension).toString()));

        super.updateComplete();
    }

    private Account updateAccount(String URL, String selector, String name) throws IOException {
        String response = urlopen.open(URL);
        Document dResponse = Jsoup.parse(response);
        List<Transaction> transactions = new ArrayList<>();
        String institute = "";
        String subInstitute = "";
        for (Element e : dResponse.select(selector)) {
            if (e.hasClass("GroupRow")) {
                institute = e.children().first().text();
            } else if (e.hasClass("GroupMemberRow") || e.hasClass("SubRow")) {
                Elements elements = e.children();
                if (elements.size() == 6) { //Special case for "Allmän pension"
                    if (elements.get(2).text().isEmpty()) {
                        //   subInstitute =  " — " + elements.get(1).text(); /* Doesn't fit atm. */
                    } else {
                        transactions.add(new Transaction(elements.get(5).text(),
                                institute + subInstitute + "\n — " + elements.get(1).text(),
                                Helpers.parseBalance(elements.get(2).text())));
                        subInstitute = "";
                    }
                } else if (elements.size() >= 7) {
                    transactions.add(new Transaction(elements.get(6).text(),
                            institute + "\n — " + elements.get(1).text(),
                            Helpers.parseBalance(elements.get(4).text())));
                }
            }
        }

        balance = BigDecimal.ZERO;
        for (Transaction t : transactions) {
            balance = balance.add(t.getAmount());
        }
        Account account = new Account(name, balance, name, Account.REGULAR, "");
        account.setTransactions(transactions);
        return account;
    }
}
