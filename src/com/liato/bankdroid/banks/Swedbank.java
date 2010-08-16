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

public class Swedbank extends Bank {
	private static final String TAG = "Swedbank";
	private static final String NAME = "Swedbank";
	private static final String NAME_SHORT = "swedbank";
	private static final String URL = "https://mobilbank.swedbank.se/";
	private static final int BANKTYPE_ID = Bank.SWEDBANK;

	private Pattern reCSRF = Pattern.compile("csrf_token\".*?value=\"([^\"]+)\"");
	private Pattern reAccounts = Pattern.compile("(account|loan)\\.html\\?id=([^\"]+)\">\\s*(?:<span.*?/span>)?([^<]+)<.*?secondary\">([^<]+)</span");
	private Pattern reTransactions = Pattern.compile("trans-date\">([^<]+)</div>.*?trans-subject\">([^<]+)</div>.*?trans-amount\">([^<]+)</div>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	public Swedbank(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = URL;
	}

	public Swedbank(String username, String password, Context context) throws BankException, LoginException {
		this(context);
		this.update(username, password);
	}

	@Override
	public Urllib login() throws LoginException, BankException {
		Urllib urlopen = new Urllib();
		String response = null;
		Matcher matcher;
		try {
			response = urlopen.open("https://mobilbank.swedbank.se/banking/swedbank/login.html");
			matcher = reCSRF.matcher(response);
			if (!matcher.find()) {
				throw new BankException(res.getText(R.string.unable_to_find).toString()+" CSRF token.");
			}
			String csrftoken = matcher.group(1);
			List <NameValuePair> postData = new ArrayList <NameValuePair>();
			postData.add(new BasicNameValuePair("xyz", username));
			postData.add(new BasicNameValuePair("zyx", password));
			postData.add(new BasicNameValuePair("_csrf_token", csrftoken));
			response = urlopen.open("https://mobilbank.swedbank.se/banking/swedbank/login.html", postData);

			if (response.contains("misslyckats")) {
				throw new LoginException(res.getText(R.string.invalid_username_password).toString());
			}
		}
		catch (ClientProtocolException e) {
			throw new BankException(e.getMessage());
		}
		catch (IOException e) {
			throw new BankException(e.getMessage());
		}
		finally {
		}
		return urlopen;
	}

	@Override
	public void update() throws BankException, LoginException {
		super.update();
		if (username == null || password == null || username.length() == 0 || password.length() == 0) {
			throw new LoginException(res.getText(R.string.invalid_username_password).toString());
		}
		Urllib urlopen = login();
		String response = null;
		Matcher matcher;
		try {
			response = urlopen.open("https://mobilbank.swedbank.se/banking/swedbank/accounts.html");
			matcher = reAccounts.matcher(response);
			while (matcher.find()) {
				accounts.add(new Account(Html.fromHtml(matcher.group(3)).toString(), Helpers.parseBalance(matcher.group(4)), matcher.group(2).trim() == "loan" ? "l"+matcher.group(2).trim() : matcher.group(2).trim()));
				balance = balance.add(Helpers.parseBalance(matcher.group(4)));
			}
			if (accounts.isEmpty()) {
				throw new BankException(res.getText(R.string.no_accounts_found).toString());
			}
			// Konungens konto
			//accounts.add(new Account("Personkonto", new BigDecimal("85351"), "0"));
			//accounts.add(new Account("Sparkonto", new BigDecimal("8590700"), "1"));
		}
		catch (ClientProtocolException e) {
			throw new BankException(e.getMessage());
		}
		catch (IOException e) {
			throw new BankException(e.getMessage());
		}
		finally {
			urlopen.close();
		}

	}
	
	@Override
	public void updateTransactions(Account account, Urllib urlopened) throws LoginException, BankException {
		super.updateTransactions(account, urlopened);
		if (account.getId().startsWith("l")) return; //No transaction history for loans
		Urllib urlopen = null;
		if (urlopened == null) {
			urlopen = login();
		}
		else {
			urlopen = urlopened;
		}
		String response = null;
		Matcher matcher;
		try {
			Log.d(TAG, "Opening: https://mobilbank.swedbank.se/banking/swedbank/account.html?id="+account.getId());
			response = urlopen.open("https://mobilbank.swedbank.se/banking/swedbank/account.html?id="+account.getId());
			matcher = reTransactions.matcher(response);
			ArrayList<Transaction> transactions = new ArrayList<Transaction>();
			while (matcher.find()) {
				transactions.add(new Transaction("20"+matcher.group(1).trim(), Html.fromHtml(matcher.group(2)).toString().trim(), Helpers.parseBalance(matcher.group(3))));
			}
			account.setTransactions(transactions);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (urlopened == null) {
			urlopen.close();
		}
	}	
}
