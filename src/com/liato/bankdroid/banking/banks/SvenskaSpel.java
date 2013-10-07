/*
 * Copyright (C) 2010 Nullbyte <http://nullbyte.eu>
 * Contributors: Jonathan Hjertström (jh@nixi.com)
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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.text.InputType;

import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class SvenskaSpel extends Bank {

	private static final String TAG = "SvenskaSpel";
	private static final String NAME = "Svenska Spel";
	private static final String NAME_SHORT = "svenskaspel";
	private static final String URL = "https://m.svenskaspel.se/Customer/Login?returnUrl=/MinaSpel";
	private static final int BANKTYPE_ID = Bank.SVENSKASPEL;
	private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_TEXT;
	private static final int INPUT_TITLETEXT_USERNAME = R.string.card_number;

    private Pattern reBalance = Pattern.compile("Balance\\\":\\\"([^<]+)\\\",", Pattern.CASE_INSENSITIVE);
	private String response = "";

	public SvenskaSpel(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = URL;
		super.INPUT_TYPE_USERNAME= INPUT_TYPE_USERNAME;
		super.INPUT_TITLETEXT_USERNAME = INPUT_TITLETEXT_USERNAME;
	}

	public SvenskaSpel(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
		this(context);
		this.update(username, password);
	}

	@Override
	protected LoginPackage preLogin() throws BankException, ClientProtocolException, IOException {
        urlopen = new Urllib(CertificateReader.getCertificates(context, R.raw.cert_svenskaspel));

		List<NameValuePair> postData = new ArrayList<NameValuePair>();
		postData.add(new BasicNameValuePair("username", username));
	    postData.add(new BasicNameValuePair("password", password));
	    postData.add(new BasicNameValuePair("loginbutton", "Logga in"));

		return new LoginPackage(urlopen, postData, response, "https://m.svenskaspel.se/Customer/Login?returnUrl=/MinaSpel");
	}

	@Override
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
             * GROUP                    EXAMPLE DATA
             * 1: balance               845
             * 
             */    		    
		   Account account = new Account("Saldo", Helpers.parseBalance(matcher.group(1)), "1");
           balance = Helpers.parseBalance(matcher.group(1));
           balance = balance.add(Helpers.parseBalance(matcher.group(1)));
           accounts.add(account);
		}
        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
        super.updateComplete();
	}
}