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

import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.legacy.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.Transaction;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class PlusGirot extends Bank {
	private static final String TAG = "PlusGirot";
	private static final String NAME = "PlusGirot";
	private static final String NAME_SHORT = "plusgirot";
	private static final String URL = "https://kontoutdrag.plusgirot.se/";
	private static final int BANKTYPE_ID = IBankTypes.PLUSGIROT;

    private Pattern reAccounts = Pattern.compile("<tr>\\s*<td[^>]+>\\s*<font[^>]+>([^<]+)</font>\\s*</td>\\s*<td[^>]+><font[^>]+>\\s*<font[^>]+>([^<]+)</font>\\s*</td>\\s*<td[^>]+>\\s*<font[^>]+>([^<]+)</font>\\s*</td>\\s*<td[^>]+>\\s*<font[^>]+>([^<]+)</font>", Pattern.CASE_INSENSITIVE);
	private Pattern reTransactions = Pattern.compile("<a[^>]+>([^<]+)</a>\\s*</font>\\s*</td>\\s*<td[^>]+>\\s*<font[^>]+>([^<]+)</font>\\s*</td>\\s*<td[^>]+>\\s*<font[^>]+>([^<]+)</font>\\s*</td>\\s*<td[^>]+>\\s*<font[^>]+>([^<]+)</f", Pattern.CASE_INSENSITIVE);
	private String response = null;
	
	public PlusGirot(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = URL;
	}

	public PlusGirot(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
		this(context);
		this.update(username, password);
	}

    
    @Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
        urlopen = new Urllib(context, CertificateReader.getCertificates(context, R.raw.cert_plusgirot));
        // Request first page to get cookies
        response = urlopen.open("https://kontoutdrag.plusgirot.se/ku/html/epostllg.htm");

        List <NameValuePair> postData = new ArrayList <NameValuePair>();
        postData.add(new BasicNameValuePair("KONTO", username));
        postData.add(new BasicNameValuePair("PIN_KOD", password));
        return new LoginPackage(urlopen, postData, response, "https://kontoutdrag.plusgirot.se/ku/bgya006/init");
    }

    @Override
	public Urllib login() throws LoginException, BankException {
		try {
            LoginPackage lp = preLogin();
			response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
			if (response.contains("elaktigt kontonummer"))  {
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
		try {
			Matcher matcher;
			matcher = reAccounts.matcher(response);
			if (matcher.find()) {
                /*
                 * Capture groups:
                 * GROUP                EXAMPLE DATA
                 * 1: Account holder    Efternamn,Fornamn
                 * 2: PG account        456 12 34-5
                 * 3: Amount            123,45
                 * 4: Credit            24,68
                 *  
                 */
			    Account account = new Account(matcher.group(2).trim() + " (" + Html.fromHtml(matcher.group(1)).toString().trim() + ")",
			                                    Helpers.parseBalance(matcher.group(3)),
			                                    matcher.group(2).trim().replaceAll("[^0-9]*", ""));
				accounts.add(account);
				balance = balance.add(Helpers.parseBalance(matcher.group(3)));
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
                 * 1: Date              2011-04-04
                 * 2: Specification     UTTAG
                 * 3: Payment code      Inr.
                 * 4: Amount            -100,00
                 *   
                 */     
				transactions.add(
				        new Transaction(matcher.group(1).trim(),
				        Html.fromHtml(matcher.group(2)).toString().trim() + " " + Html.fromHtml(matcher.group(3)).toString().trim(),
				        Helpers.parseBalance(matcher.group(4))));
			}
			accounts.get(0).setTransactions(transactions);
		}		
        finally {
            super.updateComplete();
        }
	}
}
