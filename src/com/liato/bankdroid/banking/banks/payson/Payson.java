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

package com.liato.bankdroid.banking.banks.payson;

import android.content.Context;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.Transaction;
import com.liato.bankdroid.banking.banks.payson.model.TransactionHistory;
import com.liato.bankdroid.banking.banks.payson.model.User;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class Payson extends Bank {
    private static final String TAG = "Payson";
    private static final String NAME = "Payson";
    private static final String NAME_SHORT = "payson";
    private static final String URL = "https://www.payson.se/signin/";
    private static final int BANKTYPE_ID = IBankTypes.PAYSON;
    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_TEXT | +InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;

    private Pattern reEventValidation = Pattern.compile("__EVENTVALIDATION\"\\s+value=\"([^\"]+)\"");
    private Pattern reViewState = Pattern.compile("__VIEWSTATE\"\\s+value=\"([^\"]+)\"");
    private Pattern reCleanText = Pattern.compile("\\s+", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private String response = null;
    private ObjectMapper mObjectMapper;

    public Payson(Context context) {
        super(context);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
    }

    public Payson(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
        this(context);
        this.update(username, password);
    }


    @Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
        urlopen = new Urllib(context, CertificateReader.getCertificates(context, R.raw.cert_payson));
        response = urlopen.open("https://www.payson.se/signin/");
        Matcher matcher = reViewState.matcher(response);
        if (!matcher.find()) {
            throw new BankException(res.getText(R.string.unable_to_find).toString() + " ViewState.");
        }
        String strViewState = matcher.group(1);
        matcher = reEventValidation.matcher(response);
        if (!matcher.find()) {
            throw new BankException(res.getText(R.string.unable_to_find).toString() + " EventValidation.");
        }
        String strEventValidation = matcher.group(1);

        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("__LASTFOCUS", ""));
        postData.add(new BasicNameValuePair("__EVENTTARGET", ""));
        postData.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
        postData.add(new BasicNameValuePair("__VIEWSTATE", strViewState));
        postData.add(new BasicNameValuePair("__EVENTVALIDATION", strEventValidation));
        postData.add(new BasicNameValuePair("ctl00$MainContent$SignIn1$txtEmail", username));
        postData.add(new BasicNameValuePair("ctl00$MainContent$SignIn1$txtPassword", password));
        postData.add(new BasicNameValuePair("ctl00$MainContent$SignIn1$btnLogin", "Logga in"));
        return new LoginPackage(urlopen, postData, response, "https://www.payson.se/signin/");
    }

    @Override
    public Urllib login() throws LoginException, BankException {
        try {
            LoginPackage lp = preLogin();
            response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
            if (response.contains("Felaktig E-postadress") || response.contains("Lösenord saknas") ||
                    response.contains("E-postadress saknas")) {
                throw new LoginException(res.getText(R.string.invalid_username_password).toString());
            }
        } catch (ClientProtocolException e) {
            throw new BankException(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
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

        try {
            HttpResponse httpResponse = urlopen.openAsHttpResponse(String.format("https://www.payson.se/myaccount/User/GetUserInfo?DateTime=%s", System.currentTimeMillis()), new ArrayList<NameValuePair>(), false);
            User user = readJsonValue(httpResponse, User.class);
            httpResponse = urlopen.openAsHttpResponse(String.format("https://www.payson.se/myaccount/History/List2?rows=40&page=1&sidx=&sord=asc&freeTextSearchString=&take=40&currency=&timeSpanStartDate=&timeSpanEndDate=&minAmount=&maxAmount=&purchaseType=&purchasePart=&purchaseStatus=&_=%s", System.currentTimeMillis()), new ArrayList<NameValuePair>(), false);
            TransactionHistory thistory = readJsonValue(httpResponse, TransactionHistory.class);

            Account account = new Account("Saldo", Helpers.parseBalance(user.getBalance()), "1");
            String currency = Helpers.parseCurrency(user.getBalance(), "SEK");
            account.setCurrency(currency);
            setCurrency(currency);
            accounts.add(account);
            balance = balance.add(account.getBalance());

            if (thistory != null && thistory.getRows() != null) {
                ArrayList<Transaction> transactions = new ArrayList<Transaction>();
                for (com.liato.bankdroid.banking.banks.payson.model.Transaction transaction : thistory.getRows()) {
                    String date = transaction.getCreatedDate().substring(0, 10);
                    String description = !TextUtils.isEmpty(transaction.getMessage()) ? transaction.getMessage() : transaction.getSummary();

                    Transaction t = new Transaction(date, Html.fromHtml(description).toString(), Helpers.parseBalance(transaction.getAmount()));
                    t.setCurrency(Helpers.parseCurrency(transaction.getCurrencySymbol(), account.getCurrency()));
                    transactions.add(t);
                }
                account.setTransactions(transactions);

            }
            if (accounts.isEmpty()) {
                throw new BankException(res.getText(R.string.no_accounts_found).toString());
            }
        } catch (JsonParseException e) {
            e.printStackTrace();
            throw new BankException(e.getMessage());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            throw new BankException(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new BankException(e.getMessage());
        }
        super.updateComplete();
    }

    private <T> T readJsonValue(HttpResponse response, Class<T> valueType) throws BankException {
        try {
            return readJsonValue(response.getEntity().getContent(), valueType);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private <T> T readJsonValue(InputStream is, Class<T> valueType) throws BankException {
        try {
            return getObjectmapper().readValue(is, valueType);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private ObjectMapper getObjectmapper() {
        if (mObjectMapper == null) {
            mObjectMapper = new ObjectMapper();
            mObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mObjectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        }
        return mObjectMapper;
    }
}
