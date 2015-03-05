/* Copyright (C) 2012 Nullbyte <http://nullbyte.eu>
 * Brummer Privat Kapitalförsäkring & Pension support by Per Wigren <per.wigren@gmail.com>
 * Probably does not work with depå/ISK or company accounts as they have separate login pages.
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
import com.liato.bankdroid.legacy.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class BrummerKF extends Bank {
	private static final String TAG = "BrummerKF";
	private static final String NAME = "Brummer KF & Pension";
	private static final String NAME_SHORT = "brummer_kf";
	private static final String URL = "https://www.brummer.se/";
	private static final int BANKTYPE_ID = IBankTypes.BRUMMER_KF;
    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_TEXT;
    private static final int INPUT_TYPE_PASSWORD = InputType.TYPE_CLASS_TEXT;
    private static final String INPUT_HINT_USERNAME = "YYYYMMDDNNNN";
    private static final boolean STATIC_BALANCE = true;

	private Pattern reError = Pattern.compile("<li>(Personnummer och l.*?senord matchar ej\\.)</li>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private Pattern reViewstate = Pattern.compile("\"__VIEWSTATE\"\\s+value=\"(.*?)\"", Pattern.DOTALL);
	private Pattern reEventValidation = Pattern.compile("\"__EVENTVALIDATION\"\\s+value=\"(.*?)\"", Pattern.DOTALL);
	private Pattern reAccounts = Pattern.compile("<td.*?>\\s*<a.*?>\\s*(.*?)&nbsp;(.*?)\\s*</a>\\s*</td>\\s*<td.*?>\\s*(.*?)\\s*</td>\\s*<td.*?>\\s*(.*?)\\s*</td>\\s*<td.*?>\\s*(.*?)\\s*</td>\\s*<td.*?>\\s*(.*?)\\s*</td>\\s*<td.*?>\\s*<a.*?>\\s*Avtalsinformation\\s*</a>",Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	
	String response;
	
	public BrummerKF(Context context) {
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

	public BrummerKF(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
		this(context);
		this.update(username, password);
	}

    
    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        urlopen = new Urllib(context, CertificateReader.getCertificates(context, R.raw.cert_brummer));
        urlopen.setAllowCircularRedirects(true);
        response = urlopen.open("https://www.brummer.se/sv/online/privat/Login/");
        
        Matcher mViewstate = reViewstate.matcher(response);
        if (!mViewstate.find()) {
            throw new BankException(res.getText(R.string.unable_to_find).toString()+" Viewstate.");
        }
        String viewstate       = mViewstate.group(1);
        
        Matcher mEventValidation = reEventValidation.matcher(response);
        if (!mEventValidation.find()) {
            throw new BankException(res.getText(R.string.unable_to_find).toString()+" EventValidation.");
        }
        String eventvalidation       = mEventValidation.group(1);
        
        List <NameValuePair> postData = new ArrayList <NameValuePair>();
        postData.add(new BasicNameValuePair("__VIEWSTATE", viewstate));
        postData.add(new BasicNameValuePair("__EVENTVALIDATION", eventvalidation));
        postData.add(new BasicNameValuePair("ctl00$cphMainRegion$txtUsername", username));
        postData.add(new BasicNameValuePair("ctl00$cphMainRegion$txtPassword", password));
        postData.add(new BasicNameValuePair("ctl00$cphMainRegion$btnLogin", "Logga in"));
        postData.add(new BasicNameValuePair("ctl00$ctl08", "sv"));
        postData.add(new BasicNameValuePair("ctl00$ucHeader$ctl01$loginView$ctl01$ddlQuickMenu", "-1"));
        return new LoginPackage(urlopen, postData, response, "https://www.brummer.se/sv/online/privat/Login/");
    }
    
	public Urllib login() throws LoginException, BankException, IOException {
		LoginPackage lp = preLogin();
		String response = urlopen.open(lp.getLoginTarget(), lp.getPostData());

		Matcher matcher = reError.matcher(response);
		if (matcher.find()) {
		    String errormsg = Html.fromHtml(matcher.group(1).trim()).toString();
		    if (errormsg.contains("Personnummer")) {
		        throw new LoginException(errormsg);
		    }
		    else {
                 throw new BankException(errormsg);
		    }
		}
		return urlopen;
	}	
	
	@Override
	public void update() throws BankException, LoginException, BankChoiceException, IOException {
		super.update();
		if (username == null || password == null || username.length() == 0 || password.length() == 0) {
			throw new LoginException(res.getText(R.string.invalid_username_password).toString());
		}

		urlopen = login();
		Matcher matcher;

        response = urlopen.open("https://www.brummer.se/sv/online/privat/");
		matcher = reAccounts.matcher(response);
			
		while (matcher.find()) {
            /*
             * 1: Kontonamn
             * 2: Kontonummer
             * 3: Avkastning under året
             * 4: Genomsnittlig årlig avkastning sedan start
             * 5: Avkastning sedan start
             * 6: Marknadsvärde (kronor)
             */

			accounts.add(new Account(Html.fromHtml(matcher.group(1)).toString().trim(), Helpers.parseBalance(matcher.group(6).trim()), matcher.group(2)));

			balance = balance.add(Helpers.parseBalance(matcher.group(6)));
		}
		if (accounts.isEmpty()) {
			throw new BankException(res.getText(R.string.no_accounts_found).toString());
		}
	}
}
