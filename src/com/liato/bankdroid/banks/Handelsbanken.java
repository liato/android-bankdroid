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

public class Handelsbanken extends Bank {
	private static final String TAG = "Handelsbanken";
	private static final String NAME = "Handelsbanken";
	private static final String NAME_SHORT = "handelsbanken";
	private static final String URL = "https://m.handelsbanken.se/";
	private static final int BANKTYPE_ID = Bank.HANDELSBANKEN;

	private Pattern reBalance = Pattern.compile("block-link\\s*\"\\s*href=\"/primary/_-([^\"]+)\"><span>([^<]+)</span>.*?SEK([0-9\\s.,-ÃÂ]+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private Pattern reAccountsUrl = Pattern.compile("_-([^\"]+)\"><img[^>]+><span[^>]+>Konton<",Pattern.CASE_INSENSITIVE);
	private Pattern reTransactions = Pattern.compile("padding-left\">([^<]+)</span><span[^>]*><span[^>]*>([^<]+)</span><span[^>]*>([^<]+)<", Pattern.CASE_INSENSITIVE);
	public Handelsbanken(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = URL;
	}

	public Handelsbanken(String username, String password, Context context) throws BankException, LoginException {
		this(context);
		this.update(username, password);
	}

	
	@Override
	public Urllib login() throws LoginException, BankException {
		Urllib urlopen = new Urllib();
		String response = null;
		try {
			//Let the website set som cookies
			response = urlopen.open("https://m.handelsbanken.se/primary/_-iseufea5");

			List <NameValuePair> postData = new ArrayList <NameValuePair>();
			postData.add(new BasicNameValuePair("username", username));
			postData.add(new BasicNameValuePair("pin", password));
			postData.add(new BasicNameValuePair("execute", "true"));
			Log.d(TAG, "Posting data to: " + "https://m.handelsbanken.se/primary/_-iseufea5");
			response = urlopen.open("https://m.handelsbanken.se/primary/_-iseufea5", postData);

			if (response.contains("ontrollera dina uppgifter")) {
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
			response = urlopen.open("https://m.handelsbanken.se/primary/_-iseufea5");
			//Successful login, find accounts url and retrieve account info.
			matcher = reAccountsUrl.matcher(response);
			if (!matcher.find()) {
				throw new BankException(res.getText(R.string.unable_to_find).toString()+" accounts url.");
			}
			String strAccountsUrl = "https://m.handelsbanken.se/primary/_-"+matcher.group(1);			
			Log.d("TAG", "Accounts url: "+strAccountsUrl);
			response = urlopen.open(strAccountsUrl);
			matcher = reBalance.matcher(response);
			while (matcher.find()) {
				accounts.add(new Account(Html.fromHtml(matcher.group(2)).toString().trim(), Helpers.parseBalance(matcher.group(3).trim()), matcher.group(1).trim()));
				balance = balance.add(Helpers.parseBalance(matcher.group(3)));
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
			urlopen.close();
		}

	}
	
	@Override
	public void updateTransactions(Account account, Urllib urlopened) throws LoginException, BankException {
		super.updateTransactions(account, urlopened);
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
			Log.d(TAG, "Opening: https://m.handelsbanken.se/primary/_-"+account.getId());
			response = urlopen.open("https://m.handelsbanken.se/primary/_-"+account.getId());
			matcher = reTransactions.matcher(response);
			ArrayList<Transaction> transactions = new ArrayList<Transaction>();
			while (matcher.find()) {
				transactions.add(new Transaction(matcher.group(1).trim(), Html.fromHtml(matcher.group(2)).toString().trim(), Helpers.parseBalance(matcher.group(3))));
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