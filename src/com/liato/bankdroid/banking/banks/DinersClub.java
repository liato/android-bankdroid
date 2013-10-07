/*
 * Copyright (C) 2010 Nullbyte <http://nullbyte.eu>
 * Contributors: mhagander
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
import com.liato.bankdroid.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.Transaction;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class DinersClub extends Bank {
	private static final String TAG = "DinersClub";
	private static final String NAME = "Diners Club";
	private static final String NAME_SHORT = "dinersclub";
	private static final String URL = "https://secure.dinersclub.se/dcs/login.aspx";
	private static final int BANKTYPE_ID = IBankTypes.DINERSCLUB;
	
    private Pattern reViewState = Pattern.compile("__VIEWSTATE\"\\s+value=\"([^\"]+)\"");
    private Pattern reEventValidation = Pattern.compile("__EVENTVALIDATION\"\\s+value=\"([^\"]+)\"");
	private Pattern reBalance = Pattern.compile("class=\"card\"[^>]+>\\s*<div[^>]+>\\s*<b>([^<]+)</b>\\s*<br ?/>\\s*<span[^>]+>([^<]+)</span>\\s*</div>\\s*<div[^>]+>\\s*<strong[^>]+>[^<]+</strong>([^<]+)</div>", Pattern.CASE_INSENSITIVE);
	private Pattern reInvoices = Pattern.compile("<tr[^>]+>\\s*<td class=\"right\">\\s*<a href='((Invoice|Nonbilled).aspx\\?card=\\d+&bdate=[\\d-]+)'>", Pattern.CASE_INSENSITIVE);
	private Pattern reTransactions = Pattern.compile("<tr[^>]+>\\s*<td>\\s*<a.*? href=[\"']Transact[^'\"]+[\"']>\\s*([\\d-]+)\\s*</a>\\s*</td><td>\\s*<a.*? href=[\"']Transact[^'\"]+[\"']>\\s*(.*?)\\s*</a>\\s*</td><td class=\"right\">\\s*(?:<span[^>]+>\\s*<a[^>]+>([^<]+)</a></span>\\s*)?</td><td class=\"right\">\\s*<a.*? href=[\"']Transact[^'\"]+[\"']>\\s*(.*?)\\s*</a>\\s*</td>\\s*</tr>", Pattern.CASE_INSENSITIVE);

	private String response = null;
	private String invoiceUrl;
	
	public DinersClub(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = URL;
	}

	public DinersClub(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
		this(context);
		this.update(username, password);
	}

    
    @Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
        urlopen = new Urllib(CertificateReader.getCertificates(context, R.raw.cert_dinersclub));
        try {
            response = urlopen.open("https://secure.dinersclub.se/dcs/login.aspx");
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
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
        
        List <NameValuePair> postData = new ArrayList <NameValuePair>();
        postData.add(new BasicNameValuePair("__EVENTTARGET", ""));
        postData.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
        postData.add(new BasicNameValuePair("__EVENTVALIDATION", eventValidation));
        postData.add(new BasicNameValuePair("__VIEWSTATE", viewState));
        postData.add(new BasicNameValuePair("ctl00$MainContent$Login1$UserName", username));
        postData.add(new BasicNameValuePair("ctl00$MainContent$Login1$Password", password));
        postData.add(new BasicNameValuePair("ctl00$MainContent$Login1$LoginButton", "Logga in"));

        return new LoginPackage(urlopen, postData, response, "https://secure.dinersclub.se/dcs/login.aspx");
    }

	public Urllib login() throws LoginException, BankException {
		try {
		    LoginPackage lp = preLogin();
            response = urlopen.open(lp.getLoginTarget(), lp.getPostData());		    
			if (response.contains("Har du glömt ditt lösenord")) {
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
		if (!"https://secure.dinersclub.se/dcs/eSaldo/Default.aspx".equalsIgnoreCase(urlopen.getCurrentURI())) {
		    try {
                response = urlopen.open("https://secure.dinersclub.se/dcs/eSaldo/Default.aspx");
            }
            catch (ClientProtocolException e) {
                throw new BankException(e.getMessage());
            }
            catch (IOException e) {
                throw new BankException(e.getMessage());
            }
		}

		Matcher matcher = reBalance.matcher(response);
		if (matcher.find()) {
            /*
             * Capture groups:
             * GROUP                EXAMPLE DATA
             * 1: Name              Privatkort
             * 2: Card number       1234 789456 741
             * 3: Balance           3.331,79 kr
             * 
             */
		    accounts.add(new Account(Html.fromHtml(matcher.group(1)).toString().trim(), Helpers.parseBalance(matcher.group(3)), "1"));
		    balance = balance.add(Helpers.parseBalance(matcher.group(3)));
		}
        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }

        /* Detect invoice dates - needed to find the transactions */
        matcher = reInvoices.matcher(response);
        if (matcher.find()) {
        	invoiceUrl = matcher.group(1);
        }
        else {
        	invoiceUrl = null;
        }

        super.updateComplete();
	}

	@Override
	public void updateTransactions(Account account, Urllib urlopen) throws LoginException, BankException {
		super.updateTransactions(account, urlopen);
		String response = null;
		Matcher matcher;
		try {
			/* We're going to look at all the pages until we find one that has transactions on it */
			response = urlopen.open(String.format("https://secure.dinersclub.se/dcs/eSaldo/%s", invoiceUrl));
			matcher = reTransactions.matcher(response);
			ArrayList<Transaction> transactions = new ArrayList<Transaction>();

			while (matcher.find()) {
				/*
				 * Capture groups:
				 * GROUP				EXAMPLE DATA
				 * 1: Trans. date		2010-10-06
				 * 2: Specifications	Skyways Express Ab
				 * 3: Foreign amount	30,30 EUR
				 * 4: Amount			2.462,00 kr
				 */

				transactions.add(new Transaction(matcher.group(1), matcher.group(2), Helpers.parseBalance(matcher.group(4))));
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
