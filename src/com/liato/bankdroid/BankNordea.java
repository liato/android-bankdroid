package com.liato.bankdroid;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.res.Resources;
import android.text.Html;
import com.liato.urllib.Urllib;

public class BankNordea implements Bank {

	private Context context;
	private Resources res;
	private String username;
	private String password;
	private Banks banktype = Banks.NORDEA;
	private Pattern reAccounts = Pattern.compile("account\\.html\\?id=konton:([^\"]+)\"[^>]+>\\s*<div[^>]+>([^<]+)<span[^>]+>([^<]+)</span", Pattern.CASE_INSENSITIVE );
	private Pattern reCSRF = Pattern.compile("csrf_token\".*?value=\"([^\"]+)\"");
	private ArrayList<Account> accounts = new ArrayList<Account>();
	private BigDecimal balance = new BigDecimal(0);

	public BankNordea() {
	}

	public BankNordea(String username, String password, Context context) throws BankException {
		this.update(username, password, context);
	}

	public void update(String username, String password, Context context) throws BankException {
		this.context = context;
		this.res = this.context.getResources();

		this.username = username;
		this.password = password;
		this.update();
	}
	public void update() throws BankException {
		if (username == null || password == null || username.length() == 0 || password.length() == 0) {
			throw new BankException(res.getText(R.string.invalid_username_password).toString());
		}
		Urllib urlopen = new Urllib();
		String response = null;
		Matcher matcher;
		try {
			response = urlopen.open("https://mobil.nordea.se/banking-nordea/nordea-c3/login.html");
			matcher = reCSRF.matcher(response);
			if (!matcher.find()) {
				throw new BankException(res.getText(R.string.unable_to_find).toString()+" CSRF token.");
			}
			String csrftoken = matcher.group(1);
			List <NameValuePair> postData = new ArrayList <NameValuePair>();
			postData.add(new BasicNameValuePair("xyz", username));
			postData.add(new BasicNameValuePair("zyx", password));
			postData.add(new BasicNameValuePair("_csrf_token", csrftoken));
			response = urlopen.open("https://mobil.nordea.se/banking-nordea/nordea-c3/login.html", postData);

			if (!response.contains("accounts.html")) {
				throw new BankException(res.getText(R.string.invalid_username_password).toString());
			}

			matcher = reAccounts.matcher(response);
			while (matcher.find()) {
				accounts.add(new Account(Html.fromHtml(matcher.group(2)).toString().trim(), Helpers.parseBalance(matcher.group(3)), matcher.group(1).trim()));
				balance = balance.add(Helpers.parseBalance(matcher.group(3)));
			}
			if (accounts.isEmpty()) {
				throw new BankException(res.getText(R.string.no_accounts_found).toString());
			}
			// Konungens konto
			//accounts.add(new Account("Personkonto", new BigDecimal("568268.37"), "1"));
			//accounts.add(new Account("Kapitalkonto", new BigDecimal("5789002.00"), "0"));
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
	public ArrayList<Account> getAccounts() {
		return this.accounts;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public Banks getType() {
		return banktype;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public BigDecimal getBalance() {
		return balance;
	}	
}