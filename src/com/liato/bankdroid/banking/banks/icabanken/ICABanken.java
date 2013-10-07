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

package com.liato.bankdroid.banking.banks.icabanken;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.client.ClientProtocolException;

import android.content.Context;
import android.os.Build;
import android.text.InputType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liato.bankdroid.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.Transaction;
import com.liato.bankdroid.banking.banks.icabanken.model.IcaBankenAccount;
import com.liato.bankdroid.banking.banks.icabanken.model.IcaBankenTransaction;
import com.liato.bankdroid.banking.banks.icabanken.model.response.LoginResponse;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;

import eu.nullbyte.android.urllib.Urllib;

public class ICABanken extends Bank {
	private static final String TAG = "ICABanken";
	private static final String NAME = "ICA Banken";
	private static final String NAME_SHORT = "icabanken";
	private static final String URL = "https://mobil.icabanken.se/";
	private static final int BANKTYPE_ID = IBankTypes.ICABANKEN;
	private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;
	private static final int INPUT_TYPE_PASSWORD = InputType.TYPE_CLASS_PHONE;
	private static final String INPUT_HINT_USERNAME = "ÅÅÅÅMMDD-XXXX";
	private static final boolean STATIC_BALANCE = false;

	private static final String API_URL = "https://appserver.icabanken.se";

	public ICABanken(Context context) {
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

	public ICABanken(String username, String password, Context context)
			throws BankException, LoginException, BankChoiceException {
		this(context);
		this.update(username, password);
	}

	public Urllib login() throws LoginException, BankException {
		urlopen = new Urllib();
		urlopen.addHeader("Accept", "application/json");

		try {
			String response = urlopen.open(API_URL + "/services/saldo?customerId="
					+ username + "&password=" + password);
			if(response == null || "".equals(response)) {
				throw new LoginException(res.getText(
						R.string.invalid_username_password).toString());
			}
			ObjectMapper vObjectMapper = new ObjectMapper();
			vObjectMapper.setDateFormat(new SimpleDateFormat(
					"yyyy-MM-dd hh:mm:ss", new Locale("sv", "SE")));
			LoginResponse loginResponse = vObjectMapper.readValue(response,
					LoginResponse.class);

			addAccounts(loginResponse.getAccounts());

		} catch (ClientProtocolException e) {
			throw new BankException(e.getMessage());
		} catch (IOException e) {
			throw new BankException(e.getMessage());
		}
		return urlopen;
	}

	@Override
	public void update() throws BankException, LoginException,
			BankChoiceException {
		super.update();
		if (username == null || password == null || username.length() == 0
				|| password.length() == 0) {
			throw new LoginException(res.getText(
					R.string.invalid_username_password).toString());
		}
		login();

		if (accounts.isEmpty()) {
			throw new BankException(res.getText(R.string.no_accounts_found)
					.toString());
		}

	}

	@Override
	public void updateTransactions(Account account, Urllib urlopen)
			throws LoginException, BankException {
		super.updateTransactions(account, urlopen);
		super.updateComplete();
	}

	private void addAccounts(List<IcaBankenAccount> pAccountList) {
		for (IcaBankenAccount icaAccount : pAccountList) {
			Account account = new Account(icaAccount.getName()
					+ " (Disponibelt)", icaAccount.getAvailableAmount(),
					icaAccount.getAccountNumber());
			account.setTransactions(mapTransactions(icaAccount));
			Account alias = new Account(icaAccount.getName() + " (Saldo)",
					icaAccount.getCurrentAmount(), "a:"
							+ icaAccount.getAccountNumber());
			alias.setAliasfor(icaAccount.getAccountNumber());
			accounts.add(account);
			accounts.add(alias);
			balance.add(account.getBalance());
		}
	}

	private ArrayList<Transaction> mapTransactions(IcaBankenAccount pAccount) {
		ArrayList<Transaction> transactions = new ArrayList<Transaction>();
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd",
				Locale.getDefault());
		for (IcaBankenTransaction icaTransaction : pAccount.getTransactions()) {
			String date = dateFormatter.format(icaTransaction.getPostedDate());
			Transaction transaction = new Transaction(date,
					icaTransaction.getMemoText(), icaTransaction.getAmount());
			transactions.add(transaction);
		}
		return transactions;
	}
}
