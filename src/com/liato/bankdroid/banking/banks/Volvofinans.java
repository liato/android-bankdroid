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

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
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

import eu.nullbyte.android.urllib.Urllib;

public class Volvofinans extends Bank {
	private static final String TAG = "Volvofinans";
	private static final String NAME = "Volvofinans";
	private static final String NAME_SHORT = "volvofinans";
	private static final String URL = "https://secure.volvokort.com/";
	private static final int BANKTYPE_ID = IBankTypes.VOLVOFINANS;
    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;
    private static final String INPUT_HINT_USERNAME = "ÅÅÅÅMMDDXXXX";
    
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
        urlopen = new Urllib(true);
        urlopen.setContentCharset(HTTP.ISO_8859_1);
        List <NameValuePair> postData = new ArrayList <NameValuePair>();
        postData.add(new BasicNameValuePair("username", username));
        postData.add(new BasicNameValuePair("password", password));
        postData.add(new BasicNameValuePair("TARGET", "https://www.volvokort.com/privat/inloggning/redirect.html"));
        postData.add(new BasicNameValuePair("REFERER", "https://www.volvokort.com/privat/inloggning/forenklad.html"));
        return new LoginPackage(urlopen, postData, null, "https://secure.volvokort.com/neas/KodAuth");
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
			response = urlopen.open("https://www.volvokort.com/privat/kund/kortkonto/oversikt/kortkonton.html");
			try {
				JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
				JSONArray data = object.getJSONArray("data");

				int length = data.length();
				for (int index = 0; index < length; index++) {
					JSONObject account = data.getJSONObject(index);
					accounts.add(new Account(account.getString("kontonummer"), Helpers.parseBalance(account.getString("disponibeltBelopp")), "1"));
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
}
