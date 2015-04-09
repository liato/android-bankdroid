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

import android.content.Context;
import android.text.InputType;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class PayPal extends Bank {
    private static final String TAG = "PayPal";
    private static final String NAME = "PayPal";
    private static final String NAME_SHORT = "paypal";
    private static final String ORIGIN_URL =  "https://www.paypal.com";
    private static final String REFERER_URL ="https://www.paypal.com/se/webapps/mpp/home";
    private static final String OVERVIEW_URL = "https://www.paypal.com/myaccount/home";
    private static final String LOGIN_URL = "https://www.paypal.com/signin/intent/";
    private static final String BALANCE_URL = "https://www.paypal.com/myaccount/wallet/balance";

    private static final int BANKTYPE_ID = IBankTypes.PAYPAL;
    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_TEXT
            | +InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
    private static final boolean STATIC_BALANCE = true;
    private String response = null;

    public PayPal(Context context) {
        super(context);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
        super.STATIC_BALANCE = STATIC_BALANCE;
    }

    public PayPal(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }


    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        try {
            urlopen = login();
            LoginPackage lp = new LoginPackage(urlopen, null,
                    response, OVERVIEW_URL);
            lp.setIsLoggedIn(true);
            return lp;
        } catch (LoginException e) {
            return null;
        }
    }

    @Override
    public Urllib login() throws LoginException, BankException, IOException {
        urlopen = new Urllib(context,
                CertificateReader.getCertificates(context, R.raw.cert_paypal));
        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("email", username));
        postData.add(new BasicNameValuePair("password", password));
        postData.add(new BasicNameValuePair("ul-submit-cookied", "Logga in"));
        urlopen.addHeader("Origin",ORIGIN_URL);
        urlopen.addHeader("Referer", REFERER_URL);
        response = urlopen.open(LOGIN_URL, postData, true);
        if (response.contains("Some information you entered isn't right.")) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }
        /* TODO find a reliable way to verify that the user has logged in successfully.
        if (!response.contains("cgi-bin/webscr?cmd=_logout\" class=\"btn btn-small btn-secondary logout\">")) {
            throw new BankException("Error logging in to PayPal");
        }
        */
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

        /* Start by parsing the data in "response" (containing transactions) */
        List<Transaction> transactions = new ArrayList<>();
        Document trans = Jsoup.parse(response);
        try {
            for (Element e : trans.select(".transactionItem .row")) {
                String date = e.select(".dateParts").first().text(); //"Mar 17 2015"
                date = new SimpleDateFormat("yyyy-MM-dd").format(
                        new SimpleDateFormat("MMM dd yyyy").parse(date));
                String description = e.select(".transactionDescription").first().text(); //"Unovation Inc."
                String type = e.select(".transactionType").first().text(); //"Recurring Payment "
                String amount = e.select(".transactionAmount").first().text(); //"-negative $3.00 USD"
                Transaction t = new Transaction(date,
                        description + "\n— " + type, Helpers.parseBalance(amount));
                t.setCurrency(amount.substring(amount.length() - 3));
                transactions.add(t);
            }
        } catch (ParseException | NullPointerException e) {
            //Ignore parsing errors of transactions (for now).
            //TODO handle parsing errors gracefully.
        }

        try {
            //Set bank balance from first page balance.
            String bal = trans.select(".balanceNumeral.nemo_balanceNumeral .h2").text();
            this.balance = Helpers.parseBalance(bal);
            this.setCurrency(bal.substring(bal.length() - 3));

            //Get all currencies and create separate accounts for each.
            response = urlopen.open(BALANCE_URL);
            Document doc = Jsoup.parse(response);
            Element wallet = doc.getElementById("wallet");
            bal = wallet.attr("data-balance").replace("&quot;", "\"");
            JSONObject acc = new JSONObject(bal);
            this.balance = Helpers.parseBalance(
                    acc.getJSONObject("totalAvailable").getString("unformattedAmount"));
            this.setCurrency(acc.getString("primaryCurrency"));

            JSONArray accArr = acc.getJSONArray("balanceDetails");
            for (int i = 0; i < accArr.length(); i++) {
                try {
                    JSONObject a = accArr.getJSONObject(i);
                    String accountName = a.getString("currency");
                    String displayName = accountName;
                    if (accountName.equals(this.getCurrency()))
                        displayName = accountName + " (Primary)";
                    Double amount = a.getJSONObject("available")
                            .getDouble("unformattedAmount");
                    Account account = new Account(displayName,
                            Helpers.parseBalance(amount.toString()),
                            accountName,
                            Account.REGULAR,
                            accountName);
                    account.setTransactions(transactions);
                    accounts.add(account);
                } catch (JSONException e) {
                    //Ignore if we can't add a new account..
                }
            }
        } catch (JSONException e) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString(), e);
        }
        super.updateComplete();
    }
}
