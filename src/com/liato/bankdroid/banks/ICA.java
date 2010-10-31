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

import com.liato.bankdroid.Account;
import com.liato.bankdroid.Bank;
import com.liato.bankdroid.BankException;
import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.LoginException;
import com.liato.bankdroid.R;
import com.liato.bankdroid.Transaction;
import com.liato.urllib.Urllib;

public class ICA extends Bank {
	private static final String TAG = "ICA";
	private static final String NAME = "ICA";
	private static final String NAME_SHORT = "ica";
	private static final String URL = "http://mobil.ica.se/";
	private static final int BANKTYPE_ID = Bank.ICA;

	private Pattern reAccounts = Pattern.compile("lblAvaibleAmount\">([^<]+)<", Pattern.CASE_INSENSITIVE);
	private Pattern reTransactions = Pattern.compile("<td>\\s*(\\d{4}-\\d{2}-\\d{2})\\s*</td>\\s*<td>\\s*([^<]+).*?amount\">([^<]+)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	private Pattern reViewState = Pattern.compile("__VIEWSTATE\"\\s+value=\"([^\"]+)\"");
	private Pattern reLoginError = Pattern.compile("login-error[^>]+>(.+?)<");
	public ICA(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = URL;
	}

	public ICA(String username, String password, Context context) throws BankException, LoginException {
		this(context);
		this.update(username, password);
	}

	@Override
	public Urllib login() throws LoginException, BankException {
		urlopen = new Urllib();
		String response = null;
		Matcher matcher;
		try {
			response = urlopen.open("https://www.ica.se/Logga-in/");
			matcher = reViewState.matcher(response);
			if (!matcher.find()) {
				throw new BankException(res.getText(R.string.unable_to_find).toString()+" viewstate.");
			}
			String strViewState = matcher.group(1);
			List <NameValuePair> postData = new ArrayList <NameValuePair>();
			postData.add(new BasicNameValuePair("__EVENTTARGET", ""));
			postData.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
			postData.add(new BasicNameValuePair("__LASTFOCUS", ""));
			postData.add(new BasicNameValuePair("__VIEWSTATE", strViewState));
			postData.add(new BasicNameValuePair("ctl00$fakie", ""));
			postData.add(new BasicNameValuePair("q", "Sök"));
			postData.add(new BasicNameValuePair("appendUrlString", ""));
			postData.add(new BasicNameValuePair("ctl00$cphFullWidthContainer$ctl02$btnLogin", "Logga in"));
			postData.add(new BasicNameValuePair("ctl00$cphFullWidthContainer$ctl02$chbRememberMe", "on"));
			postData.add(new BasicNameValuePair("footer-q", "Sök"));
			postData.add(new BasicNameValuePair("ctl00$cphFullWidthContainer$ctl02$tbPersno", username));
			postData.add(new BasicNameValuePair("ctl00$cphFullWidthContainer$ctl02$tbPasswd", password));
			
			response = urlopen.open("https://www.ica.se/Logga-in/", postData);
			
			matcher = reLoginError.matcher(response);
			if (matcher.find()) {
				throw new LoginException(Html.fromHtml(matcher.group(1)).toString().trim());
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
	public void update() throws BankException, LoginException {
		super.update();
		if (username == null || password == null || username.length() == 0 || password.length() == 0) {
			throw new LoginException(res.getText(R.string.invalid_username_password).toString());
		}
		urlopen = login();
		String response = null;
		Matcher matcher;
		try {
			response = urlopen.open("https://www.ica.se/Mina-sidor/Konto--Saldo/");
			matcher = reAccounts.matcher(response);
			if (matcher.find()) {
				Account account = new Account("ICA Kort", Helpers.parseBalance(matcher.group(1)), "1");
				balance = balance.add(Helpers.parseBalance(matcher.group(1)));
				matcher = reTransactions.matcher(response);
				ArrayList<Transaction> transactions = new ArrayList<Transaction>();
				while (matcher.find()) {
					transactions.add(new Transaction(matcher.group(1).trim(), Html.fromHtml(matcher.group(2)).toString().trim(), Helpers.parseBalance(matcher.group(3))));
				}
				account.setTransactions(transactions);
				accounts.add(account);
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
