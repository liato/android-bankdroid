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

public class Statoil extends Bank {
	private static final String TAG = "Statoil";
	private static final String NAME = "Statoil";
	private static final String NAME_SHORT = "statoil";
	private static final String URL = "https://applications.sebkort.com/nis/external/stse/login.do";
	private static final int BANKTYPE_ID = Bank.STATOIL;
    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;
    private static final String INPUT_HINT_USERNAME = "ÅÅMMDDXXXX";

	private Pattern reAccounts = Pattern.compile("Welcomepagebillingunitlastdisposableamount\">([^<]+)</div>", Pattern.CASE_INSENSITIVE);
	private Pattern reTransactions = Pattern.compile("transcol1\">\\s*<span>([^<]+)</span>\\s*</td>\\s*<td[^>]+>\\s*<span>([^<]+)</span>\\s*</td>\\s*<td[^>]+>\\s*<div[^>]+>\\s*<span>([^<]*)</span>\\s*</div>\\s*</td>\\s*<td[^>]+>\\s*<span>([^<]*)</span>\\s*</td>\\s*<td[^>]+>\\s*<span>([^>]*)</span>\\s*</td>\\s*<td[^>]+>\\s*<span>([^<]*)</span>\\s*</td>\\s*<td[^>]+>\\s*<span>([^<]+)</span>", Pattern.CASE_INSENSITIVE);
	private String response = null;
	public Statoil(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = URL;
		super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
		super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
	}

	public Statoil(String username, String password, Context context) throws BankException, LoginException {
		this(context);
		this.update(username, password);
	}

	@Override
	public Urllib login() throws LoginException, BankException {
		urlopen = new Urllib(true);
		try {
			List <NameValuePair> postData = new ArrayList <NameValuePair>();
			response = urlopen.open("https://applications.sebkort.com/nis/external/stse/login.do");
			List<NameValuePair> parameters = new ArrayList<NameValuePair>(3);
            parameters.add(new BasicNameValuePair("USERNAME", "0122"+username.toUpperCase()));
            parameters.add(new BasicNameValuePair("referer", "login.jsp"));
            response = urlopen.open("https://applications.sebkort.com/nis/external/hidden.jsp", postData);
            
			postData.clear();
			postData.add(new BasicNameValuePair("choice", "PWD"));
			postData.add(new BasicNameValuePair("uname", username.toUpperCase()));
			postData.add(new BasicNameValuePair("PASSWORD", password));
			postData.add(new BasicNameValuePair("target", "/nis/stse/main.do"));
			postData.add(new BasicNameValuePair("prodgroup", "0122"));
			postData.add(new BasicNameValuePair("USERNAME", "0122"+username.toUpperCase()));
			postData.add(new BasicNameValuePair("METHOD", "LOGIN"));
			postData.add(new BasicNameValuePair("CURRENT_METHOD", "PWD"));
			response = urlopen.open("https://applications.sebkort.com/siteminderagent/forms/generic.fcc", postData);
			if (response.contains("elaktig kombination")) {
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
		Matcher matcher;
		try {
			if (!"https://applications.sebkort.com/nis/stse/main.do".equals(urlopen.getCurrentURI())) {
				response = urlopen.open("https://applications.sebkort.com/nis/stse/main.do");
			}
			matcher = reAccounts.matcher(response);
			if (matcher.find()) {
				/*
				 * Capture groups:
				 * GROUP				EXAMPLE DATA
				 * 1: amount			10 579,43
				 * 
				 */
			    Account account = new Account("Statoil MasterCard" , Helpers.parseBalance(matcher.group(1)), "1");
			    account.setType(Account.CCARD);
				accounts.add(account);
				balance = balance.add(Helpers.parseBalance(matcher.group(1)));
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
		if (!urlopen.acceptsInvalidCertificates()) { //Should never happen, but we'll check it anyway.
			urlopen = login();
		}
		String response = null;
		Matcher matcher;
		try {
			Log.d(TAG, "Opening: https://applications.sebkort.com/nis/stse/getPendingTransactions.do");
			response = urlopen.open("https://applications.sebkort.com/nis/stse/getPendingTransactions.do");
			matcher = reTransactions.matcher(response);
			ArrayList<Transaction> transactions = new ArrayList<Transaction>();
			Calendar cal = Calendar.getInstance();
			while (matcher.find()) {
				/*
				 * Capture groups:
				 * GROUP				EXAMPLE DATA
				 * 1: Date				10-18
				 * 2: Date				10-19
				 * 3: Specification		ICA Kvantum
				 * 4: Location			Stockholm
				 * 5: Currency			always empty?
				 * 6: Amount			always empty?
				 * 7: Amount in sek		5791,18
				 * 
				 */				
				transactions.add(new Transaction(""+cal.get(Calendar.YEAR)+"-"+matcher.group(1).trim(), Html.fromHtml(matcher.group(3)).toString().trim()+(matcher.group(4).trim().length() > 0 ? " ("+Html.fromHtml(matcher.group(4)).toString().trim()+")" : ""), Helpers.parseBalance(matcher.group(7)).negate()));
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
