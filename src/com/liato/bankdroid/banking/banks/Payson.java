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
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;

import eu.nullbyte.android.urllib.Urllib;

public class Payson extends Bank {
	private static final String TAG = "Payson";
	private static final String NAME = "Payson";
	private static final String NAME_SHORT = "payson";
	private static final String URL = "https://www.payson.se/signin/";
	private static final int BANKTYPE_ID = IBankTypes.PAYSON;
    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_TEXT | + InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
	
    private Pattern reEventValidation = Pattern.compile("__EVENTVALIDATION\"\\s+value=\"([^\"]+)\"");
    private Pattern reViewState = Pattern.compile("__VIEWSTATE\"\\s+value=\"([^\"]+)\"");
    private Pattern reBalance = Pattern.compile("Saldo:\\s*<strong>([^<+]+)[<+]", Pattern.CASE_INSENSITIVE);
	private Pattern reTransactions = Pattern.compile("href=\"details/Default\\.aspx\\?\\d{1,}\">\\s*<span\\s*title=\"(\\d{4}-\\d{2}-\\d{2})[^\"]+\">.*?Grid1_0_3_\\d{1,}_Hy[^>]+>([^<]+)<.*?Grid1_0_5_\\d{1,}_Hy[^>]+>([^<]+)<", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private String response = null;
	
	public Payson(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = URL;
		super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
	}

	public Payson(String username, String password, Context context) throws BankException, LoginException {
		this(context);
		this.update(username, password);
	}

    
    @Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
        urlopen = new Urllib(true);
        response = urlopen.open("https://www.payson.se/signin/");
        Matcher matcher = reViewState.matcher(response);
        if (!matcher.find()) {
            throw new BankException(res.getText(R.string.unable_to_find).toString()+" ViewState.");
        }
        String strViewState = matcher.group(1);
        matcher = reEventValidation.matcher(response);
        if (!matcher.find()) {
            throw new BankException(res.getText(R.string.unable_to_find).toString()+" EventValidation.");
        }
        String strEventValidation = matcher.group(1);

        List <NameValuePair> postData = new ArrayList <NameValuePair>();
        postData.add(new BasicNameValuePair("__LASTFOCUS", ""));
        postData.add(new BasicNameValuePair("__EVENTTARGET", ""));
        postData.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
        postData.add(new BasicNameValuePair("__VIEWSTATE", strViewState));
        postData.add(new BasicNameValuePair("__EVENTVALIDATION", strEventValidation));
        postData.add(new BasicNameValuePair("ctl00$MainContent$SignIn1$txtEmail", username));
        postData.add(new BasicNameValuePair("ctl00$MainContent$SignIn1$txtPassword", password));
        postData.add(new BasicNameValuePair("ctl00$MainContent$SignIn1$btnLogin", "Logga in"));
        return new LoginPackage(urlopen, postData, response, "https://www.payson.se/signin/");
    }

    @Override
	public Urllib login() throws LoginException, BankException {
		try {
            LoginPackage lp = preLogin();
			response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
			if (response.contains("Felaktig E-postadress") || response.contains("Lösenord saknas") ||
			        response.contains("E-postadress saknas"))  {
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
	public void update() throws BankException, LoginException {
		super.update();
		if (username == null || password == null || username.length() == 0 || password.length() == 0) {
			throw new LoginException(res.getText(R.string.invalid_username_password).toString());
		}
		urlopen = login();
		try {
			Matcher matcher;
			matcher = reBalance.matcher(response);
			if (matcher.find()) {
                /*
                 * Capture groups:
                 * GROUP                EXAMPLE DATA
                 * 1: Balance           0,00 kr
                 *  
                 */
			    Account account = new Account("Konto" , Helpers.parseBalance(matcher.group(1)), "1");
			    String currency = Helpers.parseCurrency(matcher.group(1).trim(), "SEK");
			    account.setCurrency(currency);
			    this.setCurrency(currency);
				accounts.add(account);
				balance = balance.add(Helpers.parseBalance(matcher.group(1)));
			}
			
			if (accounts.isEmpty()) {
				throw new BankException(res.getText(R.string.no_accounts_found).toString());
			}
	
	
			matcher = reTransactions.matcher(response);
			ArrayList<Transaction> transactions = new ArrayList<Transaction>();
			while (matcher.find()) {
                /*
                 * Capture groups:
                 * GROUP                EXAMPLE DATA
                 * 1: Date              2010-06-03
                 * 2: Specification     Best&#228;llning fr&#229;n SPELKONTROLL.SE
                 * 3: Amount            -228,00 kr
                 *   
                 */     
				transactions.add(new Transaction(matcher.group(1).trim(), Html.fromHtml(matcher.group(2)).toString().trim(), Helpers.parseBalance(matcher.group(3))));
			}
			accounts.get(0).setTransactions(transactions);
		}		
        finally {
            super.updateComplete();
        }
	}
}
