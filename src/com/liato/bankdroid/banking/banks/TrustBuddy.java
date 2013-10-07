/* Copyright (C) 2012 Nullbyte <http://nullbyte.eu>, first version by Snah@Swedroid 2012-01-06
 * Modified for TrustBuddy by Per Wigren <per.wigren@gmail.com>
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
import android.text.InputType;
import android.util.Log;

import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class TrustBuddy extends Bank {
	private static final String TAG = "TrustBuddy";
	private static final String NAME = "TrustBuddy";
	private static final String NAME_SHORT = "trustbuddy";
	private static final String URL = "https://www.trustbuddy.com/";
	private static final int BANKTYPE_ID = IBankTypes.TRUSTBUDDY;
	private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_TEXT;
	private static final int INPUT_TYPE_PASSWORD = InputType.TYPE_CLASS_TEXT;
	private static final String INPUT_HINT_USERNAME = "Your email";
	private static final boolean STATIC_BALANCE = true;

	private Pattern reError = Pattern.compile("<h3>error\\s*:\\s*</h3>\\s*<p>\\s*(.*?)\\s*</p>",Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private Pattern reAccounts = Pattern.compile("<h1>\\s*([^<]+).*?>Balans: ([0-9, ]+) ([A-Z]{3})<",Pattern.CASE_INSENSITIVE | Pattern.DOTALL);	

	public TrustBuddy(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = URL;
		super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
		super.INPUT_TYPE_PASSWORD = INPUT_TYPE_PASSWORD;
		super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
		super.STATIC_BALANCE = STATIC_BALANCE;
	}

	public TrustBuddy(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
		this(context);
		this.update(username, password);
	}

    
    @Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
        urlopen = new Urllib(CertificateReader.getCertificates(context, R.raw.cert_trustbuddy));
        urlopen.setAllowCircularRedirects(true);
        List <NameValuePair> postData = new ArrayList <NameValuePair>();
        postData.add(new BasicNameValuePair("username", username));
        postData.add(new BasicNameValuePair("password", password));
        postData.add(new BasicNameValuePair("logon", "Logga in"));
        return new LoginPackage(urlopen, postData, null, "https://trustbuddy.com/se/logga_in/");
    }
    
	public Urllib login() throws LoginException, BankException {
		try {
			LoginPackage lp = preLogin();
			String response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
			Matcher matcher = reError.matcher(response);
			if (matcher.find()) {
			    String errormsg = Html.fromHtml(matcher.group(1).trim()).toString();
			    if (errormsg.contains("Felaktigt")) {
			        throw new LoginException(errormsg);    
			    }
			    else {
	                 throw new BankException(errormsg);    
			    }
			}
		}
		catch (ClientProtocolException e) {
			Log.e(TAG, "ClientProtocolException: "+e.getMessage());
			throw new BankException(e.getMessage());
		}
		catch (IOException e) {
			Log.e(TAG, "IOException: "+e.getMessage());
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
		String response = null;
		Matcher matcher;
		try {
			response = urlopen.open("https://trustbuddy.com/se/din_sida/");
			matcher = reAccounts.matcher(response);
			
			while (matcher.find()) {
                /*
                 * 1: Land
                 * 2: Saldo
                 * 3: Valuta
                 */
				Account temp = new Account(Html.fromHtml(matcher.group(1)).toString().trim(), Helpers.parseBalance(matcher.group(2).trim()), matcher.group(1).toLowerCase());
				temp.setCurrency(matcher.group(3));
				accounts.add(temp);
	            
				balance = balance.add(Helpers.parseBalance(matcher.group(2)));
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
	}
}
