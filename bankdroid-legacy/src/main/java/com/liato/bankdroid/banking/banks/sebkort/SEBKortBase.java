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

package com.liato.bankdroid.banking.banks.sebkort;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.Transaction;
import com.liato.bankdroid.banking.banks.sebkort.model.BillingUnit;
import com.liato.bankdroid.banking.banks.sebkort.model.CardGroup;
import com.liato.bankdroid.banking.banks.sebkort.model.TransactionGroup;
import com.liato.bankdroid.banking.banks.sebkort.model.response.BillingUnitsResponse;
import com.liato.bankdroid.banking.banks.sebkort.model.response.LoginResponse;
import com.liato.bankdroid.banking.banks.sebkort.model.response.PendingTransactionsResponse;
import com.liato.bankdroid.banking.banks.sebkort.model.response.UserResponse;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.legacy.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public abstract class SEBKortBase extends Bank {

    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;

    private static final String INPUT_HINT_USERNAME = "ÅÅMMDDXXXX";

    private static final boolean STATIC_BALANCE = true;

    private ObjectMapper mObjectMapper = new ObjectMapper();

    private String response = null;

    private String mProviderPart;

    private String mProdgroup;

    private String mApiBase;

    private int[] mCertificates;

    private BasicNameValuePair mParamsTarget;

    private BasicNameValuePair mParamsErrorTarget;

    private Map<Account, String> mBillingUnitIds = new HashMap<Account, String>();


    public SEBKortBase(Context context, String providerPart, String prodgroup, @DrawableRes int logoResource) {
        this(context, providerPart, prodgroup, "secure.sebkort.com", new int[]{R.raw.cert_sebkort}, logoResource);
    }

    public SEBKortBase(Context context, String providerPart, String prodgroup, String apiBase,
            int[] certificates, @DrawableRes int logoResource) {
        super(context, logoResource);
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
        super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
        super.STATIC_BALANCE = STATIC_BALANCE;
        super.URL = String
                .format("https://%s/nis/m/%s/external/t/login/index", apiBase, providerPart);
        mProviderPart = providerPart;
        mProdgroup = prodgroup;
        mApiBase = apiBase;
        mCertificates = certificates;
        mObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mObjectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        mParamsTarget = new BasicNameValuePair("target", URL);
        mParamsErrorTarget = new BasicNameValuePair("errorTarget", URL);
    }

    public SEBKortBase(String username, String password, Context context, String url,
            String prodgroup, @DrawableRes int logoResource)
            throws BankException, LoginException, BankChoiceException, IOException {
        this(context, url, prodgroup, logoResource);
        this.update(username, password);
    }

    public SEBKortBase(String username, String password, Context context, String url,
            String prodgroup, String apiBase, int[] certificates, @DrawableRes int logoResource)
            throws BankException, LoginException, BankChoiceException, IOException {
        this(context, url, prodgroup, apiBase, certificates, logoResource);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        urlopen = new Urllib(context, CertificateReader.getCertificates(context, mCertificates));
        //Get required cookies
        response = urlopen.open(String
                .format("https://%s/nis/m/%s/external/t/login/index", mApiBase, mProviderPart));
        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.clear();
        postData.add(new BasicNameValuePair("SEB_Referer", "/nis"));
        postData.add(new BasicNameValuePair("SEB_Auth_Mechanism", "5"));
        postData.add(new BasicNameValuePair("TYPE", "LOGIN"));
        postData.add(new BasicNameValuePair("CURRENT_METHOD", "PWD"));
        postData.add(new BasicNameValuePair("UID", mProdgroup + getUsername().toUpperCase()));
        postData.add(new BasicNameValuePair("PASSWORD", getPassword()));
        postData.add(new BasicNameValuePair("mProdgroup", mProdgroup));
        postData.add(mParamsTarget);
        postData.add(mParamsErrorTarget);
        return new LoginPackage(urlopen, postData, response,
                String.format("https://%s/auth4/Authentication/select.jsp", mApiBase));
    }

    @Override
    public Urllib login() throws LoginException, BankException, IOException {
        LoginPackage lp = preLogin();
        urlopen.addHeader("Origin", String.format("https://%s", mApiBase));
        urlopen.addHeader("Referer",
                String.format("https://%s/nis/m/%s/external/t/login/index", mApiBase,
                        mProviderPart));
        urlopen.addHeader("X-Requested-With", "XMLHttpRequest");
        List<NameValuePair> postData = lp.getPostData();
        postData.remove(mParamsTarget);
        postData.remove(mParamsErrorTarget);
        postData.add(new BasicNameValuePair("target",
                String.format("/nis/m/%s/login/loginSuccess", mProviderPart)));
        postData.add(new BasicNameValuePair("errorTarget",
                String.format("/nis/m/%s/external/login/loginError", mProviderPart)));

        LoginResponse r = mObjectMapper
                .readValue(urlopen.openStream(lp.getLoginTarget(), postData, true),
                        LoginResponse.class);
        if ("Failure".equalsIgnoreCase(r.getReturnCode())) {
            throw new LoginException(
                    !TextUtils.isEmpty(r.getMessage()) ? Html.fromHtml(r.getMessage()).toString()
                            : res.getText(R.string.invalid_username_password).toString());
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
        UserResponse ur = mObjectMapper.readValue(urlopen.openStream(
                String.format("https://%s/nis/m/%s/a/user", mApiBase, mProviderPart)),
                UserResponse.class);
        BillingUnitsResponse br = mObjectMapper.readValue(urlopen.openStream(
                String.format("https://%s/nis/m/%s/a/billingUnits", mApiBase, mProviderPart)),
                BillingUnitsResponse.class);

        boolean multipleAccounts = br.getBody().size() > 1;
        for (BillingUnit bu : br.getBody()) {
            Account account = new Account(
                    formatAccountName(bu.getArrangementNumber(), "Disponibelt belopp",
                            multipleAccounts), Helpers.parseBalance(bu.getDisposableAmount()),
                    bu.getArrangementNumber());
            account.setType(Account.CCARD);
            account.setCurrency(currency);
            mBillingUnitIds.put(account, bu.getBillingUnitId());
            accounts.add(account);
            balance = balance.add(account.getBalance());
            account = new Account(
                    formatAccountName(bu.getArrangementNumber(), "Saldo", multipleAccounts),
                    Helpers.parseBalance(bu.getBalance()), bu.getArrangementNumber() + "_2");
            account.setType(Account.OTHER);
            account.setAliasfor(bu.getArrangementNumber());
            account.setCurrency(currency);
            accounts.add(account);
            account = new Account(
                    formatAccountName(bu.getArrangementNumber(), "Köpgräns", multipleAccounts),
                    Helpers.parseBalance(bu.getCreditAmountNumber()),
                    bu.getArrangementNumber() + "_3");
            account.setType(Account.OTHER);
            account.setAliasfor(bu.getArrangementNumber());
            account.setCurrency(currency);
            accounts.add(account);
        }

        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
        super.updateComplete();
    }

    private String formatAccountName(String accountNumber, String name,
            boolean includeAccountNnumber) {
        return includeAccountNnumber ? String.format("%s (%s)", accountNumber, name) : name;
    }

    @Override
    public void updateTransactions(Account account, Urllib urlopen) throws LoginException,
            BankException, IOException {
        super.updateTransactions(account, urlopen);
        if (account.getType() != Account.CCARD) {
            return;
        }

        PendingTransactionsResponse r = mObjectMapper.readValue(urlopen.openStream(
                String.format("https://%s/nis/m/%s/a/pendingTransactions/%s", mApiBase,
                        mProviderPart, mBillingUnitIds.get(account))),
                PendingTransactionsResponse.class);
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        for (CardGroup cg : r.getBody().getCardGroups()) {
            for (TransactionGroup tg : cg.getTransactionGroups()) {
                for (com.liato.bankdroid.banking.banks.sebkort.model.Transaction t : tg
                        .getTransactions()) {
                    transactions.add(new Transaction(
                            Helpers.formatDate(new Date(t.getOriginalAmountDateDate())),
                            t.getDescription(), BigDecimal.valueOf(t.getAmountNumber()).negate(),
                            account.getCurrency()));
                }
            }
        }
        account.setTransactions(transactions);
    }
}
