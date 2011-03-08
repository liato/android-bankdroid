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
import java.math.BigDecimal;
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
import android.text.InputType;
import android.util.Log;

import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.Transaction;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;

import eu.nullbyte.android.urllib.Urllib;

public class Eurocard extends Bank {
	private static final String TAG = "Eurocard";
	private static final String NAME = "Eurocard";
	private static final String NAME_SHORT = "eurocard";
	private static final String URL = "https://e-saldo.eurocard.se/nis/external/ecse/login.do";
	private static final int BANKTYPE_ID = IBankTypes.EUROCARD;
	private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;
    private static final String INPUT_HINT_USERNAME = "ÅÅMMDDXXXX";
	
	private Pattern reAccounts = Pattern.compile("Welcomepagecardimagecontainer\">\\s*[^<]+<br>[^>]+<br>([^>]+)</div>\\s*</div>\\s*</div>.*?indentationwrapper\">\\s*<a\\s*href=\"getPendingTransactions\\.do\\?id=([^\"]+)\">.*?billedamount\">([^<]+)</div>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private Pattern reSaldo = Pattern.compile("Billingunitbalanceamount\">\\s*([^<]+)<", Pattern.CASE_INSENSITIVE);
	private Pattern reTransactions = Pattern.compile("transcol1\">\\s*<span>([^<]+)</span>\\s*</td>\\s*<td[^>]+>\\s*<span>([^<]+)</span>\\s*</td>\\s*<td[^>]+>\\s*(?:<div[^>]+>\\s*)?<span>([^<]*)</span>\\s*(?:</div>\\s*)?</td>\\s*<td[^>]+>\\s*<span>([^<]*)</span>\\s*</td>\\s*<td[^>]+>\\s*<span>([^<]*)</span>\\s*</td>\\s*<td[^>]+>\\s*<span>([^<]*)</span>\\s*</td>\\s*<td[^>]+>\\s*<span>([^<]+)</span>", Pattern.CASE_INSENSITIVE);
    private ArrayList<String> accountIds = new ArrayList<String>();
	private String response = null;
	public Eurocard(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = URL;
		super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
		super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
	}

	public Eurocard(String username, String password, Context context) throws BankException, LoginException {
		this(context);
		this.update(username, password);
	}

    
    @Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
        urlopen = new Urllib(true);
        List <NameValuePair> postData = new ArrayList <NameValuePair>();
        postData.add(new BasicNameValuePair("target", "/nis/ecse/main.do"));                
        postData.add(new BasicNameValuePair("prodgroup", "0005"));              
        postData.add(new BasicNameValuePair("USERNAME", "0005"+username));              
        postData.add(new BasicNameValuePair("METHOD", "LOGIN"));                
        postData.add(new BasicNameValuePair("CURRENT_METHOD", "PWD"));              
        postData.add(new BasicNameValuePair("uname", username));
        postData.add(new BasicNameValuePair("PASSWORD", password));
        return new LoginPackage(urlopen, postData, response, "https://e-saldo.eurocard.se/siteminderagent/forms/generic.fcc");
    }

	@Override
	public Urllib login() throws LoginException, BankException {
		try {
			LoginPackage lp = preLogin();
			response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
			if (response.contains("Felaktig kombination")) {
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
		Matcher matcher = reAccounts.matcher(response);
        Integer accountId = 0;
		while (matcher.find()) {
            /*
             * Capture groups:
             * GROUP                     EXAMPLE DATA           NOTES
             * 1: account number         **** **** **** 1234
             * 2: id                     a1c2d3d4e5f6s7b8c9d0   Changes when session expires
             * 3: ofakturerat amount     &nbsp;2 988,96
             * 
             */

		    // Create a separate account for "Ofakturerat".
		    // Set the balance for the main account to 0 and update it later
			accounts.add(new Account(Html.fromHtml(matcher.group(1)).toString().trim(), new BigDecimal(0), accountId.toString()));
			accounts.add(new Account("└ " + "Ofakturerat", Helpers.parseBalance(matcher.group(3)), "o:ofak:"+accountId.toString(), Account.OTHER));
            accountIds.add(matcher.group(2).trim());
            accountId += 1;			
		}

		try {
            response = urlopen.open("https://e-saldo.eurocard.se/nis/ecse/getBillingUnits.do");
            matcher = reSaldo.matcher(response);
            int i = 0;
            while (matcher.find()) {
                /*
                 * Capture groups:
                 * GROUP                     EXAMPLE DATA
                 * 1: balance                &nbsp;40 988,96
                 * 
                 */ 
                
                // Update the main account balance
                if (accounts.size() >= i*2+1) {
                    accounts.get(i*2).setBalance(Helpers.parseBalance(matcher.group(1)));
                }
            }
		}
        catch (ClientProtocolException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }


		if (accounts.isEmpty()) {
			throw new BankException(res.getText(R.string.no_accounts_found).toString());
		}
        super.updateComplete();
	}

	@Override
	public void updateTransactions(Account account, Urllib urlopen) throws LoginException, BankException {
		super.updateTransactions(account, urlopen);
		Matcher matcher;
		// If the account is of type "other" it's the fake "Ofakturerat" account.
		if (account.getType() == Account.OTHER) return;
		try {
		    String accountWebId = accountIds.get(Integer.parseInt(account.getId()));
			Log.d(TAG, "Opening: https://e-saldo.eurocard.se/nis/ecse/getPendingTransactions.do?id="+accountWebId);
			response = urlopen.open("https://e-saldo.eurocard.se/nis/ecse/getPendingTransactions.do?id="+accountWebId);
			matcher = reTransactions.matcher(response);
			ArrayList<Transaction> transactions = new ArrayList<Transaction>();
			while (matcher.find()) {
                /*
                 * Capture groups:
                 * GROUP                EXAMPLE DATA
                 * 1: trans. date       10-18
                 * 2: reg. date         10-19
                 * 3: specification     ICA Kvantum
                 * 4: location          Stockholm
                 * 5: currency          SEK
                 * 6: amount/tax        147,64
                 * 7: amount in sek     5791,18
                 * 
                 */
			    String[] monthday = matcher.group(1).trim().split("-");
				transactions.add(new Transaction(Helpers.getTransactionDate(monthday[0], monthday[1]),
				        Html.fromHtml(matcher.group(3)).toString().trim()+(matcher.group(4).trim().length() > 0 ? " ("+Html.fromHtml(matcher.group(4)).toString().trim()+")" : ""),
				        Helpers.parseBalance(matcher.group(7)).negate()));
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