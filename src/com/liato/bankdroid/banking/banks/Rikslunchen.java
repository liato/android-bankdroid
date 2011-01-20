/*
 * Copyright (C) 2010 Nullbyte <http://nullbyte.eu>
 * Contributors: PMC
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
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.text.InputType;

import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;

import eu.nullbyte.android.urllib.Urllib;

public class Rikslunchen extends Bank {

	private static final String TAG = "Rikslunchen";
	private static final String NAME = "Rikslunchen";
	private static final String NAME_SHORT = "rikslunchen";
	private static final String URL = "http://www.rikslunchen.se/index.html";
	private static final int BANKTYPE_ID = Bank.RIKSLUNCHEN;
	private static final int INPUT_TYPE_PASSWORD = InputType.TYPE_CLASS_PHONE;
    protected boolean INPUT_HIDDEN_USERNAME = true;
    protected int INPUT_TITLETEXT_PASSWORD = R.string.card_id;	

	private String myResponse = "";

	public Rikslunchen(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = URL;
		super.INPUT_HINT_USERNAME = "Rikslunchen";
		super.INPUT_TYPE_PASSWORD = INPUT_TYPE_PASSWORD;
		super.INPUT_HIDDEN_USERNAME = INPUT_HIDDEN_USERNAME;
		super.INPUT_TITLETEXT_PASSWORD = INPUT_TITLETEXT_PASSWORD;
	}

	public Rikslunchen(String username, String password, Context context) throws BankException, LoginException {
		this(context);
		this.update(username, password);
	}

	@Override
	protected LoginPackage preLogin() throws BankException, ClientProtocolException, IOException {
		urlopen = new Urllib(true, true);

		List<NameValuePair> postData = new ArrayList<NameValuePair>();
		postData.add(new BasicNameValuePair("c0-param0", "string:" + password));
		postData.add(new BasicNameValuePair("callCount", "1"));
		postData.add(new BasicNameValuePair("windowName", ""));
		postData.add(new BasicNameValuePair("c0-scriptName", "cardUtil"));
		postData.add(new BasicNameValuePair("c0-methodName", "getCardData"));
		postData.add(new BasicNameValuePair("c0-id", "0"));
		postData.add(new BasicNameValuePair("batchId", "1"));
		postData.add(new BasicNameValuePair("page", "%2Findex.html"));
		postData.add(new BasicNameValuePair("httpSessionId", ""));
		postData.add(new BasicNameValuePair("scriptSessionId", ""));

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("http://www.rikslunchen.se/dwr/call/plaincall/cardUtil.getCardData.dwr");
		httppost.setEntity(new UrlEncodedFormEntity(postData));

		HttpResponse response = httpclient.execute(httppost);
		InputStream streamResponse = response.getEntity().getContent();
		StringWriter writer = new StringWriter();
		IOUtils.copy(streamResponse, writer);

		return new LoginPackage(urlopen, postData, writer.toString(), "http://www.rikslunchen.se/dwr/call/plaincall/cardUtil.getCardData.dwr");
	}

	@Override
	public Urllib login() throws LoginException, BankException {
		try {
			LoginPackage lp = preLogin();

			if (lp.getResponse().contains("Ange giltigt kortnummer.")) {
				throw new LoginException(res.getText(R.string.invalid_username_password).toString());
			}

			myResponse = lp.getResponse();

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
		if (password == null || password.length() == 0) {
			throw new LoginException(res.getText(R.string.invalid_username_password).toString());
		}

		urlopen = login();
		try {

			int begin = myResponse.indexOf("balance");
			int end = myResponse.indexOf("cardNo");
			BigDecimal balance = Helpers.parseBalance(myResponse.substring(begin + 9, end - 2));

			accounts.add(new Account("Rikslunchen", balance, "1"));
		} finally {
			super.updateComplete();
		}
	}
}