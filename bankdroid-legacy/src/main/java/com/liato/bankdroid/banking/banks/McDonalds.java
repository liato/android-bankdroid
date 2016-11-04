/*
 * Copyright (C) 2010 Nullbyte <http://nullbyte.eu>
 * Contributors: PMC
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
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.text.Html;
import android.text.InputType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.nullbyte.android.urllib.Urllib;

public class McDonalds extends Bank {

    private static final String NAME = "McDonald's Presentkort";

    private static final String URL = "http://apps.mcdonalds.se/sweden/giftquer.nsf/egift?OpenForm";

    private static final int BANKTYPE_ID = Bank.MCDONALDS;

    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;

    private static final boolean INPUT_HIDDEN_PASSWORD = true;

    private static final int INPUT_TITLETEXT_USERNAME = R.string.card_number;

    private Pattern reBalance = Pattern.compile("saldo:\\s*([0-9,. -]+)\\s*kronor");

    private Pattern reTransactions = Pattern.compile(
            "<tr><td>(\\d{2}-\\d{2}-\\d{2})\\s*\\d{2}:\\d{2}</td><td>([^<]+)</td><td>[^<]+</td><td>([^<]+)</td></tr>");

    private String response = "";

    public McDonalds(Context context) {
        super(context, R.drawable.logo_mcdonalds);

        super.url = URL;
        super.inputTypeUsername = INPUT_TYPE_USERNAME;
        super.inputHiddenPassword = INPUT_HIDDEN_PASSWORD;
        super.inputTitletextUsername = INPUT_TITLETEXT_USERNAME;
    }

    @Override
    public int getBanktypeId() {
        return BANKTYPE_ID;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public McDonalds(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException, ClientProtocolException, IOException {
        urlopen = new Urllib(context);

        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("__Click", "0"));
        postData.add(new BasicNameValuePair("CardNumber", getUsername()));

        return new LoginPackage(urlopen, postData, response,
                "http://apps.mcdonalds.se/sweden/giftquer.nsf/egift?OpenForm&Seq=1");
    }

    @Override
    public Urllib login() throws LoginException, BankException, IOException {
        LoginPackage lp = preLogin();
        response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
        if (response.contains("felaktigt kortnummer")) {
            throw new LoginException(res.getText(R.string.invalid_card_number).toString());
        }
        return urlopen;
    }

    @Override
    public void update() throws BankException, LoginException, BankChoiceException, IOException {
        super.update();
        if (getUsername().length() != 19) {
            throw new LoginException(res.getText(R.string.invalid_card_number).toString());
        }
        login();
        Matcher matcher = reBalance.matcher(response);
        if (matcher.find()) {
            /*
             * Capture groups:
             * GROUP                    EXAMPLE DATA
             * 1: balance               845
             *
             */
            Account account = new Account("Presentkort", Helpers.parseBalance(matcher.group(1)),
                    "1");
            balance = Helpers.parseBalance(matcher.group(1));
            ArrayList<Transaction> transactions = new ArrayList<Transaction>();
            matcher = reTransactions.matcher(response);
            while (matcher.find()) {
                /*
                 * Capture groups:
                 * GROUP                    EXAMPLE DATA
                 * 1: Date                  11-03-17
                 * 2: Location              Sthlm, Sk&ouml;ndal
                 * 3: Amount                -144
                 *
                 */
                transactions.add(new Transaction("20" + matcher.group(1).trim(),
                        Html.fromHtml(matcher.group(2)).toString().trim(),
                        Helpers.parseBalance(matcher.group(3))));
                account.setTransactions(transactions);
            }
            accounts.add(account);
        }
        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
        super.updateComplete();
    }
}
