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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.text.Html;

import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.Transaction;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;

import eu.nullbyte.android.urllib.Urllib;

public class Avanza extends Bank {
	private static final String TAG = "Avanza";
	private static final String NAME = "Avanza";
	private static final String NAME_SHORT = "avanza";
	private static final String URL = "https://www.avanza.se/";
    private static final int BANKTYPE_ID = Bank.AVANZA;
	
	private Pattern reAccounts = Pattern.compile("depa\\.jsp\\?depotnr=([^\"]+)[^>]+>[^<]+</a>\\s*</td>\\s*<td[^>]+>([^<]+)<.*?<td[^>]+>([^<]+)</td>\\s*<td[^>]+>([^<]+)<", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private Pattern reTransactions = Pattern.compile("(?:warrantguide\\.jsp|aktie\\.jsp)(?:.*?)orderbookId=(?:.*?)>(.*?)<(?:.*?)<nobr>(?:.*?)<nobr>(?:.*?)<nobr>(.*?)<", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	public Avanza(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = URL;
	}

	public Avanza(String username, String password, Context context) throws BankException, LoginException {
		this(context);
		this.update(username, password);
	}

	
    @Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
        urlopen = new Urllib(true, true);
        List <NameValuePair> postData = new ArrayList <NameValuePair>();
        postData.add(new BasicNameValuePair("username", username));
        postData.add(new BasicNameValuePair("password", password));
        return new LoginPackage(urlopen, postData, null, "https://www.avanza.se/aza/login/login.jsp");
    }

	@Override
	public Urllib login() throws LoginException, BankException {
		String response = null;
		try {
		    LoginPackage lp = preLogin();
			response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
			if (response.contains("Felaktigt") && !response.contains("Logga ut")) {
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
		Urllib urlopen = login();
		String response = null;
		Matcher matcher;
		try {
			response = urlopen.open("https://www.avanza.se/aza/depa/sammanfattning/sammanfattning.jsp");
						
			matcher = reAccounts.matcher(response);
			while (matcher.find()) {
                /*
                 * Capture groups:
                 * GROUP                EXAMPLE DATA
                 * 1: ID                3505060
                 * 2: Type              Aktie- och fondkonto Premium Silver
                 * 3: % since purchase  1,90
                 * 4: Amount in SEK     820
                 *    
                 */    
			    accounts.add(new Account(Html.fromHtml(matcher.group(1)).toString().trim(), Helpers.parseBalance(matcher.group(4)), matcher.group(1).trim()));
				balance = balance.add(Helpers.parseBalance(matcher.group(4)));
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

	
	@Override
	public void updateTransactions(Account account, Urllib urlopen) throws LoginException, BankException {
		super.updateTransactions(account, urlopen);
		
		String response = null;
		Matcher matcher;
		try {						
			response = urlopen.open("https://www.avanza.se/aza/depa/depa.jsp?depotnr="+account.getId());
			matcher = reTransactions.matcher(response);
			ArrayList<Transaction> transactions = new ArrayList<Transaction>();
			String strDate = null;
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			strDate = sdf.format(cal.getTime());

			while (matcher.find()) {
				transactions.add(new Transaction(strDate, Html.fromHtml(matcher.group(1)).toString().trim(), Helpers.parseBalance(matcher.group(2))));
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