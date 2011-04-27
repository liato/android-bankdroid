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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.text.Html;
import android.util.Log;

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

public class AmericanExpress extends Bank {
	private static final String TAG = "AmericanExpress";
	private static final String NAME = "American Express";
	private static final String NAME_SHORT = "americanexpress";
	private static final String URL = "https://home.americanexpress.com/home/se/home_c.shtml";
	private static final int BANKTYPE_ID = IBankTypes.AMERICANEXPRESS;
	
	private Pattern reAccounts = Pattern.compile("leftnav'\\)\">([^<]+)</a></div>\\s*</td>\\s*<td\\s*colspan=\"6\"\\s*id=\"headerSectionLeft\">\\s*<span\\s*class=\"cardTitle\">.*?BPIndex=(\\d{1,})&[^>]+>([^<]+)</a>.*?Utest&aring;ende skuld</div>\\s*<div[^>]+>[^<]+</div>\\s*<div[^>]+>([^<]+)</div>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private Pattern reTransactions = Pattern.compile("tableStandardText\"\\s*id=\"Roc\\d{1,}\">\\s*<td[^>]+>\\s*(\\d{1,2}\\s[a-z]{3}\\s\\d{4})</td>\\s*<td[^>]+>([^<]+)</td>\\s*<td[^>]+>[^<]+</td>\\s*<td[^>]+>([^<]+)<", Pattern.CASE_INSENSITIVE);
	
	private String response = null;

	public AmericanExpress(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = URL;
	}

	public AmericanExpress(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
		this(context);
		this.update(username, password);
	}

    @Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
        urlopen = new Urllib(true, true);
        urlopen.setContentCharset(HTTP.ISO_8859_1);
        response = urlopen.open("https://home.americanexpress.com/home/se/home_c.shtml");
        List <NameValuePair> postData = new ArrayList <NameValuePair>();

        postData.add(new BasicNameValuePair("request_type", "LogLogonHandler"));
        postData.add(new BasicNameValuePair("DestPage", "https://global.americanexpress.com/myca/intl/acctsumm/emea/accountSummary.do?request_type=&Face=sv_SE"));
        postData.add(new BasicNameValuePair("Face", "sv_SE"));
        postData.add(new BasicNameValuePair("brandname", ""));
        postData.add(new BasicNameValuePair("TARGET", "https://global.americanexpress.com/myca/intl/acctsumm/emea/accountSummary.do?request_type=&Face=sv_SE"));
        postData.add(new BasicNameValuePair("CHECKBOXSTATUS", "1"));
        postData.add(new BasicNameValuePair("Logon", "Continue..."));
        postData.add(new BasicNameValuePair("devicePrint", "version%3D1%26pm%5Ffpua%3Dmozilla%2F5%2E0%20%28windows%3B%20u%3B%20windows%20nt%206%2E1%3B%20en%2Dus%3B%20rv%3A1%2E9%2E2%2E7%29%20gecko%2F20100713%20firefox%2F3%2E6%2E7%20%28%20%2Enet%20clr%203%2E5%2E30729%3B%20%2Enet4%2E0c%29%7C5%2E0%20%28Windows%3B%20en%2DUS%29%7CWin32%26pm%5Ffpsc%3D24%7C1680%7C1050%7C988%26pm%5Ffpsw%3Dswf%7Cdef%7Cqt1%7Cqt2%7Cqt3%7Cqt4%7Cqt5%7Cqt6%26pm%5Ffptz%3D1%26pm%5Ffpln%3Dlang%3Den%2DUS%7Csyslang%3D%7Cuserlang%3D%26pm%5Ffpjv%3D1%26pm%5Ffpco%3D1"));
        postData.add(new BasicNameValuePair("REMEMBERME", "on"));
        postData.add(new BasicNameValuePair("manage", "option1"));
        postData.add(new BasicNameValuePair("UserID", username));
        postData.add(new BasicNameValuePair("USERID", username));
        postData.add(new BasicNameValuePair("Password", password));
        postData.add(new BasicNameValuePair("PWD", password));

        return new LoginPackage(urlopen, postData, response, "https://global.americanexpress.com/myca/logon/emea/action?request_type=LogLogonHandler&Face=sv_SE");
    }

	@Override
	public Urllib login() throws LoginException, BankException {
		try {
		    LoginPackage lp = preLogin();
			response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
			
			if (!response.contains("Your Personal Cards")) {
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
	public void update() throws BankException, LoginException, BankChoiceException {
		super.update();
		if (username == null || password == null || username.length() == 0 || password.length() == 0) {
			throw new LoginException(res.getText(R.string.invalid_username_password).toString());
		}
		
		Log.d(TAG, "Logging in...");
		urlopen = login();
        Log.d(TAG, "Url after login: " + urlopen.getCurrentURI());
		
		Matcher matcher = reAccounts.matcher(response);
		while (matcher.find()) {
            /*
             * Capture groups:
             * GROUP                    EXAMPLE DATA
             * 1: Account number        XXX-11111
             * 2: ID                    0
             * 3: Name                  SAS EuroBonus American Express&reg; Card
             * 4: Amount                1.111,11 kr
             * 
             */   			    
			accounts.add(new Account(Html.fromHtml(matcher.group(3)).toString().trim(),
			        Helpers.parseBalance(matcher.group(4)).negate(),
			        matcher.group(2).trim()));
			balance = balance.add(Helpers.parseBalance(matcher.group(4)).negate());
		}
		
		if (accounts.isEmpty()) {
			throw new BankException(res.getText(R.string.no_accounts_found).toString());
		}
	    super.updateComplete();
	}

	@Override
	public void updateTransactions(Account account, Urllib urlopen) throws LoginException, BankException {
		super.updateTransactions(account, urlopen);

		try {
			response = urlopen.open("https://www99.americanexpress.com/myca/intl/estatement/emea/statement.do?request_type=&Face=sv_SE&sorted_index=0&BPIndex=" + account.getId());
			Matcher matcher = reTransactions.matcher(response);
			ArrayList<Transaction> transactions = new ArrayList<Transaction>();
			while (matcher.find()) {
                /*
                 * Capture groups:
                 * GROUP                    EXAMPLE DATA
                 * 1: Date                  17 jan 2011
                 * 2: Specification         xx
                 * 3: Amount                2,00&nbsp;kr
                 * 
                 */
                SimpleDateFormat sdfFrom = new SimpleDateFormat("d MMM yyyy");
                SimpleDateFormat sdfTo = new SimpleDateFormat("yyyy-MM-dd");
                Date transactionDate;
                try {
                    transactionDate = sdfFrom.parse(matcher.group(1).trim());
                    String strDate = sdfTo.format(transactionDate);
                    transactions.add(new Transaction(strDate,
                                                     Html.fromHtml(matcher.group(2)).toString().trim(),
                                                     Helpers.parseBalance(matcher.group(3).trim()).negate()));
                }
                catch (ParseException e) {
                    Log.d(TAG, "Unable to parse date: " + matcher.group(1).trim());
                }
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