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

import android.content.Context;
import android.text.Html;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class TicketRikskortet extends Bank {

    private static final String NAME = "Ticket Rikskortet";

    private static final String URL = "https://www.edenred.se/rikskuponger/mina-sidor/logga-in/";

    private static final String URL_OVERVIEW = "https://www.edenred.se/rikskuponger/mina-sidor/employee/start/";

    private static final String URL_TRANSACTIONS = "https://www.edenred.se/rikskuponger/mina-sidor/employee/start/transaktioner/";

    private static final int BANKTYPE_ID = Bank.RIKSKORTET;

    private Pattern reViewState = Pattern.compile("__VIEWSTATE\"\\s+value=\"([^\"]+)\"");

    private Pattern reEventValidation = Pattern
            .compile("__EVENTVALIDATION\"\\s+value=\"([^\"]+)\"");

    private Pattern reBalance = Pattern.compile("EmployeeBalanceLabel\">([^<]+)</span>",
            Pattern.CASE_INSENSITIVE);

    private Pattern reTransactions = Pattern.compile(
            "(\\d{4}-\\d{2}-\\d{2})</td><td[^>]+>([^<]+)</td><td[^>]+>([^<]+)</td>",
            Pattern.CASE_INSENSITIVE);

    private String response = null;

    public TicketRikskortet(Context context) {
        super(context, R.drawable.logo_rikskortet);

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

    public TicketRikskortet(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }


    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        urlopen = new Urllib(context,
                CertificateReader.getCertificates(context, R.raw.cert_ticketrikskortet));
        response = urlopen.open(URL);
        Matcher matcher = reViewState.matcher(response);
        if (!matcher.find()) {
            throw new BankException(
                    res.getText(R.string.unable_to_find).toString() + " ViewState.");
        }
        String viewState = matcher.group(1);

        matcher = reEventValidation.matcher(response);
        if (!matcher.find()) {
            throw new BankException(
                    res.getText(R.string.unable_to_find).toString() + " EventValidation.");
        }
        String eventValidation = matcher.group(1);

        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("__EVENTTARGET", ""));
        postData.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
        postData.add(new BasicNameValuePair("__EVENTVALIDATION", eventValidation));
        postData.add(new BasicNameValuePair("__VIEWSTATE", viewState));
        postData.add(new BasicNameValuePair(
                "ctl00$CorporateHeaderArea$CorporateHeaderID$QuickSearch$SearchText", "Sök här"));
        postData.add(
                new BasicNameValuePair("ctl00$StartpageArea$ApplicationArea$LoginControl$UserName",
                        getUsername()));
        postData.add(
                new BasicNameValuePair("ctl00$StartpageArea$ApplicationArea$LoginControl$Password",
                        getPassword()));
        postData.add(new BasicNameValuePair(
                "ctl00$StartpageArea$ApplicationArea$LoginControl$LoginButton", "Logga in"));
        return new LoginPackage(urlopen, postData, response, URL);
    }

    public Urllib login() throws LoginException, BankException, IOException {
        LoginPackage lp = preLogin();
        response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
        if (response.contains("Inloggningen misslyckades")) {
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
        if (!urlopen.getCurrentURI().equalsIgnoreCase(URL_OVERVIEW)) {
            response = urlopen.open(URL_OVERVIEW);
        }

        Matcher matcher = reBalance.matcher(response);
        if (matcher.find()) {
            /*
             * Capture groups:
             * GROUP                EXAMPLE DATA
             * 1: Balance           590,22 kr
             *
             */
            BigDecimal b = Helpers.parseBalance(matcher.group(1));
            accounts.add(new Account("Saldo", b, "1"));
            balance = balance.add(Helpers.parseBalance(matcher.group(1)));
        }
        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
        super.updateComplete();
    }

    @Override
    public void updateTransactions(Account account, Urllib urlopen) throws LoginException,
            BankException, IOException {
        super.updateTransactions(account, urlopen);

        Matcher matcher;
        String response = urlopen.open(URL_TRANSACTIONS);
        matcher = reTransactions.matcher(response);
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();

        while (matcher.find()) {
            /*
             * Capture groups:
             * GROUP                EXAMPLE DATA
             * 1: Trans. date       2012-06-01
             * 2: Specification     DANMARKSG  KISTA
             * 3: Amount            - 85 kr
             *
             */

            transactions.add(new Transaction(matcher.group(1),
                    Html.fromHtml(matcher.group(2).trim()).toString(),
                    Helpers.parseBalance(matcher.group(3))));
        }
        account.setTransactions(transactions);
    }
}
