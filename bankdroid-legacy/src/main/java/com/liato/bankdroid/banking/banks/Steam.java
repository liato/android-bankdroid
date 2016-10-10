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

import android.content.Context;
import android.text.Html;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;
import timber.log.Timber;

public class Steam extends Bank {

    private static final String TAG = "Steam";

    private static final String NAME = "Steam Wallet";

    private static final String NAME_SHORT = "steam";

    private static final String URL = "https://store.steampowered.com/login/?redir=account";

    private static final int BANKTYPE_ID = IBankTypes.STEAM;

    private static final boolean STATIC_BALANCE = true;

    private Pattern reBalance = Pattern.compile("accountBalance\">\\s*<div[^>]+>([^<]+)</div>",
            Pattern.CASE_INSENSITIVE);

    private Pattern reTransactions = Pattern.compile(
            "(?:even|odd)\">\\s*<div\\s*class=\"transactionRowDate\">([^<]+)</div>\\s*<div.*?RowPrice\">([^<]+)</div>\\s*<div.*?RowEvent\">([^<]+)</div>.*?RowTitle\">([^<]+)</div>\\s*<span[^>]+>([^<]*)<",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private String response = null;

    public Steam(Context context) {
        super(context, R.drawable.logo_steam);

        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
        super.STATIC_BALANCE = STATIC_BALANCE;
    }

    public Steam(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }


    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        urlopen = new Urllib(context, CertificateReader.getCertificates(context, R.raw.cert_steam));
        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("redir", "account"));
        postData.add(new BasicNameValuePair("username", getUsername()));
        postData.add(new BasicNameValuePair("password", getPassword()));
        return new LoginPackage(urlopen, postData, null, "https://store.steampowered.com/login/");
    }

    @Override
    public Urllib login() throws LoginException, BankException, IOException {
        LoginPackage lp = preLogin();
        response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
        if (response.contains("Enter the characters above")) {
            throw new LoginException(
                    "You have entered the wrong username/password too many times and Steam now requires you to enter a CAPTCHA.\nPlease wait 10 minutes before logging in again.");
        }
        if (response.contains("Incorrect login.")) {
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
        Matcher matcher = reBalance.matcher(response);
        if (matcher.find()) {
            /*
             * Capture groups:
             * GROUP                EXAMPLE DATA
             * 1: Amount            0,--&#8364;
             *
             */
            String amount = Html.fromHtml(matcher.group(1)).toString().trim().replace("--", "00");
            Account account = new Account("Wallet", Helpers.parseBalance(amount), "1");
            String currency = Helpers.parseCurrency(amount, "USD");
            this.setCurrency(currency);
            account.setCurrency(currency);
            balance = balance.add(Helpers.parseBalance(amount));
            ArrayList<Transaction> transactions = new ArrayList<Transaction>();
            matcher = reTransactions.matcher(response);
            while (matcher.find()) {
                    /*
                 * Capture groups:
                 * GROUP                EXAMPLE DATA
                 * 1: Date              18 Oct 2007
                 * 2: Amount            0,99&#8364;
                 * 3: Event             Purchase
                 * 4: Item              Team Fortress 2&nbsp;
                 * 5: Sub item          Mann Co. Supply Crate Key
                 *
                 */
                SimpleDateFormat sdfFrom = new SimpleDateFormat("d MMM yyyy");
                SimpleDateFormat sdfTo = new SimpleDateFormat("yyyy-MM-dd");
                Date transactionDate;
                try {
                    transactionDate = sdfFrom.parse(matcher.group(1).trim());
                    String strDate = sdfTo.format(transactionDate);
                    BigDecimal price = Helpers.parseBalance(
                            Html.fromHtml(matcher.group(2)).toString().trim().replace("--", "00"));
                    if ("Purchase".equalsIgnoreCase(matcher.group(3).trim())) {
                        price = price.negate();
                    }
                    transactions.add(new Transaction(strDate,
                            Html.fromHtml(matcher.group(4)).toString().trim() + (
                                    Html.fromHtml(matcher.group(5)).toString().trim().length() > 1 ?
                                            " (" + Html.fromHtml(matcher.group(5)).toString().trim()
                                                    + ")" : ""),
                            price,
                            Helpers.parseCurrency(Html.fromHtml(matcher.group(2)).toString().trim(),
                                    "USD")));
                } catch (ParseException e) {
                    Timber.e(e, "Unable to parse date: %s", matcher.group(1).trim());
                }
            }
            Collections.sort(transactions, Collections.reverseOrder());
            account.setTransactions(transactions);
            accounts.add(account);
        }
        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
        super.updateComplete();
    }
}
