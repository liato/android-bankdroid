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
import android.text.InputType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;
import timber.log.Timber;

public class Bredband2VoIP extends Bank {

    private static final String API_URL = "https://portal.bredband2.com/";

    //<a href="/services/digisipbalance/iServiceID/~ID~/" class="digisipBalance" target="_blank">Saldo</a>
    private Pattern reSaldoUrl = Pattern.compile(
            "<a href=\"/services/digisipbalance/iServiceID/(\\d+)/\" class=\"digisipBalance\" target=\"_blank\">Saldo</a>",
            Pattern.CASE_INSENSITIVE);

    private Pattern reSaldo = Pattern.compile("<td class=\"white\">\\s+(\\d+.\\d{2}) kr",
            Pattern.CASE_INSENSITIVE);

    private Pattern reInvoiceUrl = Pattern.compile("<a href=\"([^\"]+)\" class=\"invoice\"");

    private Pattern reTransactions = Pattern.compile(
            "^      ([\\d-]+)\\s+([\\S]+)\\s+([\\S]+)\\s+([\\d:]+)\\s+([\\d\\.]+)", Pattern.MULTILINE);

    private String response = null;

    public Bredband2VoIP(Context context) {
        super(context, R.drawable.logo_bredband2voip);
        super.inputTypeUsername = InputType.TYPE_CLASS_PHONE;
        super.inputHintUsername = "19XXXXXX-XXXX";
    }

    @Override
    public int getBanktypeId() {
        return IBankTypes.BREDBAND2VOIP;
    }

    @Override
    public String getName() {
        return "Bredband2 VoIP";
    }

    public Bredband2VoIP(String username, String password, Context context)
            throws BankException, LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        urlopen = new Urllib(context,
                CertificateReader.getCertificates(context, R.raw.cert_bredband2));
        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("cUsername", getUsername()));
        postData.add(new BasicNameValuePair("cPassword", getPassword()));
        postData.add(new BasicNameValuePair("bIsCompany", "0"));
        postData.add(new BasicNameValuePair("submit", "Logga in"));
        response = urlopen.open(API_URL + "index/", postData, true);
        LoginPackage lp = new LoginPackage(urlopen, postData, response, API_URL + "start");
        if (response.contains("Logga ut")) {
            lp.setIsLoggedIn(true);
        }
        return lp;
    }

    @Override
    public Urllib login() throws LoginException, BankException, IOException {
        LoginPackage lp = preLogin();
        if (!lp.isLoggedIn()) {
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

        response = urlopen.open(API_URL + "services/");
        Matcher mSaldoUrl = reSaldoUrl.matcher(response);
        while (mSaldoUrl.find()) {
            String account = mSaldoUrl.group(1);
            String r = urlopen.open(
                    API_URL + "/services/digisipbalance/iServiceID/" + account + "/");
            Matcher mSaldo = reSaldo.matcher(r);
            if (mSaldo.find()) {
                accounts.add(new Account(account,
                        Helpers.parseBalance(mSaldo.group(1)),
                        account));
            }
        }
        super.updateComplete();
    }

    @Override
    public void updateTransactions(Account account, Urllib urlopen) throws LoginException,
            BankException, IOException {
        super.updateTransactions(account, urlopen);

        //<a href="/services/invoicelist/iServiceID/~ID~/">Samtal</a>
        response = urlopen.open(API_URL + "services/invoicelist/iServiceID/" + account.getId());
        Matcher mInvoiceUrl = reInvoiceUrl.matcher(response);
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        int i = 1;
        while (mInvoiceUrl.find() && (i++ <= 4 || transactions.size() <= 30)) {
            try {
                String url = mInvoiceUrl.group(1);
                String sInvoice = urlopen.open(API_URL + url);
                Matcher mTransaction = reTransactions.matcher(sInvoice);
                while (mTransaction.find()) {
                    transactions.add(new Transaction(mTransaction.group(2),
                            mTransaction.group(1) + "  —  " + mTransaction.group(4),
                            Helpers.parseBalance(mTransaction.group(5)).negate()));
                }
            } catch (Exception e) {
                Timber.w(e, "Unable to parse: %s", mInvoiceUrl.group(1));
            }
        }
        account.setTransactions(transactions);
    }
}
