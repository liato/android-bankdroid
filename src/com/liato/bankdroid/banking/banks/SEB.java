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
import org.apache.http.protocol.HTTP;

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
import com.liato.bankdroid.provider.IBankTypes;

import eu.nullbyte.android.urllib.Urllib;

public class SEB extends Bank {
	private static final String TAG = "SEB";
	private static final String NAME = "SEB";
	private static final String NAME_SHORT = "seb";
	private static final String URL = "https://m.seb.se/cgi-bin/pts3/mpo/9000/mpo9001.aspx?P1=logon.htm";
	private static final int BANKTYPE_ID = IBankTypes.SEB;
    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;
    private static final String INPUT_HINT_USERNAME = "ÅÅMMDDXXXX";
	
	private Pattern reAccounts = Pattern.compile("/cgi-bin/pts3/mps/1100/mps1102\\.aspx\\?M1=show&amp;P1=([^&]+)&amp;P2=1&amp;P4=1\">([^<]+)</a></td>\\s*</tr>\\s*<tr[^>]+>\\s*<td>[^<]+</td>\\s*<td[^>]+>[^<]+</td>\\s*<td[^>]+>([^<]+)</td>\\s*", Pattern.CASE_INSENSITIVE);
	private Pattern reTransactions = Pattern.compile("(\\d{6})\\s*<br\\s?/>\\s*<span\\s*id=\"MPSMaster_MainPlaceHolder_repAccountTransactions[^\"]+\"\\s*class=\"name\">([^/]+)(?:/(\\d{2}-\\d{2}-\\d{2}))?</span>\\s*<span\\s*id=\"MPSMaster_MainPlaceHolder_repAccountTransactions[^\"]+\"\\s*class=\"value\">([^<]+)</span>", Pattern.CASE_INSENSITIVE);
	
	private String response = null;

	public SEB(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = URL;
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
        super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
	}

	public SEB(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
		this(context);
		this.update(username, password);
	}

    @Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
        urlopen = new Urllib(true);
        urlopen.setContentCharset(HTTP.ISO_8859_1);
        urlopen.addHeader("Referer", "https://m.seb.se/");
        urlopen.setKeepAliveTimeout(5);
        //response = urlopen.open("https://m.seb.se/cgi-bin/pts3/mpo/9000/mpo9001.aspx?P1=logon.htm");
        List <NameValuePair> postData = new ArrayList <NameValuePair>();
        postData.add(new BasicNameValuePair("A1", username));
        postData.add(new BasicNameValuePair("A2", password));
        return new LoginPackage(urlopen, postData, response, "https://m.seb.se/cgi-bin/pts3/mps/1000/mps1001b.aspx");
    }

	@Override
	public Urllib login() throws LoginException, BankException {
		try {
		    LoginPackage lp = preLogin();
			response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
			
			if (!response.contains("1100/mps1101.aspx?X1=passWord")) {
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
		
		urlopen = login();
		Matcher matcher;
		try {
			response = urlopen.open("https://m.seb.se/cgi-bin/pts3/mps/1100/mps1101.aspx?X1=passWord");
			matcher = reAccounts.matcher(response);
			while (matcher.find()) {
                /*
                 * Capture groups:
                 * GROUP                    EXAMPLE DATA
                 * 1: ID                    GJmQRqlrOPmM++1zf50d6Q==
                 * 2: Name                  Personkonto
                 * 3: Amount                2.208,03
                 * 
                 */   			    
				accounts.add(new Account(Html.fromHtml(matcher.group(2)).toString().trim(), Helpers.parseBalance(matcher.group(3)), matcher.group(1).trim()));
				balance = balance.add(Helpers.parseBalance(matcher.group(3)));
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

		//No transaction history for loans, funds and credit cards.
		int accType = account.getType();
		if (accType == Account.LOANS || accType == Account.FUNDS || accType == Account.CCARD) return;

		Matcher matcher;
		try {
			response = urlopen.open("https://m.seb.se/cgi-bin/pts3/mps/1100/mps1102.aspx?M1=show&P2=1&P4=1&P1=" + account.getId());
			matcher = reTransactions.matcher(response);
			ArrayList<Transaction> transactions = new ArrayList<Transaction>();
			while (matcher.find()) {
                /*
                 * Capture groups:
                 * GROUP                    EXAMPLE DATA
                 * 1: Book. date            101214
                 * 2: Transaction           St1
                 * 3: Trans. date           10-12-11
                 * 4: Amount                -200,07  
                 * 
                 */
			    String date;
			    if (matcher.group(3) == null || matcher.group(3).length() == 0) {
			        date = Html.fromHtml(matcher.group(1)).toString().trim();
			        date = String.format("%s-%s-%s", date.substring(0,2), date.substring(2,4), date.substring(4,6));
			    }
			    else {
			        date = Html.fromHtml(matcher.group(3)).toString().trim();
			    }
				transactions.add(new Transaction("20"+date, Html.fromHtml(matcher.group(2)).toString().trim(), Helpers.parseBalance(matcher.group(4))));
			}
			Collections.sort(transactions, Collections.reverseOrder());
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