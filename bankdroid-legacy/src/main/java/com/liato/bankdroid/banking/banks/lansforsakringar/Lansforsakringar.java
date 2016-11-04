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

package com.liato.bankdroid.banking.banks.lansforsakringar;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.Transaction;
import com.liato.bankdroid.banking.banks.lansforsakringar.model.request.AccountsRequest;
import com.liato.bankdroid.banking.banks.lansforsakringar.model.request.ChallengeRequest;
import com.liato.bankdroid.banking.banks.lansforsakringar.model.request.LoginRequest;
import com.liato.bankdroid.banking.banks.lansforsakringar.model.request.TransactionsRequest;
import com.liato.bankdroid.banking.banks.lansforsakringar.model.response.AccountsResponse;
import com.liato.bankdroid.banking.banks.lansforsakringar.model.response.ChallengeResponse;
import com.liato.bankdroid.banking.banks.lansforsakringar.model.response.LoginResponse;
import com.liato.bankdroid.banking.banks.lansforsakringar.model.response.NumberResponse;
import com.liato.bankdroid.banking.banks.lansforsakringar.model.response.TransactionsResponse;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.legacy.R;
import com.liato.bankdroid.provider.IBankTypes;
import com.liato.bankdroid.utils.StringUtils;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.InputType;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;
import timber.log.Timber;

public class Lansforsakringar extends Bank {

    private static final String NAME = "Länsförsäkringar";

    private static final int BANKTYPE_ID = IBankTypes.LANSFORSAKRINGAR;

    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;

    private static final int INPUT_TYPE_PASSWORD = InputType.TYPE_CLASS_PHONE;

    private static final String INPUT_HINT_USERNAME = "ÅÅÅÅMMDDXXXX";

    private static final String API_BASEURL = "https://mobil.lansforsakringar.se/appoutlet/";

    private ObjectMapper mObjectMapper = new ObjectMapper();

    private HashMap<String, String> mAccountLedger = new HashMap<>();

    public Lansforsakringar(Context context) {
        super(context, R.drawable.logo_lansforsakringar);

        super.inputTypeUsername = INPUT_TYPE_USERNAME;
        super.inputTypePassword = INPUT_TYPE_PASSWORD;
        super.inputHintUsername = INPUT_HINT_USERNAME;
        mObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mObjectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        mObjectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }

