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

package com.liato.bankdroid.banking.banks.ica;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.Transaction;
import com.liato.bankdroid.banking.banks.ica.model.LoginError;
import com.liato.bankdroid.banking.banks.ica.model.Overview;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.legacy.R;
import com.liato.bankdroid.provider.IBankTypes;
import com.liato.bankdroid.utils.StringUtils;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;

import android.content.Context;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class ICA extends Bank {

    private static final String API_URL = "https://api.ica.se/api/";

    private static final String AUTHENTICATION_TICKET_HEADER = "AuthenticationTicket";

    private static final String SESSION_TICKET_HEADER = "SessionTicket";

    private static final String LOGOUT_KEY_HEADER = "LogoutKey";

    private ObjectMapper mObjectMapper = new ObjectMapper();

    private Map<String, String> mHeaders = new HashMap<String, String>();

    public ICA(Context context) {
        super(context, R.drawable.logo_ica);
        url = "http://mobil.ica.se/";
        inputTypeUsername = InputType.TYPE_CLASS_PHONE;
        inputTypePassword = InputType.TYPE_CLASS_PHONE;
        inputHintUsername = "ÅÅMMDDXXXX";
        mHeaders.put(AUTHENTICATION_TICKET_HEADER, null);
        mHeaders.put(SESSION_TICKET_HEADER, null);
        mHeaders.put(LOGOUT_KEY_HEADER, null);
    }

    @Override
    public int getBanktypeId() {
        return IBankTypes.ICA;
    }

    @Override
    public String getName() {
        return "ICA";
    }

    public ICA(String username, String password, Context context)
            throws BankException, LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }

    public Urllib login() throws LoginException, BankException, IOException {
        urlopen = new Urllib(context, CertificateReader.getCertificates(context, R.raw.cert_ica));
        urlopen.addHeader("Accept", "application/json;charset=UTF-8");
        urlopen.addHeader("Content-Type", "application/json;charset=UTF-8");
        urlopen.addHeader("Authorization", "Basic " + Base64.encodeToString(
                StringUtils.getBytes(getUsername() + ":" + getPassword()), Base64.NO_WRAP));

        try {
            HttpResponse httpResponse = urlopen.openAsHttpResponse(API_URL + "login",
                    new ArrayList<NameValuePair>(), false);
            if (httpResponse.getStatusLine().getStatusCode() == 401) {
                LoginError le = readJsonValue(httpResponse, LoginError.class);
                if (le != null && "UsernamePassword".equals(le.getMessageCode())) {
                    if (!TextUtils.isEmpty(le.getMessage())) {
                        throw new LoginException(le.getMessage());
                    } else {
                        throw new LoginException(context.getText(
                                R.string.invalid_username_password).toString());
                    }
                } else {
                    throw new BankException(context.getText(
                            R.string.invalid_username_password).toString());
                }
            }

            for (Map.Entry<String, String> entry : mHeaders.entrySet()) {
                Header header = httpResponse.getFirstHeader(entry.getKey());
                if (header == null || TextUtils.isEmpty(header.getValue())) {
                    throw new BankException(context.getString(
                            R.string.unable_to_find).toString() + " " + entry.getKey());
                }
                mHeaders.put(entry.getKey(), header.getValue());
            }

            urlopen.addHeader(AUTHENTICATION_TICKET_HEADER,
                    mHeaders.get(AUTHENTICATION_TICKET_HEADER));
            httpResponse = urlopen.openAsHttpResponse(API_URL + "user/minasidor",
                    new ArrayList<NameValuePair>(), false);
            Overview overview = readJsonValue(httpResponse, Overview.class);

            if (overview == null) {
                throw new BankException(context.getString(R.string.unable_to_find) + " overview.");
            }

            if (!TextUtils.isEmpty(overview.getAccountName())) {
                Account account = new Account(overview.getAccountName(),
                        BigDecimal.valueOf(overview.getAvailableAmount()),
                        overview.getAccountNumber());
                balance = balance.add(account.getBalance());
                accounts.add(account);
                List<Transaction> transactions = new ArrayList<Transaction>();
                for (com.liato.bankdroid.banking.banks.ica.model.Transaction t : overview
                        .getTransactions()) {
                    transactions.add(new Transaction(t.getTransactionDate(), t.getDescription(),
                            BigDecimal.valueOf(t.getAmount())));
                }
                account.setTransactions(transactions);
            }
            for (com.liato.bankdroid.banking.banks.ica.model.Account a : overview.getAccounts()) {
                Account account = new Account(a.getName(),
                        BigDecimal.valueOf(a.getAvailableAmount()), a.getAccountNumber());
                balance = balance.add(account.getBalance());
                accounts.add(account);
                List<Transaction> transactions = new ArrayList<Transaction>();
                for (com.liato.bankdroid.banking.banks.ica.model.Transaction t : a
                        .getTransactions()) {
                    transactions.add(new Transaction(t.getTransactionDate(), t.getDescription(),
                            BigDecimal.valueOf(t.getAmount())));
                }
                account.setTransactions(transactions);
            }

            Account account = new Account("Erhållen bonus i år",
                    BigDecimal.valueOf(overview.getAcquiredBonus()), "bonus");
            account.setType(Account.OTHER);
            accounts.add(account);
            account = new Account("Årets totala inköp på ICA",
                    BigDecimal.valueOf(overview.getYearlyTotalPurchased()), "totalpurchased");
            account.setType(Account.OTHER);
            accounts.add(account);

            if (accounts.isEmpty()) {
                throw new BankException(res.getText(R.string.no_accounts_found).toString());
            }

            urlopen.addHeader(LOGOUT_KEY_HEADER, mHeaders.get(LOGOUT_KEY_HEADER));
            httpResponse = urlopen.openAsHttpResponse(API_URL + "logout",
                    new ArrayList<NameValuePair>(), false);
            httpResponse.getStatusLine();
        } catch (JsonParseException e) {
            throw new BankException(e.getMessage(), e);
        }
        return urlopen;
    }

    @Override
    public void update() throws BankException, LoginException,
            BankChoiceException, IOException {
        super.update();
        if (getUsername().isEmpty() || getPassword().isEmpty()) {
            throw new LoginException(res.getText(
                    R.string.invalid_username_password).toString());
        }
        login();
        super.updateComplete();
    }

    private <T> T readJsonValue(HttpResponse response, Class<T> valueType) throws BankException {
        try {
            return readJsonValue(response.getEntity().getContent(), valueType);
        } catch (IOException e) {
            return null;
        }
    }

    private <T> T readJsonValue(InputStream is, Class<T> valueType) throws BankException {
        try {
            return mObjectMapper.readValue(is, valueType);
        } catch (Exception e) {
            return null;
        }
    }
}
