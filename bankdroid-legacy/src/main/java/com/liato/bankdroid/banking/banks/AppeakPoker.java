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
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.legacy.R;

import org.apache.http.NameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.content.Context;
import android.text.InputType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import eu.nullbyte.android.urllib.Urllib;

public class AppeakPoker extends Bank {

    private static final String NAME = "Appeak Poker";

    private static final String URL = "http://poker.appeak.se/";

    private static final int BANKTYPE_ID = Bank.APPEAKPOKER;

    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_TEXT;

    private static final boolean INPUT_HIDDEN_PASSWORD = true;

    private String mChips = null;

    public AppeakPoker(Context context) {
        super(context, R.drawable.logo_appeakpoker);
        super.url = URL;
        super.inputTypeUsername = INPUT_TYPE_USERNAME;
        super.inputHiddenPassword = INPUT_HIDDEN_PASSWORD;
        super.displayDecimals = false;
        currency = "chips";
    }

    @Override
    public int getBanktypeId() {
        return BANKTYPE_ID;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public AppeakPoker(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        urlopen = new Urllib(context);
        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        return new LoginPackage(urlopen, postData, "",
                String.format("http://poker.appeak.se/playerInfo/?username=%s", getUsername()));
    }

    @Override
    public Urllib login() throws LoginException, BankException, IOException {
        LoginPackage lp = preLogin();
        String response = urlopen.open(lp.getLoginTarget());
        Document d = Jsoup.parse(response);
        Element e = d.select("#content > table tr:eq(2) td:eq(1)").first();
        if (e == null) {
            throw new LoginException(res.getText(R.string.invalid_username).toString());
        } else {
            mChips = e.html();
        }
        return urlopen;
    }

    @Override
    public void update() throws BankException, LoginException, BankChoiceException, IOException {
        super.update();
        if (getUsername().isEmpty()) {
            throw new LoginException(res.getText(R.string.invalid_card_number).toString());
        }
        login();
        if (mChips != null) {
            Account account = new Account("Chips",
                    Helpers.parseBalance(mChips.replaceAll("\\D", "")), "1");
            account.setCurrency("chips");
            balance = account.getBalance();
            accounts.add(account);
        }
        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
        super.updateComplete();
    }
}