    @Override
    public int getBanktypeId() {
        return BANKTYPE_ID;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public Urllib login() throws LoginException, BankException, IOException {
        urlopen = new Urllib(context,
                CertificateReader.getCertificates(context, R.raw.cert_lansforsakringar));
        urlopen.addHeader("Content-Type", "application/json; charset=UTF-8");
        urlopen.addHeader("DeviceId", UUID.randomUUID().toString());
        urlopen.addHeader("deviceInfo", "Galaxy Nexus;4.1.1;1.8;Portrait");
        urlopen.addHeader("Accept", "application/json; charset=UTF-8");
        //TODO: Change user-agent to "lf-android-app" if they block Bankdroid
        //urlopen.setUserAgent("lf-android-app");

        NumberResponse nr = readJsonValue(API_BASEURL + "security/client", null,
                NumberResponse.class);
        ChallengeResponse cr = readJsonValue(API_BASEURL + "security/client", objectAsJson(
                new ChallengeRequest(nr.getNumber(), nr.getNumberPair(),
                        generateChallenge(nr.getNumber()))), ChallengeResponse.class);
        urlopen.addHeader("Ctoken", cr.getToken());
        try {
            LoginResponse lr = readJsonValue(API_BASEURL + "security/user",
                    objectAsJson(new LoginRequest(getUsername(), getPassword())), LoginResponse.class);
            urlopen.addHeader("Utoken", lr.getTicket());
        } catch (Exception e) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString(), e);
        }
        return urlopen;
    }


    private <T> T readJsonValue(InputStream is, Class<T> valueType) throws BankException, IOException {
        try {
            return mObjectMapper.readValue(is, valueType);
        } catch (JsonParseException | JsonMappingException e) {
            throw new BankException(e.getMessage(), e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Timber.w(e, "Closing JSON stream failed");
            }
        }
    }

    private <T> T readJsonValue(String url, String postData, Class<T> valueType)
            throws BankException, IOException {
        return readJsonValue(urlopen.openStream(url, postData, false), valueType);
    }

    @Nullable
    private String objectAsJson(Object value) {
        try {
            return mObjectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            Timber.w(e, "Failed converting Object to JSON");
        }
        return null;
    }

    private String generateChallenge(int originalChallenge) {
        try {
            String h = Integer.toHexString(
                    originalChallenge + (1000 * 20 / 4) + 100 * (18 / 3) + 10 * (2 / 2) + 6);
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(StringUtils.getBytes(h));
            BigInteger number = new BigInteger(1, messageDigest);
            String md5 = number.toString(16);
            while (md5.length() < 40) {
                md5 = "0" + md5;
            }
            return md5;
        } catch (NoSuchAlgorithmException e) {
            Timber.w(e, "Länsförsäkringar: Error generating challenge");
        }
        return "";

    }

    @Override
    public void update() throws BankException, LoginException, BankChoiceException, IOException {
        super.update();
        if (getUsername().isEmpty() || getPassword().isEmpty()) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }

        urlopen = login();

        mAccountLedger.clear();
        AccountsResponse ar = readJsonValue(API_BASEURL + "account/bytype",
                objectAsJson(new AccountsRequest(AccountsRequest.Type.CHECKING)),
                AccountsResponse.class);
        for (com.liato.bankdroid.banking.banks.lansforsakringar.model.response.Account a : ar
                .getAccounts()) {
            accounts.add(new Account(a.getAccountName(), new BigDecimal(a.getBalance()),
                    a.getAccountNumber()));
            //a.getLedger() should be saved to database, used when fetching transactions
            mAccountLedger.put(a.getAccountNumber(), a.getLedger());
            balance = balance.add(new BigDecimal(a.getBalance()));
        }
        ar = readJsonValue(API_BASEURL + "account/bytype",
                objectAsJson(new AccountsRequest(AccountsRequest.Type.SAVING)),
                AccountsResponse.class);
        for (com.liato.bankdroid.banking.banks.lansforsakringar.model.response.Account a : ar
                .getAccounts()) {
            accounts.add(new Account(a.getAccountName(), new BigDecimal(a.getBalance()),
                    a.getAccountNumber()));
            mAccountLedger.put(a.getAccountNumber(), a.getLedger());
            balance = balance.add(new BigDecimal(a.getBalance()));
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
        // No transaction history for funds and loans
        if (account.getType() != Account.REGULAR) {
            return;
        }

        ArrayList<Transaction> transactions = new ArrayList<>();
        //TODO: Get upcoming transactions?
        //TransactionsResponse tr = readJsonValue(API_BASEURL + "account/upcoming", objectAsJson(new UpcomingTransactionsRequest(account.getId())), TransactionsResponse.class);

        try {
            TransactionsResponse tr = readJsonValue(API_BASEURL + "account/transaction",
                    objectAsJson(new TransactionsRequest(0,
                            mAccountLedger.containsKey(account.getId()) ? mAccountLedger
                                    .get(account.getId()) : "DEPIOSIT", account.getId())),
                    TransactionsResponse.class);
            for (com.liato.bankdroid.banking.banks.lansforsakringar.model.response.Transaction t : tr
                    .getTransactions()) {
                //TODO: Set locale to Europe/Stockholm on date?
                transactions
                        .add(new Transaction(Helpers.formatDate(new Date(t.getTransactiondate())),
                                t.getText(), new BigDecimal(t.getAmmount())));
            }
            account.setTransactions(transactions);
        } catch (BankException e) {
            Timber.e(e, "Failed updating Länsförsäkringar transactions");
        }

        super.updateComplete();
    }
}
