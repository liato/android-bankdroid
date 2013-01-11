/*
 * Copyright (C) 2010 Nullbyte <http://nullbyte.eu>
 * Contributors: COLA
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
import java.util.Date;
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

public class OKQ8 extends Bank {
	private static final String TAG = "OKQ8";
	private static final String NAME = "OKQ8 VISA";
	private static final String NAME_SHORT = "okq8";
	private static final String URL = "https://nettbank.edb.com/Logon/index.jsp?domain=0066&from_page=http://www.okq8.se&to_page=https://nettbank.edb.com/cardpayment/transigo/logon/done/okq8";
	private static final int BANKTYPE_ID = IBankTypes.OKQ8;
    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;
    private static final String INPUT_HINT_USERNAME = "ÅÅMMDDXXXX";
    private static final boolean STATIC_BALANCE = true;
	
	private Pattern reLoginRedir = Pattern.compile("value=\"([^\"]*)\"", Pattern.CASE_INSENSITIVE);
	private Pattern reBalance = Pattern.compile("<div class=\"numberpositive\">([^<]*)</div>", Pattern.CASE_INSENSITIVE);
	private Pattern reTransactions = Pattern.compile("style=\"white-space: nowrap\">([^<]*)</td>\\s*<td[^>]*>[^<]*</td>\\s*<td[^>]*>[^<]*</td>\\s*<td[^>]*>([^<]*)</td>\\s*<td[^>]*><div[^>]*>([^<]*)</div></td>", Pattern.CASE_INSENSITIVE);
	private String response = null;
	
	public OKQ8(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = URL;
		super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
		super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
		super.STATIC_BALANCE = STATIC_BALANCE;
	}

	public OKQ8(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
		this(context);
		this.update(username, password);
	}

    
    @Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
        urlopen = new Urllib(true);
        Date d = new Date();
        List <NameValuePair> postData = new ArrayList <NameValuePair>();
        response = urlopen.open("https://nettbank.edb.com/Logon/index.jsp?domain=0066&from_page=http://www.okq8.se&to_page=https://nettbank.edb.com/cardpayment/transigo/logon/done/okq8");
        //p_tranid is the epoch time in milliseconds
        postData.add(new BasicNameValuePair("p_tranid", Long.toString(d.getTime())));
        postData.add(new BasicNameValuePair("p_errorScreen", "LOGON_REPOST_ERROR"));
        postData.add(new BasicNameValuePair("n_bank", ""));
        postData.add(new BasicNameValuePair("empty_pwd", ""));
        postData.add(new BasicNameValuePair("user_id", username.toUpperCase()));
        postData.add(new BasicNameValuePair("password", password));
        return new LoginPackage(urlopen, postData, response, "https://nettbank.edb.com/Logon/logon/step1");
    }

	public Urllib login() throws LoginException, BankException {
        Matcher matcher;
        String value = null;
		try {
			LoginPackage lp = preLogin();
			List <NameValuePair> postData = lp.getPostData();
			response = urlopen.open(lp.getLoginTarget(), postData);
			if (!response.contains("LOGON_OK")) {
				throw new LoginException(res.getText(R.string.invalid_username_password).toString());
			}
			
			/*
			 * After login ok we end up at an intermediate login page with a submit
			 * form that contains information that must be passed to the next page:
			 * <input type="hidden" name="so" value="xxx"/>
			 * <input type="hidden" name="last_logon_time" value="xxx"/>
			 * <input type="hidden" name="failed_logon_attempts" value="xxx"/>
			 * <input type="hidden" name="login_service_url" value="xxx"/>
			 */
			matcher = reLoginRedir.matcher(response);
			postData.clear();
			if(!matcher.find())
			{
				throw new LoginException("Could not find value for 'so'.");
			}
			value = matcher.group(1);
			postData.add(new BasicNameValuePair("so", value));
			
			if(!matcher.find())
			{
				throw new LoginException("Could not find value for 'last_logon_time'.");
			}
			value = matcher.group(1);
			postData.add(new BasicNameValuePair("last_logon_time", value));
			
			if(!matcher.find())
			{
				throw new LoginException("Could not find value for 'failed_logon_attempts'.");
			}
			value = matcher.group(1);
			postData.add(new BasicNameValuePair("failed_logon_attempts", value));
			
			if(!matcher.find())
			{
				throw new LoginException("Could not find value for 'login_service_url'.");
			}
			value = matcher.group(1);
			postData.add(new BasicNameValuePair("login_service_url", value));
			
			response = urlopen.open("https://nettbank.edb.com/cardpayment/transigo/logon/done/okq8", postData);
			
			if(response.contains("HTML REDIRECT"))
			{
				throw new LoginException("Login failed.");
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
		if (response == null) {
			urlopen = login();
		}
		try {
			/*
			 * The start page contains the balance of the account ("Kvar att utnytta") so read it.
			 * The balance is the first value (of three) that are matched by reBalance expression.
			 */
			Matcher matcher;
			matcher = reBalance.matcher(response);
			
			/*
			 * The start page contains the balance of the account ("Kvar att utnytta") so read it.
			 * The balance is the first value (of three) that are matched by reBalance expression.
			 */
			matcher = reBalance.matcher(response);
			if(matcher.find())
			{
			    accounts.add(new Account("Kvar att utnyttja" , Helpers.parseBalance(matcher.group(1)), "1"));
			    balance = balance.add(Helpers.parseBalance(matcher.group(1)));
			}
			/*
			 * Find the next value that is "Saldo". Add a new account but don't add to the balance.
			 */
			if(matcher.find())
			{
			    accounts.add(new Account("Saldo" , Helpers.parseBalance(matcher.group(1)), "2"));
			    accounts.add(new Account("Saldo" , Helpers.parseBalance(matcher.group(1)).negate(), "4"));
			}
			/*
			 * Find the next value that is "Köpgräns". Add a new account but don't add to the balance.
			 */
			if(matcher.find())
			{
			    accounts.add(new Account("Köpgräns" , Helpers.parseBalance(matcher.group(1)), "3"));
			}			
			
			if (accounts.isEmpty()) {
				throw new BankException(res.getText(R.string.no_accounts_found).toString());
			}
	
	
			response = urlopen.open("https://nettbank.edb.com/cardpayment/transigo/card/overview/lastTransactionsAccount");
	
			matcher = reTransactions.matcher(response);
			ArrayList<Transaction> transactions = new ArrayList<Transaction>();
			while (matcher.find()) {
				/*
				 * Capture group 1 = date
				 * Capture group 2 = text
				 * Capture group 3 = amount
				 * Negate the amount since buys are reported as positive.
				 */
				transactions.add(new Transaction(matcher.group(1).trim(), Html.fromHtml(matcher.group(2)).toString().trim(), Helpers.parseBalance(matcher.group(3)).negate()));
			}
			accounts.get(0).setTransactions(transactions);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
        finally {
            super.updateComplete();
        }
	}
}
