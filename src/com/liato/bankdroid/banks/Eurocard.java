package com.liato.bankdroid.banks;

import java.io.IOException;
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

import com.liato.bankdroid.Account;
import com.liato.bankdroid.Bank;
import com.liato.bankdroid.BankException;
import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.LoginException;
import com.liato.bankdroid.R;
import com.liato.bankdroid.Transaction;
import com.liato.urllib.Urllib;

public class Eurocard extends Bank {
	private static final String TAG = "Eurocard";
	private static final String NAME = "Eurocard";
	private static final String NAME_SHORT = "eurocard";
	private static final String URL = "https://e-saldo.eurocard.se/nis/external/ecse/login.do";
	private static final int BANKTYPE_ID = Bank.EUROCARD;
	private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;
	
	private Pattern reAccounts = Pattern.compile("getInvoiceList\\.do\\?id=([^\"]+)\">([^<]+)</a></td>(?:\\s*<td>[^<]+</td>){2}\\s*<td\\s*align=\"right\">([^<]+)<", Pattern.CASE_INSENSITIVE);
	private Pattern reTransactions = Pattern.compile("<nobr>(\\d\\d-\\d\\d)</nobr>\\s*</td>\\s*<td\\s*valign=\"top\">\\s*<nobr>[^<]+</nobr>\\s*</td>\\s*<td><div\\s*class=\"BreakLine\">([^<]+)</div>\\s*</td>\\s*<td\\s*valign=\"top\">\\s*<nobr>([^<]*)</nobr>\\s*</td>\\s*<td\\s*valign=\"top\">\\s*<nobr>([^<]*)</nobr>\\s*</td>\\s*<td[^>]+>\\s*<nobr>([^>]*)</nobr>\\s*</td>\\s*<td\\s*valign=\"top\">[^<]+</td>\\s*<td[^>]+>\\s*<nobr>([^<]+)</nobr>", Pattern.CASE_INSENSITIVE);
	private String response = null;
	public Eurocard(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = URL;
		super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
	}

	public Eurocard(String username, String password, Context context) throws BankException, LoginException {
		this(context);
		this.update(username, password);
	}

	@Override
	public Urllib login() throws LoginException, BankException {
		urlopen = new Urllib(true);
		try {
			List <NameValuePair> postData = new ArrayList <NameValuePair>();
			postData.add(new BasicNameValuePair("target", "/nis/ecse/main.do"));				
			postData.add(new BasicNameValuePair("prodgroup", "0005"));				
			postData.add(new BasicNameValuePair("USERNAME", "0005"+username));				
			postData.add(new BasicNameValuePair("METHOD", "LOGIN"));				
			postData.add(new BasicNameValuePair("CURRENT_METHOD", "PWD"));				
			postData.add(new BasicNameValuePair("uname", username));
			postData.add(new BasicNameValuePair("PASSWORD", password));
			
			Log.d(TAG, "Posting to https://e-saldo.eurocard.se/siteminderagent/forms/generic.fcc");
			response = urlopen.open("https://e-saldo.eurocard.se/siteminderagent/forms/generic.fcc", postData);
			Log.d(TAG, "Url after post: "+urlopen.getCurrentURI());
			
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
		while (matcher.find()) {
			accounts.add(new Account(Html.fromHtml(matcher.group(2)).toString().trim(), Helpers.parseBalance(matcher.group(3)), matcher.group(1).trim()));
			balance = balance.add(Helpers.parseBalance(matcher.group(3)));
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
		try {
			Log.d(TAG, "Opening: https://e-saldo.eurocard.se/nis/ecse/getPendingTransactions.do");
			response = urlopen.open("https://e-saldo.eurocard.se/nis/ecse/getPendingTransactions.do");
			matcher = reTransactions.matcher(response);
			ArrayList<Transaction> transactions = new ArrayList<Transaction>();
			String strDate = null;
			Calendar cal = Calendar.getInstance();
			while (matcher.find()) {
				/*
				 * Capture groups:
				 * GROUP				EXAMPLE DATA
				 * 1: date		 		09-26
				 * 2: specification		ICA Kvantum
				 * 3: location          Stockholm
				 * 4: currency			SEK or empty
				 * 5: tax				12.99 or empty
				 * 6: amount			118.65
				 * 
				 */
				strDate = ""+cal.get(Calendar.YEAR)+"-"+Html.fromHtml(matcher.group(1)).toString().trim();
				transactions.add(new Transaction(strDate, Html.fromHtml(matcher.group(2)).toString().trim()+(matcher.group(3).trim().length() > 0 ? " ("+Html.fromHtml(matcher.group(3)).toString().trim()+")" : ""), Helpers.parseBalance(matcher.group(6)).negate()));
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