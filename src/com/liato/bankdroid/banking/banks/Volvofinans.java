/*
 * Copyright (C) 2013 Nullbyte <http://nullbyte.eu>
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.content.Context;
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

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class Volvofinans extends Bank {
	private static final String TAG = "Volvofinans";
	private static final String NAME = "Volvofinans";
	private static final String NAME_SHORT = "volvofinans";
	private static final String URL = "https://inloggad.volvofinans.se/privat/inloggning/forenklad.html";
	private static final int BANKTYPE_ID = IBankTypes.VOLVOFINANS;
    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;
    private static final String INPUT_HINT_USERNAME = "ÅÅÅÅMMDDXXXX";
    private static SimpleDateFormat DATE_PARSER = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy");
    private static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd", new Locale("sv_SE"));
    private HashMap<String, String> mAccountUrlMappings = new HashMap<String, String>();
    
	public Volvofinans(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = URL;
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
        super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
	}

	public Volvofinans(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
		this(context);
		this.update(username, password);
	}

	@Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
        urlopen = new Urllib(CertificateReader.getCertificates(context, R.raw.cert_volvofinans));
        urlopen.setContentCharset(HTTP.ISO_8859_1);
        List <NameValuePair> postData = new ArrayList <NameValuePair>();
        postData.add(new BasicNameValuePair("username", username));
        postData.add(new BasicNameValuePair("password", password));
        postData.add(new BasicNameValuePair("TARGET", "https://inloggad.volvofinans.se/privat/inloggning/redirect.html"));
        postData.add(new BasicNameValuePair("REFERER", "https://inloggad.volvofinans.se/privat/inloggning/forenklad.html"));
        return new LoginPackage(urlopen, postData, null, "https://secure.volvofinans.se/neas/KodAuth");
    }

    @Override
	public Urllib login() throws LoginException, BankException {
	    try {
	        LoginPackage lp = preLogin();
	        String response = urlopen.open(lp.getLoginTarget(), lp.getPostData());

			if (response.contains("Fel personr/organisationsnr och/eller lösenord.")) {
				throw new LoginException(res.getText(R.string.invalid_username_password).toString());
			}

			if (response.contains("Internetbanken är stängd för tillfället och beräknas vara tillgänglig")) {
				throw new LoginException(res.getText(R.string.bank_closed).toString());
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
		String response = null;
		try {
			response = urlopen.open("https://inloggad.volvofinans.se/privat/kund/kortkonto/oversikt/kortkonton.html");
			try {
				JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
				JSONArray data = object.getJSONArray("data");

				int length = data.length();
				for (int index = 0; index < length; index++) {
					JSONObject account = data.getJSONObject(index);
					Document d = Jsoup.parse(account.getString("namnUrl"));
					Element e = d.getElementsByTag("a").first();
					if (e != null && e.attr("href") != null) {
    					mAccountUrlMappings.put(account.getString("kontonummer"), e.attr("href").replace("/info.html", "/info/kontoutdrag.html"));
					}
					accounts.add(new Account(String.format("%s (%s)", account.getString("namn"), account.getString("kontonummer")), Helpers.parseBalance(account.getString("disponibeltBelopp")).subtract(Helpers.parseBalance(account.getString("limit"))), account.getString("kontonummer")));
				}
			}
			catch (JSONException e) {
				throw new BankException(e.getMessage());
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
        String url = mAccountUrlMappings.get(account.getId());
        if (url != null) {
            try {
                response = urlopen.open("https://inloggad.volvofinans.se" + url);
                ArrayList<Transaction> transactions = new ArrayList<Transaction>();
                account.setTransactions(transactions);
                JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
                JSONArray data = object.getJSONArray("data");

                int length = data.length();
                for (int index = 0; index < length; index++) {
                    JSONObject acc = data.getJSONObject(index);
                    String date = acc.getString("datum");
                    try {
                        Date d = DATE_PARSER.parse(date);
                        date = DATE_FORMATTER.format(d);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    
                    transactions.add(new Transaction(date, acc.getString("text"), Helpers
                            .parseBalance(acc.getString("belopp")).negate()));
                }
                account.setTransactions(transactions);
                if (accounts.isEmpty()) {
                    throw new BankException(res.getText(R.string.no_accounts_found).toString());
                }

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
