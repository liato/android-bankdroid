/*
 * Copyright (C) 2010 Nullbyte <http://nullbyte.eu>
 * Contributors: DEGE
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

public class Handelsbanken extends Bank {
	private static final String TAG = "Handelsbanken";
	private static final String NAME = "Handelsbanken";
	private static final String NAME_SHORT = "handelsbanken";
	private static final String URL = "https://m.handelsbanken.se/";
	private static final int BANKTYPE_ID = Bank.HANDELSBANKEN;
    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;
    private static final String INPUT_HINT_USERNAME = "ÅÅMMDDXXXX";

	private Pattern reBalance = Pattern.compile("block-link\\s*\"\\s*href=\"[^\"]*?/primary/_-([^\"]+)\"><span>([^<]+)</span>.*?SEK(?:&nbsp;|\\s*)?([0-9\\s.,-ÃÂ]+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private Pattern reAccountsUrl = Pattern.compile("_-([^\"]+)\">(?:<img[^>]+>)?<img[^>]+><span[^>]+>Konton<",Pattern.CASE_INSENSITIVE);
	private Pattern reLoginUrl = Pattern.compile("_-([^\"]+)\">(?:<img[^>]+>)?<img[^>]+><span[^>]+>Logga",Pattern.CASE_INSENSITIVE);
	private Pattern reTransactions = Pattern.compile("padding-left\">([^<]+)</span><span[^>]*><span[^>]*>([^<]+)</span><span[^>]*>([^<]+)<", Pattern.CASE_INSENSITIVE);

	private ArrayList<String> accountIds = new ArrayList<String>();
	private String response = null;
	
	public Handelsbanken(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = URL;
		super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
		super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
	}

	public Handelsbanken(String username, String password, Context context) throws BankException, LoginException {
		this(context);
		this.update(username, password);
	}

	
    
    @Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
        urlopen = new Urllib();
        response = urlopen.open("https://m.handelsbanken.se/primary/");
        Matcher matcher = reLoginUrl.matcher(response);
        if (!matcher.find()) {
            throw new BankException(res.getText(R.string.unable_to_find).toString()+" login url.");
        }
        String strLoginUrl = "https://m.handelsbanken.se/primary/_-"+matcher.group(1);
        List <NameValuePair> postData = new ArrayList <NameValuePair>();
        postData.add(new BasicNameValuePair("username", username));
        postData.add(new BasicNameValuePair("pin", password));
        postData.add(new BasicNameValuePair("execute", "true"));
        return new LoginPackage(urlopen, postData, response, strLoginUrl);
    }

	@Override
	public Urllib login() throws LoginException, BankException {
		try {
			LoginPackage lp = preLogin();
			response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
			if (response.contains("ontrollera dina uppgifter")) {
				throw new LoginException(res.getText(R.string.invalid_username_password).toString());
			}
		}
		catch (ClientProtocolException e) {
			Log.d(TAG, "ClientProtocolException: "+e.getMessage());
			throw new BankException(e.getMessage());
		}
		catch (IOException e) {
			Log.d(TAG, "IOException: "+e.getMessage());
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
		Matcher matcher;
		try {
			matcher = reAccountsUrl.matcher(response);
			if (!matcher.find()) {
				throw new BankException(res.getText(R.string.unable_to_find).toString()+" accounts url.");
			}
			String strAccountsUrl = "https://m.handelsbanken.se/primary/_-"+matcher.group(1);			
			response = urlopen.open(strAccountsUrl);
			matcher = reBalance.matcher(response);
			Integer accountId = 0;
			while (matcher.find()) {
				accounts.add(new Account(Html.fromHtml(matcher.group(2)).toString().trim(), Helpers.parseBalance(matcher.group(3).trim()), accountId.toString()));
				balance = balance.add(Helpers.parseBalance(matcher.group(3)));
				accountIds.add(matcher.group(1));
				accountId += 1;
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
	

	public void updateTransactions(Account account, Urllib urlopen) throws LoginException, BankException {
		super.updateTransactions(account, urlopen);
		Matcher matcher;
		try {
			String accountWebId = accountIds.get(Integer.parseInt(account.getId()));
			Log.d(TAG, "Opening: https://m.handelsbanken.se/primary/_-"+accountWebId);
			response = urlopen.open("https://m.handelsbanken.se/primary/_-"+accountWebId);
			matcher = reTransactions.matcher(response);
			ArrayList<Transaction> transactions = new ArrayList<Transaction>();
			while (matcher.find()) {
				transactions.add(new Transaction(matcher.group(1).trim(), Html.fromHtml(matcher.group(2)).toString().trim(), Helpers.parseBalance(matcher.group(3))));
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