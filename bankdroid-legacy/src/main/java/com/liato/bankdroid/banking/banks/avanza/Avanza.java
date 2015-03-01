/*
 * Copyright (C) 2014 Nullbyte <http://nullbyte.eu>
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

package com.liato.bankdroid.banking.banks.avanza;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.legacy.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.Transaction;
import com.liato.bankdroid.banking.banks.avanza.model.AccountOverview;
import com.liato.bankdroid.banking.banks.avanza.model.Position;
import com.liato.bankdroid.banking.banks.avanza.model.PositionAggregation;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class Avanza extends Bank {
    private static final String API_URL = "https://iphone.avanza.se/iphone-ws/";

    public Avanza(Context context) {
        super(context);
        TAG = "Avanza";
        NAME = "Avanza";
        NAME_SHORT = "avanza";
        URL = "https://iphone.avanza.se";
        BANKTYPE_ID = IBankTypes.AVANZA;
    }

    public Avanza(String username, String password, Context context)
            throws BankException, LoginException, BankChoiceException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
        urlopen = new Urllib(context, CertificateReader.getCertificates(context, R.raw.cert_avanza));
        urlopen.addHeader("Referer", URL + "/start");
        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("j_username", username));
        postData.add(new BasicNameValuePair("j_password", password));
        postData.add(new BasicNameValuePair("url", URL + "/start"));
        String response = urlopen.open(URL + "/ab/handlelogin", postData);
        String homeUrl = "";
        try {
            JSONObject jsonResponse = new JSONObject(response);
            homeUrl = jsonResponse.getString("redirectUrl");
        } catch (JSONException e) {
            throw new BankException(res.getString(R.string.invalid_username_password));
        }
        LoginPackage lp = new LoginPackage(urlopen, postData, "", URL + homeUrl);
        lp.setIsLoggedIn(true);
        return lp;
    }

    public Urllib login() throws LoginException, BankException {
        urlopen = new Urllib(context, CertificateReader.getCertificates(context, R.raw.cert_avanza));
        urlopen.addHeader("ctag", "1122334455");
        urlopen.addHeader("Authorization", "Basic " + Base64.encodeToString(new String(username + ":" + password).getBytes(), Base64.NO_WRAP));
        balance = new BigDecimal(0);

        try {
            HttpResponse httpResponse = urlopen.openAsHttpResponse(API_URL + "account/overview/all", new ArrayList<NameValuePair>(), false);
            if (httpResponse.getStatusLine().getStatusCode() == 401) {
                throw new LoginException(context.getText(
                        R.string.invalid_username_password).toString());
            }
            ObjectMapper vObjectMapper = new ObjectMapper();
            AccountOverview r = vObjectMapper.readValue(httpResponse.getEntity().getContent(), AccountOverview.class);
            for (com.liato.bankdroid.banking.banks.avanza.model.Account account : r.getAccounts()) {
                Account a = new Account(account.getAccountName(), new BigDecimal(account.getOwnCapital()), account.getAccountId());
                if (!account.getCurrencyAccounts().isEmpty()) {
                    a.setCurrency(account.getCurrencyAccounts().get(0).getCurrency());
                }
                if (!account.getPositionAggregations().isEmpty()) {
                    Date now = new Date();
                    ArrayList<Transaction> transactions = new ArrayList<Transaction>();
                    for (com.liato.bankdroid.banking.banks.avanza.model.CurrencyAccount currencyAccount : account.getCurrencyAccounts()) {
                        transactions.add(new Transaction(Helpers.formatDate(now),
                                "\u2014  " + currencyAccount.getCurrency() + "  \u2014",
                                BigDecimal.valueOf(currencyAccount.getBalance()),
                                currencyAccount.getCurrency()));
                    }
                    for (PositionAggregation positionAgList : account.getPositionAggregations()) {
                        if (positionAgList.getPositions().isEmpty()) {
                            continue;
                        }
                        List<Position> positions = positionAgList.getPositions();
                        transactions.add(new Transaction(Helpers.formatDate(now),
                                "\u2014  " + positionAgList.getInstrumentTypeName() +
                                        "  " + positionAgList.getTotalProfitPercent() + "%  \u2014",
                                BigDecimal.valueOf(positionAgList.getTotalValue()),
                                a.getCurrency()));
                        for (Position p : positions) {
                            Transaction t = new Transaction(Helpers.formatDate(now),
                                    p.getInstrumentName(),
                                    BigDecimal.valueOf(p.getProfit()),
                                    a.getCurrency());
                            transactions.add(t);
                        }
                    }
                    a.setTransactions(transactions);
                }
                balance = balance.add(a.getBalance());
                accounts.add(a);
                // Add subtypes for account as own account.
                if (!account.getPositionAggregations().isEmpty()) {
                    Date now = new Date();
                    for (com.liato.bankdroid.banking.banks.avanza.model.CurrencyAccount currencyAccount : account.getCurrencyAccounts()) {
                        Account b = new Account("\u2014  " + account.getAccountId() + ",  " +
                                currencyAccount.getCurrency(),
                                new BigDecimal(currencyAccount.getBalance()),
                                account.getAccountId() + currencyAccount.getCurrency(),
                                Account.OTHER,
                                currencyAccount.getCurrency());
                        b.setHidden(true);
                        accounts.add(b);
                    }
                    for (PositionAggregation positionAgList : account.getPositionAggregations()) {
                        if (positionAgList.getPositions().isEmpty()) {
                            continue;
                        }
                        Account b = new Account("\u2014  " + account.getAccountId() + ",  " +
                                positionAgList.getInstrumentTypeName() +
                                "  " + positionAgList.getTotalProfitPercent() + "% ",
                                new BigDecimal(positionAgList.getTotalValue()),
                                account.getAccountId() + positionAgList.getInstrumentTypeName(),
                                Account.OTHER, a.getCurrency());
                        b.setHidden(true);
                        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
                        for (Position p : positionAgList.getPositions()) {
                            transactions.add(new Transaction(Helpers.formatDate(now),
                                    p.getInstrumentName(),
                                    BigDecimal.valueOf(p.getProfit()),
                                    a.getCurrency()));
                        }
                        b.setTransactions(transactions);
                        accounts.add(b);
                    }
                }
            }
        } catch (JsonParseException e) {
            throw new BankException(e.getMessage(), e);
        } catch (ClientProtocolException e) {
            throw new BankException(e.getMessage(), e);
        } catch (IOException e) {
            throw new BankException(e.getMessage(), e);
        } catch (Exception e) {
            throw new BankException(e.getMessage(), e);
        }
        return urlopen;
    }

    @Override
    public void update() throws BankException, LoginException,
            BankChoiceException {
        super.update();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            throw new LoginException(res.getText(
                    R.string.invalid_username_password).toString());
        }
        login();
        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
        super.updateComplete();
    }
}
