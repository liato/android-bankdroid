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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.content.Context;
import android.text.Html;

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

public class AvanzaMini extends Bank {
	private static final String TAG = "AvanzaMini";
	private static final String NAME = "Avanza Mini";
	private static final String NAME_SHORT = "avanzamini";
	private static final String URL = "https://www.avanza.se/mini/hem/";
	private static final int BANKTYPE_ID = IBankTypes.AVANZAMINI;
	
    private Pattern reAvanzaMini = Pattern.compile("w100\\s+azatable\"[^>]+>\\s*<tbody>\\s*(.*)</tbody>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private Pattern accountsAvanzaMini = Pattern.compile("<tr>\\s*<td>([^<]+)</td>\\s*<td\\s+class=\"tright\">([^<]+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	public AvanzaMini(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = URL;
	}

	public AvanzaMini(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
		this(context);
		this.update(username, password);
	}

    @Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
        urlopen = new Urllib(CertificateReader.getCertificates(context, R.raw.cert_avanza));
        urlopen.setAllowCircularRedirects(true);
        String response = urlopen.open("https://www.avanza.se/mini/logga_in/");
        Document d = Jsoup.parse(response);
        Element e = d.getElementById("javax.faces.ViewState");
        if (e == null || e.attr("value") == null) {
            throw new BankException(res.getText(R.string.unable_to_find).toString() + " ViewState.");
        }
        String viewState = e.attr("value");
        e = d.select("input[type=submit]").first();
        if (e == null || e.attr("value") == null || e.attr("name") == null) {
            throw new BankException(res.getText(R.string.unable_to_find).toString() + " SubmitValue.");
        }
        String submitButtonName = e.attr("name");
        String submitButtonValue = e.attr("value");
        List <NameValuePair> postData = new ArrayList <NameValuePair>();
        postData.add(new BasicNameValuePair("login", "login"));
        postData.add(new BasicNameValuePair("username", username));
        postData.add(new BasicNameValuePair("password", password));
        postData.add(new BasicNameValuePair("conversationPropagation", "none"));
        postData.add(new BasicNameValuePair("javax.faces.ViewState", viewState));
        postData.add(new BasicNameValuePair(submitButtonName, submitButtonValue));
        return new LoginPackage(urlopen, postData, null, "https://www.avanza.se/mini/logga_in/");
    }

	@Override
	public Urllib login() throws LoginException, BankException {
		String response = null;
		try {
			LoginPackage lp = preLogin();
			urlopen.addHeader("Referer", "https://www.avanza.se/mini/logga_in/");
			response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
			if (response.contains("Felaktigt") && response.contains("Logga in")) {
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
		String response = null;
		Matcher matcher;
		try {
			response = urlopen.open("https://www.avanza.se/mini/mitt_konto/");
			matcher = reAvanzaMini.matcher(response);
	        if (matcher.find()) {
	            int count = 1;
	            Matcher submatcher = accountsAvanzaMini.matcher(matcher.group(1));
	            while (submatcher.find()){
	                accounts.add(new Account(Html.fromHtml(submatcher.group(1)).toString().trim(), Helpers.parseBalance(submatcher.group(2)), Integer.toString(count)));
	                balance = balance.add(Helpers.parseBalance(submatcher.group(2)));
	                count++;
	            }
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
}