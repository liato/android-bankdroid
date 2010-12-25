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

public class Swedbank extends Bank {
	private static final String TAG = "Swedbank";
	private static final String NAME = "Swedbank";
	private static final String NAME_SHORT = "swedbank";
	private static final String URL = "https://mobilbank.swedbank.se/";
	private static final int BANKTYPE_ID = Bank.SWEDBANK;
    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;
    private static final String INPUT_HINT_USERNAME = "ÅÅMMDD-XXXX";
    
	private Pattern reCSRF = Pattern.compile("csrf_token\".*?value=\"([^\"]+)\"");
	private Pattern reAccounts = Pattern.compile("(account|loan)\\.html\\?id=([^\"]+)\">\\s*(?:<span.*?/span>)?([^<]+)<.*?secondary\">([^<]+)</span");
	private Pattern reLinklessAccounts = Pattern.compile("<li>\\s*([^<]+)<br/?><span\\sclass=\"secondary\">([^<]+)</span>\\s*</li>", Pattern.CASE_INSENSITIVE);
	private Pattern reTransactions = Pattern.compile("trans-date\">([^<]+)</div>.*?trans-subject\">([^<]+)</div>.*?trans-amount\">([^<]+)</div>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	public Swedbank(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = URL;
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
        super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
	}

	public Swedbank(String username, String password, Context context) throws BankException, LoginException {
		this(context);
		this.update(username, password);
	}

	@Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
        urlopen = new Urllib();
        Matcher matcher;
        String response = urlopen.open("https://mobilbank.swedbank.se/banking/swedbank/login.html");
        matcher = reCSRF.matcher(response);
        if (!matcher.find()) {
            throw new BankException(res.getText(R.string.unable_to_find).toString()+" CSRF token.");
        }
        String csrftoken = matcher.group(1);
        List <NameValuePair> postData = new ArrayList <NameValuePair>();
        postData.add(new BasicNameValuePair("xyz", username));
        postData.add(new BasicNameValuePair("zyx", password));
        postData.add(new BasicNameValuePair("_csrf_token", csrftoken));
        return new LoginPackage(urlopen, postData, response, "https://mobilbank.swedbank.se/banking/swedbank/login.html");
    }

    @Override
	public Urllib login() throws LoginException, BankException {
	    
	    try {
	        LoginPackage lp = preLogin();
	        String response = urlopen.open(lp.getLoginTarget(), lp.getPostData());

			if (response.contains("misslyckats")) {
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
		String response = null;
		Matcher matcher;
		try {
			response = urlopen.open("https://mobilbank.swedbank.se/banking/swedbank/accounts.html");
			matcher = reAccounts.matcher(response);
			while (matcher.find()) {
				Account account = new Account(Html.fromHtml(matcher.group(3)).toString(), Helpers.parseBalance(matcher.group(4)), matcher.group(1).trim() == "loan" ? "l:" + matcher.group(2).trim() : matcher.group(2).trim());
				if (matcher.group(1).trim() == "loan") {
				    account.setType(Account.LOANS);
				}
				else {
				    balance = balance.add(Helpers.parseBalance(matcher.group(4)));
				}
				accounts.add(account);
			}
			matcher = reLinklessAccounts.matcher(response);
			int accid = 0;
			while (matcher.find()) {
				Account account = new Account(Html.fromHtml(matcher.group(1)).toString(), Helpers.parseBalance(matcher.group(2)), "ll:"+accid);
				account.setType(Account.OTHER);
                accounts.add(account);
                accid++;
			}
			if (accounts.isEmpty()) {
				throw new BankException(res.getText(R.string.no_accounts_found).toString());
			}
			// Konungens konto
			//accounts.add(new Account("Personkonto", Helpers.parseBalance("85351"), "0"));
			//accounts.add(new Account("Sparkonto", Helpers.parseBalance("8590700"), "1"));
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
		if (account.getType() == Account.LOANS || account.getType() == Account.OTHER) return; //No transaction history for loans

		String response = null;
		Matcher matcher;
		try {
			Log.d(TAG, "Opening: https://mobilbank.swedbank.se/banking/swedbank/account.html?id="+account.getId());
			response = urlopen.open("https://mobilbank.swedbank.se/banking/swedbank/account.html?id="+account.getId());
			matcher = reTransactions.matcher(response);
			ArrayList<Transaction> transactions = new ArrayList<Transaction>();
			while (matcher.find()) {
				transactions.add(new Transaction("20"+matcher.group(1).trim(), Html.fromHtml(matcher.group(2)).toString().trim(), Helpers.parseBalance(matcher.group(3))));
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
