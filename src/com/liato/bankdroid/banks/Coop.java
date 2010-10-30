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
import android.util.Log;

import com.liato.bankdroid.Account;
import com.liato.bankdroid.Bank;
import com.liato.bankdroid.BankException;
import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.LoginException;
import com.liato.bankdroid.R;
import com.liato.bankdroid.Transaction;
import com.liato.urllib.Urllib;

public class Coop extends Bank {
	private static final String TAG = "Coop";
	private static final String NAME = "Coop";
	private static final String NAME_SHORT = "coop";
	private static final String URL = "https://www.coop.se/mina-sidor/oversikt/";
	private static final int BANKTYPE_ID = Bank.COOP;

	private Pattern reViewState = Pattern.compile("__VIEWSTATE\"\\s+value=\"([^\"]+)\"");
	private Pattern reBalanceVisa = Pattern.compile("aktuellt\\s*saldo:</span>\\s*<span>([^<]+)<", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private Pattern reBalanceKonto = Pattern.compile("Aktuellt\\s*saldo:</span>[^>]*>([^<]+)<", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private Pattern reTransactionsKonto = Pattern.compile("<td>(\\d{4}-\\d{2}-\\d{2})</td>\\s*<td>([^<]+)</td>\\s*<td>[^<]*</td>\\s*<td>([^<]*)</td>\\s*<td[^>]*>([^<]+)</td>", Pattern.CASE_INSENSITIVE);
	private Pattern reTransactionsVisa = Pattern.compile("<td>(\\d{4}-\\d{2}-\\d{2})</td>\\s*<td>([^<]+)</td>\\s*<td>([^<]*)</td>\\s*<td>([^<]*)</td>\\s*<td.*?</td>\\s*<td><s.*?value=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
	
	public Coop(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = URL;
	}

	public Coop(String username, String password, Context context) throws BankException, LoginException {
		this(context);
		this.update(username, password);
	}

	@Override
	public Urllib login() throws LoginException, BankException {
		urlopen = new Urllib();
		String response = null;
		Matcher matcher;
		try {
			response = urlopen.open("https://www.coop.se/Mina-sidor/Oversikt/");
			matcher = reViewState.matcher(response);
			if (!matcher.find()) {
				throw new BankException(res.getText(R.string.unable_to_find).toString()+" viewstate.");
			}
			String strViewState = matcher.group(1);
			List <NameValuePair> postData = new ArrayList <NameValuePair>();
			postData.add(new BasicNameValuePair("ctl00$ContentPlaceHolderTodo$ContentPlaceHolderMainPageContainer$ContentPlaceHolderMainPageWithNavigationAndGlobalTeaser$ContentPlaceHolderPreContent$RegisterMediumUserForm$TextBoxUserName", username));
			postData.add(new BasicNameValuePair("ctl00$ContentPlaceHolderTodo$ContentPlaceHolderMainPageContainer$ContentPlaceHolderMainPageWithNavigationAndGlobalTeaser$ContentPlaceHolderPreContent$RegisterMediumUserForm$TextBoxPassword", password));
			postData.add(new BasicNameValuePair("ctl00$ContentPlaceHolderTodo$ContentPlaceHolderMainPageContainer$ContentPlaceHolderMainPageWithNavigationAndGlobalTeaser$ContentPlaceHolderPreContent$RegisterMediumUserForm$ButtonLogin", "Logga in"));
			postData.add(new BasicNameValuePair("__VIEWSTATE", strViewState));
			postData.add(new BasicNameValuePair("__EVENTTARGET", ""));
			postData.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
			response = urlopen.open("https://www.coop.se/Mina-sidor/Oversikt/", postData);
			Log.d(TAG, urlopen.getCurrentURI());
			if (response.contains("Felmeddelande")) {
				throw new LoginException(res.getText(R.string.invalid_username_password).toString());
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
			Account account;
			response = urlopen.open("https://www.coop.se/Mina-sidor/Oversikt/Kontoutdrag-MedMera-Visa/");
			matcher = reBalanceVisa.matcher(response);
			if (matcher.find()) {
				account = new Account("MedMera Visa", Helpers.parseBalance(matcher.group(1).trim()), "1");
				balance = balance.add(Helpers.parseBalance(matcher.group(1)));
				matcher = reTransactionsVisa.matcher(response);
				ArrayList<Transaction> transactions = new ArrayList<Transaction>();
				while (matcher.find()) {
					String title = matcher.group(4).length() > 0 ? matcher.group(4).trim() + " (" + matcher.group(3).trim() + ")" : matcher.group(2);
					transactions.add(new Transaction(matcher.group(1).trim(), Html.fromHtml(title).toString().trim(), Helpers.parseBalance(matcher.group(5))));
				}
				account.setTransactions(transactions);
				accounts.add(account);
			}
			response = urlopen.open("https://www.coop.se/Mina-sidor/Oversikt/Kontoutdrag-MedMera-Konto/");
			matcher = reBalanceKonto.matcher(response);
			if (matcher.find()) {
				account = new Account("MedMera Konto", Helpers.parseBalance(matcher.group(1).trim()), "2");
				balance = balance.add(Helpers.parseBalance(matcher.group(1)));
				matcher = reTransactionsKonto.matcher(response);
				ArrayList<Transaction> transactions = new ArrayList<Transaction>();
				while (matcher.find()) {
					String title = matcher.group(4).length() > 0 ? matcher.group(4) : matcher.group(3);
					transactions.add(new Transaction(matcher.group(1).trim(), Html.fromHtml(title).toString().trim(), Helpers.parseBalance(matcher.group(4))));
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
	}
}