package com.liato.bankdroid.banks;

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

import com.liato.bankdroid.Account;
import com.liato.bankdroid.Bank;
import com.liato.bankdroid.BankException;
import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.LoginException;
import com.liato.bankdroid.R;
import com.liato.bankdroid.Transaction;
import com.liato.urllib.Urllib;

public class FirstCard extends Bank {
	private static final String TAG = "FirstCard";
	private static final String NAME = "First Card";
	private static final String NAME_SHORT = "firstcard";
	private static final String URL = "https://e-saldo.eurocard.se/nis/external/ecse/login.do";
	private static final int BANKTYPE_ID = Bank.FIRSTCARD;
    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;
    private static final String INPUT_HINT_USERNAME = "ÅÅMMDDXXXX";
	
	private Pattern reAccounts = Pattern.compile("translist\\.jsp\\?p=a&(?:amp;)?cardID=([^\"]+)\">([^<]+)</a>\\s*</td>\\s*<td[^>]+>([^<]+)</td>", Pattern.CASE_INSENSITIVE);
	private Pattern reTransactions = Pattern.compile("pagecolumns\">(\\d{6})</td>\\s*<td>\\s*</td>\\s*<td>([^<]+)</td>\\s*<td[^>]+>([^<]+)</td>\\s*<td[^>]+>([^<]+)</td>\\s*<td[^>]+>([^<]+)<", Pattern.CASE_INSENSITIVE);
	private String response = null;
	public FirstCard(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = URL;
		super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
		super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
	}

	public FirstCard(String username, String password, Context context) throws BankException, LoginException {
		this(context);
		this.update(username, password);
	}

	@Override
	public Urllib login() throws LoginException, BankException {
		urlopen = new Urllib(true);
		try {
			List <NameValuePair> postData = new ArrayList <NameValuePair>();
			postData.add(new BasicNameValuePair("op", "login"));				
			postData.add(new BasicNameValuePair("searchIndex", ""));				
			postData.add(new BasicNameValuePair("country", "0"));				
			postData.add(new BasicNameValuePair("soktext", "Skriv sökord här"));				
			postData.add(new BasicNameValuePair("pnr", username));
			postData.add(new BasicNameValuePair("intpwd", password));
			
			Log.d(TAG, "Posting to https://www.firstcard.se/valkom.jsp");
			response = urlopen.open("https://www.firstcard.se/valkom.jsp", postData);
			Log.d(TAG, "Url after post: "+urlopen.getCurrentURI());
			
			if (response.contains("felaktig identitet") || response.contains("obligatoriskt") || response.contains("ange en internetkod")) {
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
		try {
			response = urlopen.open("https://www.firstcard.se/mkol/index.jsp");
			Matcher matcher = reAccounts.matcher(response);
			while (matcher.find()) {
				/*
				 * Capture groups:
				 * GROUP				EXAMPLE DATA
				 * 1: id				kdKPq4ghlcy9wpXymSzzS46wWQcS_0OT
				 * 2: account number	1111 3333 7777 9999
				 * 3: amount 			9 824,08
				 * 
				 */				
				accounts.add(new Account(Html.fromHtml(matcher.group(2)).toString().trim(), Helpers.parseBalance(matcher.group(3)), matcher.group(1).trim()));
				balance = balance.add(Helpers.parseBalance(matcher.group(3)));
			}

			if (accounts.isEmpty()) {
				throw new BankException(res.getText(R.string.no_accounts_found).toString());
			}			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        finally {
            super.updateComplete();
        }
	}

	@Override
	public void updateTransactions(Account account, Urllib urlopen) throws LoginException, BankException {
		super.updateTransactions(account, urlopen);
		Matcher matcher;
		try {
			Log.d(TAG, "Opening: https://www.firstcard.se/mkol/translist.jsp?p=a&cardID="+account.getId());
			response = urlopen.open("https://www.firstcard.se/mkol/translist.jsp?p=a&cardID="+account.getId());
			matcher = reTransactions.matcher(response);
			ArrayList<Transaction> transactions = new ArrayList<Transaction>();
			while (matcher.find()) {
				/*
				 * Capture groups:
				 * GROUP						EXAMPLE DATA
				 * 1: date						101006
				 * 2: specification				GOOGLE *RealArcade
				 * 3: currency					USD
				 * 4: amount					3,49
				 * 5: amount in local currency	24,08
				 * 
				 */
				String strDate = Html.fromHtml(matcher.group(1)).toString().trim();
				strDate = "20"+strDate.charAt(0)+strDate.charAt(1)+"-"+strDate.charAt(2)+strDate.charAt(3)+"-"+strDate.charAt(4)+strDate.charAt(5);
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