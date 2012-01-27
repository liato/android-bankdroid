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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.text.Html;
import android.text.InputType;

import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.Transaction;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;

import eu.nullbyte.android.urllib.Urllib;

public class ICA extends Bank {
	private static final String TAG = "ICA";
	private static final String NAME = "ICA";
	private static final String NAME_SHORT = "ica";
	private static final String URL = "http://mobil.ica.se/";
	private static final int BANKTYPE_ID = IBankTypes.ICA;
    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;
    private static final String INPUT_HINT_USERNAME = "ÅÅMMDDXXXX";

	private Pattern reAccounts = Pattern.compile("lblAvaibleAmount\">([^<]+)<", Pattern.CASE_INSENSITIVE);
	private Pattern reTransactions = Pattern.compile("<td>\\s*(\\d{4}-\\d{2}-\\d{2})\\s*</td>\\s*<td>\\s*([^<]+).*?amount\">([^<]+)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	private Pattern reViewState = Pattern.compile("__VIEWSTATE\"\\s+value=\"([^\"]+)\"");
	private Pattern reLoginError = Pattern.compile("login-error[^>]+>(.+?)<");
	public ICA(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = URL;
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
        super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
	}

	public ICA(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
		this(context);
		this.update(username, password);
	}

    
    @Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
        urlopen = new Urllib(true);
        String response = urlopen.open("https://www.ica.se/Logga-in/");
        Matcher matcher = reViewState.matcher(response);
        if (!matcher.find()) {
            throw new BankException(res.getText(R.string.unable_to_find).toString()+" viewstate.");
        }
        String strViewState = matcher.group(1);
        List <NameValuePair> postData = new ArrayList <NameValuePair>();
        postData.add(new BasicNameValuePair("__EVENTTARGET", ""));
        postData.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
        postData.add(new BasicNameValuePair("__LASTFOCUS", ""));
        postData.add(new BasicNameValuePair("__VIEWSTATE", strViewState));
        postData.add(new BasicNameValuePair("ctl00$fakie", "0"));
        postData.add(new BasicNameValuePair("q", "Sök"));
        postData.add(new BasicNameValuePair("appendUrlString", ""));
        postData.add(new BasicNameValuePair("ctl00$cphFullWidthContainer$ctl02$btnLogin", "Logga in"));
        postData.add(new BasicNameValuePair("ctl00$cphFullWidthContainer$ctl02$txtCivicRegistrationNumber", ""));
        postData.add(new BasicNameValuePair("ctl00$cphFullWidthContainer$ctl02$txtEmail", ""));
        postData.add(new BasicNameValuePair("footer-q", "Sök"));
        postData.add(new BasicNameValuePair("ctl00$cphFullWidthContainer$ctl02$tbPersno", username));
        postData.add(new BasicNameValuePair("ctl00$cphFullWidthContainer$ctl02$tbPasswd", password));

        return new LoginPackage(urlopen, postData, response, "https://www.ica.se/Logga-in/");
    }

	@Override
	public Urllib login() throws LoginException, BankException {
		try {
			LoginPackage lp = preLogin();
			String response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
			Matcher matcher = reLoginError.matcher(response);
			if (matcher.find()) {
				throw new LoginException(Html.fromHtml(matcher.group(1)).toString().trim());
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
		String response = null;
		Matcher matcher;
		try {
			response = urlopen.open("https://www.ica.se/Mina-sidor/Konto--Saldo/");
			matcher = reAccounts.matcher(response);
			if (matcher.find()) {
				Account account = new Account("ICA Kort", Helpers.parseBalance(matcher.group(1)), "1");
				balance = balance.add(Helpers.parseBalance(matcher.group(1)));
				matcher = reTransactions.matcher(response);
				ArrayList<Transaction> transactions = new ArrayList<Transaction>();
				while (matcher.find()) {
					transactions.add(new Transaction(matcher.group(1).trim(), Html.fromHtml(matcher.group(2)).toString().trim(), Helpers.parseBalance(matcher.group(3))));
				}
				account.setTransactions(transactions);
				accounts.add(account);
			}
			if (accounts.isEmpty()) {
				throw new BankException(res.getText(R.string.no_accounts_found).toString());
			}
		}
		catch (ClientProtocolException e) {
			throw new BankException(e.getMessage());
		}
		catch (IOException e) {
			throw new BankException(e.getMessage());
		}
        finally {
            super.updateComplete();
        }
	}
}
