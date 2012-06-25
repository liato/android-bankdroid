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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;

import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.Transaction;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;

import eu.nullbyte.android.urllib.Urllib;

public class Bioklubben extends Bank {
	private static final String TAG = "Bioklubben";
	private static final String NAME = "Bioklubben";
	private static final String NAME_SHORT = "bioklubben";
	private static final String URL = "http://bioklubben.sfbio.se/user/login";
	private static final int BANKTYPE_ID = Bank.BIOKLUBBEN;

    private Pattern reViewState = Pattern.compile("__VIEWSTATE\"\\s+value=\"([^\"]+)\"");
    private Pattern reEventValidation = Pattern.compile("__EVENTVALIDATION\"\\s+value=\"([^\"]+)\"");
    private Pattern reBalance = Pattern.compile("pointsblock-bignumber-bn\">([^<]+)</div>");

    private String response = null;
    
    public Bioklubben(Context context) {
        super(context);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
        currency = context.getString(R.string.points);
    }

    public Bioklubben(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
        this(context);
        this.update(username, password);
    }

    
    @Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
        urlopen = new Urllib();
        /*
        response = urlopen.open("https://www.edenred.se/sv/System/Logga-in/");
        Matcher matcher = reViewState.matcher(response);
        if (!matcher.find()) {
            throw new BankException(res.getText(R.string.unable_to_find).toString()+" ViewState.");
        }
        String viewState = matcher.group(1);

        matcher = reEventValidation.matcher(response);
        if (!matcher.find()) {
            throw new BankException(res.getText(R.string.unable_to_find).toString()+" EventValidation.");
        }
        String eventValidation = matcher.group(1);            
        */
        
        List <NameValuePair> postData = new ArrayList <NameValuePair>();
        postData.add(new BasicNameValuePair("name", username));
        postData.add(new BasicNameValuePair("pass", password));
        postData.add(new BasicNameValuePair("form_build_id", "form-5fab45c2b5bcacfe564ef96cf8329a82"));
        postData.add(new BasicNameValuePair("form_id", "user_login"));
        postData.add(new BasicNameValuePair("op", "Logga in"));
        postData.add(new BasicNameValuePair("persistent_login", "1"));
        return new LoginPackage(urlopen, postData, response, "https://bioklubben.sfbio.se/user/login");
    }

    public Urllib login() throws LoginException, BankException {
        try {
            LoginPackage lp = preLogin();
            response = urlopen.open(lp.getLoginTarget(), lp.getPostData());         
            if (response.contains("Felaktigt anv")) {
                throw new LoginException(res.getText(R.string.invalid_username_password).toString());
            }
        }
        catch (ClientProtocolException e) {
            throw new BankException(e.getMessage());
        }
        catch (IOException e) {
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
        Matcher matcher = reBalance.matcher(response);
        if (matcher.find()) {
            /*
             * Capture groups:
             * GROUP                EXAMPLE DATA
             * 1: Balance           218
             * 
             */
            BigDecimal b = Helpers.parseBalance(matcher.group(1));
            Account a = new Account("Poäng", b, "1");
            a.setCurrency(context.getString(R.string.points));
            accounts.add(a);
            balance = balance.add(Helpers.parseBalance(matcher.group(1)));
        }
        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
        super.updateComplete();
    }
}