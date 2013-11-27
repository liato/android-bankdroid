/*
 * Copyright (C) 2013 Nullbyte <http://nullbyte.eu>
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
import com.liato.bankdroid.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.Transaction;
import com.liato.bankdroid.banking.banks.avanza.model.AccountOverview;
import com.liato.bankdroid.banking.banks.avanza.model.Position;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class Avanza extends Bank {
    private static final String TAG = "Avanza";
    private static final String NAME = "Avanza";
    private static final String NAME_SHORT = "avanza";
    private static final String URL = "https://www.avanza.se/";
    private static final int BANKTYPE_ID = IBankTypes.AVANZA;

    private static final String API_URL = "https://iphone.avanza.se/iphone-ws/";

    public Avanza(Context context) {
        super(context);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
    }

    public Avanza(String username, String password, Context context)
            throws BankException, LoginException, BankChoiceException {
        this(context);
        this.update(username, password);
    }

    public Urllib login() throws LoginException, BankException {
        urlopen = new Urllib(context, CertificateReader.getCertificates(context, R.raw.cert_avanza));
        urlopen.addHeader("ctag", "1122334455");
        urlopen.addHeader("Authorization", "Basic " + Base64.encodeToString(new String(username + ":" + password).getBytes(), Base64.NO_WRAP));

        try {
            HttpResponse httpResponse = urlopen.openAsHttpResponse(API_URL + "account/overview/all", null, false);
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
                if (!account.getPositionAggregations().isEmpty() && !account.getPositionAggregations().get(0).getPositions().isEmpty()) {
                    List<Position> positions = account.getPositionAggregations().get(0).getPositions();
                    Date now = new Date();
                    ArrayList<Transaction> transactions = new ArrayList<Transaction>(positions.size());
                    for (Position p : positions) {
                        Transaction t = new Transaction(Helpers.formatDate(now), p.getInstrumentName(), BigDecimal.valueOf(p.getProfit()), a.getCurrency());
                        transactions.add(t);
                    }
                    a.setTransactions(transactions);
                }
                accounts.add(a);
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
            throw new BankException(res.getText(R.string.no_accounts_found)
                    .toString());
        }
        super.updateComplete();
    }
}
