/*
 * Copyright (C) 2011 Nullbyte <http://nullbyte.eu>
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import com.liato.bankdroid.legacy.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.Transaction;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class DanskeBank extends Bank {
	private static final String TAG = "DanskeBank";
	private static final String NAME = "DanskeBank";
	private static final String NAME_SHORT = "danskebank";
	private static final String URL = "https://mobil.danskebank.se/XI?WP=XAI&WO=Logon&WA=MBSELogon&gsSprog=SE&gsBrand=OEB";
	private static final int BANKTYPE_ID = IBankTypes.DANSKEBANK;
    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;
    private static final int INPUT_TYPE_PASSWORD = InputType.TYPE_CLASS_PHONE;
    private static final String INPUT_HINT_USERNAME = "ÅÅMMDDXXXX";
	
    private Pattern reSessionId = Pattern.compile("WSES=([^\"& ]+)", Pattern.CASE_INSENSITIVE);
    private Pattern rePersonnr = Pattern.compile("WAFT=([^\"& ]+)", Pattern.CASE_INSENSITIVE);
	private Pattern reAccounts = Pattern.compile("<a\\shref=\"[^\"]+KBList[^\"]+WCI=([^\"]+)\">([^<]+)</a><br/>([^<]+)<br/>Saldo:([^<]+)<br/>Disponibelt:([^<]+)<", Pattern.CASE_INSENSITIVE);
	private Pattern reTransactions = Pattern.compile("<a\\shref=\"[^\"]+KBDetVis[^\"]+\">([^<]+)</a><br/>Datum:([^<]+)<br/>Belopp:([^<]+)<br/>Status:([^<]+)<", Pattern.CASE_INSENSITIVE);
	
	private String response = null;
	private String mSessionId = null;
	private String mPersonnr = null;

	public DanskeBank(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = URL;
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
        super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
        super.INPUT_TYPE_PASSWORD = INPUT_TYPE_PASSWORD;
	}

	public DanskeBank(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
		this(context);
		this.update(username, password);
	}

    @Override
    protected LoginPackage preLogin() {
        urlopen = new Urllib(context, CertificateReader.getCertificates(context, R.raw.cert_danskebank));
        urlopen.setContentCharset(HTTP.ISO_8859_1);
        urlopen.addHeader("Referer", "https://mobil.danskebank.se/");

        List <NameValuePair> postData = new ArrayList <NameValuePair>();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd.MM.yyyy");
        postData.add(new BasicNameValuePair("gsSprog", "SE"));
        postData.add(new BasicNameValuePair("gsBrand", "OEB"));
        postData.add(new BasicNameValuePair("gsSession", ""));
        postData.add(new BasicNameValuePair("gsProdukt", "XAS"));
        postData.add(new BasicNameValuePair("gsNextObj", "Forside"));
        postData.add(new BasicNameValuePair("gsNextAkt", "MBForside"));
        postData.add(new BasicNameValuePair("gsNextUObj", "SC"));
        postData.add(new BasicNameValuePair("gsCurItem", ""));
        postData.add(new BasicNameValuePair("gsCurItem2", ""));
        postData.add(new BasicNameValuePair("gsCurItem3", ""));
        postData.add(new BasicNameValuePair("gsCurItem4", ""));
        postData.add(new BasicNameValuePair("gsCurItem5", ""));
        postData.add(new BasicNameValuePair("gsCurObj", "Logon"));
        postData.add(new BasicNameValuePair("gsCurAkt", "MBSELogon"));
        postData.add(new BasicNameValuePair("gsCurUObj", "SC"));
        postData.add(new BasicNameValuePair("hidStatusType", "A00"));
        postData.add(new BasicNameValuePair("hidStatusTekst", ""));
        postData.add(new BasicNameValuePair("hidStatusTid", sdf.format(new Date())));
        postData.add(new BasicNameValuePair("gsSikSystem", "KO"));
        postData.add(new BasicNameValuePair("gsLand", "SE"));
        postData.add(new BasicNameValuePair("gsAftlnr", username));
        postData.add(new BasicNameValuePair("gsLogon", password));
       
        return new LoginPackage(urlopen, postData, response, "https://mobil.danskebank.se/XI");
    }

	@Override
	public Urllib login() throws LoginException, BankException, IOException {
		LoginPackage lp = preLogin();
		response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
		if (response.contains("et personnummer eller servicekod du angett")) {
			throw new LoginException(res.getText(R.string.invalid_username_password).toString());
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
		matcher = reSessionId.matcher(response);
		if (matcher.find()) {
		    mSessionId = matcher.group(1);
		}
		else {
		    throw new BankException(res.getText(R.string.unable_to_find).toString() + " session id.");
		}
        matcher = rePersonnr.matcher(response);
        if (matcher.find()) {
            mPersonnr = matcher.group(1);
        }
        else {
            throw new BankException(res.getText(R.string.unable_to_find).toString() + " personnummer.");
        }

		response = urlopen.open(String.format("https://mobil.danskebank.se/XI?WP=XAS&WO=Konto&WA=KTList&WSES=%s&WAFT=%s", mSessionId, mPersonnr));
		matcher = reAccounts.matcher(response);
		while (matcher.find()) {
            /*
             * Capture groups:
             * GROUP                    EXAMPLE DATA
             * 1: Internal acc number?  0123456789
             * 2: Account name          Danske Direkt Bas
             * 3: Account number        01234567890
             * 4: Balance               1.124,56
             * 5: Balance (disp.)       1.124,56
             *
             */
		    String name = Html.fromHtml(matcher.group(2)).toString().trim();
		    Account account = new Account(name, Helpers.parseBalance(matcher.group(5)), matcher.group(1).trim());
		    if (name.contains("lån") || name.contains("Lån")) {
		        account.setType(Account.LOANS);
		    }
		    else {
	             balance = balance.add(Helpers.parseBalance(matcher.group(5)));
		    }
			accounts.add(account);
		}

		if (accounts.isEmpty()) {
			throw new BankException(res.getText(R.string.no_accounts_found).toString());
		}
		super.updateComplete();
	}

	@Override
	public void updateTransactions(Account account, Urllib urlopen) throws LoginException,
            BankException, IOException {
		super.updateTransactions(account, urlopen);

		//No transaction history for loans, funds and credit cards.
		int accType = account.getType();
		if (accType == Account.LOANS || accType == Account.FUNDS || accType == Account.CCARD) return;

		response = urlopen.open(String.format("https://mobil.danskebank.se/XI?WP=XAS&WAFT=%s&WSES=%s&WO=Konto&WA=KBList&WCI=%s", mPersonnr, mSessionId, account.getId()));
		Matcher matcher = reTransactions.matcher(response);
		ArrayList<Transaction> transactions = new ArrayList<Transaction>();
		while (matcher.find()) {
            /*
             * Capture groups:
             * GROUP                    EXAMPLE DATA
             * 1: Transaction           Till Fondsparande
             * 2: Date                  2011-07-28
             * 3: Amount                ?
             * 4: Status                Väntar | Utförd
             *
             */
			transactions.add(new Transaction(Html.fromHtml(matcher.group(2)).toString().trim(), Html.fromHtml(matcher.group(1)).toString().trim(), Helpers.parseBalance(matcher.group(3))));
		}
		account.setTransactions(transactions);
	}	
}
