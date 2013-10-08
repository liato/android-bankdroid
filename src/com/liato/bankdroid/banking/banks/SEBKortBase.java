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
import java.util.Collections;
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

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public abstract class SEBKortBase extends Bank {
    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;
    private static final String INPUT_HINT_USERNAME = "ÅÅMMDDXXXX";
    private static final boolean STATIC_BALANCE = true;

	private Pattern reAccounts = Pattern.compile("Welcomepagebillingunit(?:last(?:disposable|credit)amount|2rowcol2)\">([^<]+)</(?:div|td)>", Pattern.CASE_INSENSITIVE);
	private Pattern reTransactions = Pattern.compile("transcol1\">\\s*<span>([^<]+)</span>\\s*</td>\\s*<td[^>]+>\\s*<span>([^<]+)</span>\\s*</td>\\s*<td[^>]+>\\s*(?:<div[^>]+>\\s*)?<span>([^<]*)</span>\\s*(?:</div>\\s*)?</td>\\s*<td[^>]+>\\s*<span>([^<]*)</span>\\s*</td>\\s*<td[^>]+>\\s*<span>([^>]*)</span>\\s*</td>\\s*<td[^>]+>\\s*<span>([^<]*)</span>\\s*</td>\\s*<td[^>]+>\\s*<span>([^<]+)</span>", Pattern.CASE_INSENSITIVE);
	private String response = null;
	private String provider_part;
	private String prodgroup;
	public SEBKortBase(Context context, String provider_part, String prodgroup) {
		super(context);
		super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
		super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
		super.STATIC_BALANCE = STATIC_BALANCE;
		this.provider_part = provider_part;
		this.prodgroup = prodgroup;
		super.URL = String.format("https://application.sebkort.com/nis/external/%s/login.do", provider_part);
	}

	public SEBKortBase(String username, String password, Context context, String url, String prodgroup) throws BankException, LoginException, BankChoiceException {
		this(context, url, prodgroup);
		this.update(username, password);
	}
    
    @Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
        urlopen = new Urllib(CertificateReader.getCertificates(context, R.raw.cert_sebkort));
        List <NameValuePair> postData = new ArrayList <NameValuePair>();
        response = urlopen.open(String.format("https://application.sebkort.com/nis/external/%s/login.do", provider_part));
        urlopen.addHeader("Referer", String.format("https://application.sebkort.com/nis/external/%s/login.do", provider_part));
        response = urlopen.open(String.format("https://application.sebkort.com/nis/external/hidden.jsp?USERNAME=%s&CURRENT_METHOD=&referer=login.jsp", prodgroup+username.toUpperCase()));
        urlopen.removeHeader("Referer");
        
        postData.clear();
        postData.add(new BasicNameValuePair("SEB_Referer", "/nis"));
        postData.add(new BasicNameValuePair("SEB_Auth_Mechanism", "5"));
        postData.add(new BasicNameValuePair("target", String.format("/nis/%s/main.do", provider_part)));
        postData.add(new BasicNameValuePair("prodgroup", prodgroup));
        postData.add(new BasicNameValuePair("UID", prodgroup+username.toUpperCase()));
        postData.add(new BasicNameValuePair("TYPE", "LOGIN"));
        postData.add(new BasicNameValuePair("CURRENT_METHOD", "PWD"));
        postData.add(new BasicNameValuePair("uname", username.toUpperCase()));
        postData.add(new BasicNameValuePair("PASSWORD", password));
        
        return new LoginPackage(urlopen, postData, response, "https://application.sebkort.com/auth4/Authentication/select.jsp");
    }

	@Override
	public Urllib login() throws LoginException, BankException {
		try {
			LoginPackage lp = preLogin();
			response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
			if (response.contains("elaktig kombination") || response.contains("ett felaktigt") || response.contains("invalid login")) {
				throw new LoginException(res.getText(R.string.invalid_username_password).toString());
			}
		}
		catch (ClientProtocolException e) {
			throw new BankException(e.getMessage());
		}
		catch (IOException e) {
            e.printStackTrace();
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
		Matcher matcher;
		try {
			if (!String.format("https://application.sebkort.com/nis/%s/main.do", provider_part).equals(urlopen.getCurrentURI())) {
				response = urlopen.open(String.format("https://application.sebkort.com/nis/%s/main.do", provider_part));
			}
			matcher = reAccounts.matcher(response);
            /*
             * Capture groups:
             * GROUP                EXAMPLE DATA
             * 1: amount            10 579,43
             * 
             */
			if (matcher.find()) {
			    Account account = new Account("Köpgräns" , Helpers.parseBalance(matcher.group(1)), "3");
			    account.setType(Account.OTHER);
                account.setAliasfor("1");
			    accounts.add(account);
			}
            if (matcher.find()) {
                Account account = new Account("Saldo" , Helpers.parseBalance(matcher.group(1)), "2");
                account.setType(Account.OTHER);
                account.setAliasfor("1");
                accounts.add(account);
            }
            if (matcher.find()) {
                Account account = new Account("Disponibelt belopp" , Helpers.parseBalance(matcher.group(1)), "1");
                account.setType(Account.CCARD);
                accounts.add(account);
                balance = balance.add(Helpers.parseBalance(matcher.group(1)));
            }
            Collections.reverse(accounts);
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
	
	@Override
	public void updateTransactions(Account account, Urllib urlopen) throws LoginException, BankException {
		super.updateTransactions(account, urlopen);
		if (account.getType() != Account.CCARD) return;
		String response = null;
		Matcher matcher;
		try {
			response = urlopen.open(String.format("https://application.sebkort.com/nis/%s/getPendingTransactions.do", provider_part));
			matcher = reTransactions.matcher(response);
			ArrayList<Transaction> transactions = new ArrayList<Transaction>();
			while (matcher.find()) {
				/*
				 * Capture groups:
				 * GROUP				EXAMPLE DATA
				 * 1: Trans. date		10-18
				 * 2: Book. date		10-19
				 * 3: Specification		ICA Kvantum
				 * 4: Location			Stockholm
				 * 5: Currency			currency code (e.g. EUR) for transactions in non-SEK
				 * 6: Amount			local currency amount (in $currency) for transactions in non-SEK
				 * 7: Amount in sek		5791,18
				 * 
				 */
			    String[] monthday = matcher.group(1).trim().split("-");
				transactions.add(new Transaction(Helpers.getTransactionDate(monthday[0], monthday[1]),
				        Html.fromHtml(matcher.group(3)).toString().trim()+(matcher.group(4).trim().length() > 0 ? " ("+Html.fromHtml(matcher.group(4)).toString().trim()+")" : ""),
				        Helpers.parseBalance(matcher.group(7)).negate()));
	            Collections.sort(transactions, Collections.reverseOrder());
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
