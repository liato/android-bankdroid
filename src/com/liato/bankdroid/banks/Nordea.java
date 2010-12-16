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

package com.liato.bankdroid.banks;

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
import android.util.Log;

import com.liato.bankdroid.Account;
import com.liato.bankdroid.Bank;
import com.liato.bankdroid.BankException;
import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.LoginException;
import com.liato.bankdroid.R;
import com.liato.bankdroid.Transaction;
import com.liato.urllib.Urllib;

public class Nordea extends Bank {
	private static final String TAG = "Nordea";
	private static final String NAME = "Nordea";
	private static final String NAME_SHORT = "nordea";
	private static final String URL = "https://mobil.nordea.se/";
	private static final int BANKTYPE_ID = Bank.NORDEA;
    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;
    private static final int INPUT_TYPE_PASSWORD = InputType.TYPE_CLASS_PHONE;
    private static final String INPUT_HINT_USERNAME = "ÅÅMMDD-XXXX";
	
	private Pattern reAccounts = Pattern.compile("account\\.html\\?id=konton:([^\"]+)\"[^>]+>\\s*<div[^>]+>([^<]+)<span[^>]+>([^<]+)</span", Pattern.CASE_INSENSITIVE);
	private Pattern reFundsLoans = Pattern.compile("(?:fund|loan)\\.html\\?id=(?:fonder|lan):([^\"]+)\".*?>.*?>([^<]+).*?>([^<]+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private Pattern reCards = Pattern.compile("/card/details\\.html\\?id=(\\d{1,})[^\"]*\".*?>\\s*<span[^>]*>\\s*<span>([^<]+)</span>\\s*<span[^>]+>([^<]+)<", Pattern.CASE_INSENSITIVE);
	private Pattern reTransactions = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})\\s</dt>[^>]+>([^<]+)[^>]+>.*?(?:Positive|Negative)\">([^<]+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private Pattern reCSRF = Pattern.compile("csrf_token\".*?value=\"([^\"]+)\"");

	public Nordea(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = URL;
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
        super.INPUT_TYPE_PASSWORD = INPUT_TYPE_PASSWORD;
        super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
	}

	public Nordea(String username, String password, Context context) throws BankException, LoginException {
		this(context);
		this.update(username, password);
	}

	@Override
	public Urllib login() throws LoginException, BankException {
		urlopen = new Urllib();
		String response = null;
		Matcher matcher;
		try {
			response = urlopen.open("https://mobil.nordea.se/banking-nordea/nordea-c3/login.html");
			matcher = reCSRF.matcher(response);
			if (!matcher.find()) {
				throw new BankException(res.getText(R.string.unable_to_find).toString()+" CSRF token.");
			}
			String csrftoken = matcher.group(1);
			List <NameValuePair> postData = new ArrayList <NameValuePair>();
			postData.add(new BasicNameValuePair("xyz", username));
			postData.add(new BasicNameValuePair("zyx", password));
			postData.add(new BasicNameValuePair("_csrf_token", csrftoken));
			Log.d("BankNordea", "Posting to https://mobil.nordea.se/banking-nordea/nordea-c3/login.html");
			response = urlopen.open("https://mobil.nordea.se/banking-nordea/nordea-c3/login.html", postData);
			Log.d("BankNordea", "Url after post: "+urlopen.getCurrentURI());
			
			if (response.contains("felaktiga uppgifter")) {
				throw new LoginException(res.getText(R.string.invalid_username_password).toString());
			}
			
		} catch (ClientProtocolException e) {
			throw new BankException(e.getMessage());
		} catch (IOException e) {
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
		String response = null;
		Matcher matcher;
		try {
		    
		    Log.d("BankNordea", "Opening: https://mobil.nordea.se/banking-nordea/nordea-c3/accounts.html");
			response = urlopen.open("https://mobil.nordea.se/banking-nordea/nordea-c3/accounts.html");
			
			matcher = reAccounts.matcher(response);
			while (matcher.find()) {
				accounts.add(new Account(Html.fromHtml(matcher.group(2)).toString().trim(), Helpers.parseBalance(matcher.group(3)), matcher.group(1).trim()));
				balance = balance.add(Helpers.parseBalance(matcher.group(3)));
			}
			
			Log.d("BankNordea", "Opening: https://mobil.nordea.se/banking-nordea/nordea-c3/funds/portfolio/funds.html");
			response = urlopen.open("https://mobil.nordea.se/banking-nordea/nordea-c3/funds/portfolio/funds.html");

			matcher = reFundsLoans.matcher(response);
			while (matcher.find()) {
				accounts.add(new Account(Html.fromHtml(matcher.group(2)).toString().trim(), Helpers.parseBalance(matcher.group(3)), "f:"+matcher.group(1).trim(), -1L, Account.FUNDS));
			}

			Log.d("BankNordea", "Opening: https://mobil.nordea.se/banking-nordea/nordea-c3/accounts.html?type=lan");
			response = urlopen.open("https://mobil.nordea.se/banking-nordea/nordea-c3/accounts.html?type=lan");
			
			matcher = reFundsLoans.matcher(response);
			while (matcher.find()) {
				accounts.add(new Account(Html.fromHtml(matcher.group(2)).toString().trim(), Helpers.parseBalance(matcher.group(3)), "l:"+matcher.group(1).trim(), -1L, Account.LOANS));
			}
			matcher = reCards.matcher(response);
			Log.d(TAG, "Opening: https://mobil.nordea.se/banking-nordea/nordea-c3/card/list.html");
			response = urlopen.open("https://mobil.nordea.se/banking-nordea/nordea-c3/card/list.html");
			while (matcher.find()) {
				accounts.add(new Account(Html.fromHtml(matcher.group(2)).toString().trim(), Helpers.parseBalance(matcher.group(3)), "c:"+matcher.group(1).trim(), -1L, Account.CCARD));
				balance = balance.add(Helpers.parseBalance(matcher.group(3)));
			}

			if (accounts.isEmpty()) {
				throw new BankException(res.getText(R.string.no_accounts_found).toString());
			}
			
			// Konungens konto
			//accounts.add(new Account("Personkonto", Helpers.parseBalance("568268.37"), "1"));
			//accounts.add(new Account("Kapitalkonto", Helpers.parseBalance("5789002.00"), "0"));
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

	@Override
	public void updateTransactions(Account account, Urllib urlopen) throws LoginException, BankException {
		super.updateTransactions(account, urlopen);

		//No transaction history for loans, funds and credit cards.
		int accType = account.getType();
		if (accType == Account.LOANS || accType == Account.FUNDS || accType == Account.CCARD) return;

		String response = null;
		Matcher matcher;
		try {
			response = urlopen.open("https://mobil.nordea.se/banking-nordea/nordea-c3/accounts.html");
			Log.d(TAG, "Opening: https://mobil.nordea.se/banking-nordea/nordea-c3/account.html?id=konton:"+account.getId());
			response = urlopen.open("https://mobil.nordea.se/banking-nordea/nordea-c3/account.html?id=konton:"+account.getId());
			matcher = reTransactions.matcher(response);
			ArrayList<Transaction> transactions = new ArrayList<Transaction>();
			while (matcher.find()) {
				transactions.add(new Transaction(Html.fromHtml(matcher.group(1)).toString().trim(), Html.fromHtml(matcher.group(2)).toString().trim(), Helpers.parseBalance(matcher.group(3))));
			}
			account.setTransactions(transactions);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}