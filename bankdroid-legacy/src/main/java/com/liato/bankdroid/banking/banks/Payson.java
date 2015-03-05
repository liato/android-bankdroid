/*
 * Copyright (C) 2010-2015 Nullbyte <http://nullbyte.eu>
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
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.legacy.R;
import com.liato.bankdroid.provider.IBankTypes;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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

    private Pattern reVerificationToken = Pattern.compile("<input[^>]+name=\"__RequestVerificationToken\"[^>]+value=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    private String response = null;
    private JSONObject userInfo = null;

    public Payson(Context context) {
        super(context);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
    }

    public Payson(String username, String password, Context context) throws BankChoiceException,
            BankException, LoginException, IOException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        urlopen = new Urllib(context, CertificateReader.getCertificates(context, R.raw.cert_payson));
        response = urlopen.open(URL);
        Matcher matcher = reVerificationToken.matcher(response);
        if (!matcher.find()) {
            throw new BankException(res.getText(R.string.unable_to_find).toString() + " RequestVerificationToken");
        }
        String verificationToken = matcher.group(1);

        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("__RequestVerificationToken", verificationToken));
        postData.add(new BasicNameValuePair("Username", username));
        postData.add(new BasicNameValuePair("Password", password));
        postData.add(new BasicNameValuePair("RedirectAfterLogin", ""));
        return new LoginPackage(urlopen, postData, response, "https://www.payson.se/myaccount/account/SignIn/");
    }

    @Override
    public Urllib login() throws LoginException, BankException, IOException {
        try {
            LoginPackage lp = preLogin();
            response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
            userInfo = new JSONObject(urlopen.open("https://www.payson.se/myaccount/user/getuserinfo"));
        } catch (JSONException e) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }
        return urlopen;
    }

    @Override
    public void update() throws BankChoiceException, BankException, LoginException, IOException {
        super.update();
        if (username == null || password == null || username.length() == 0 || password.length() == 0) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }
        urlopen = login();

        try {
            Account account = new Account(res.getText(R.string.balance).toString(),
                    Helpers.parseBalance(userInfo.getString("balance")), "1");
            accounts.add(account);
        } catch (JSONException e) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString(), e);
        }
        super.updateComplete();
    }
}
