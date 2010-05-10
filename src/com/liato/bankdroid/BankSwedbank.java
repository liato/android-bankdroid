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

import android.text.Html;
import com.liato.urllib.Urllib;

public class BankSwedbank implements Bank {

	private String username;
	private String password;
	private Banks banktype = Banks.SWEDBANK;
	private Pattern reCSRF = Pattern.compile("csrf_token\"\\s*value=\"([^\"]+)\"");
	private Pattern reAccounts = Pattern.compile("account\\.html\\?id=([^\"]+)\">\\s*<span.*?/span>([^<]+) <.*?secondary\">([0-9 .,-]+)</span");
	private ArrayList<Account> accounts = new ArrayList<Account>();
	private BigDecimal balance = new BigDecimal(0);

	public BankSwedbank() {

	}

	public BankSwedbank(String username, String password) throws BankException {
		this.update(username, password);
	}

	public void update(String username, String password) throws BankException {
		this.username = username;
		this.password = password;
		this.update();
	}

	public void update() throws BankException {
		if (username == null || password == null || username.length() == 0 || password.length() == 0) {
			throw new BankException("Personnummer och l�senord st�mmer ej.");
		}        Urllib urlopen = new Urllib();
		String response = null;
		Matcher matcher;
		try {
			response = urlopen.open("https://mobilbank.swedbank.se/banking/swedbank-light/login.html");
			matcher = reCSRF.matcher(response);
			if (!matcher.find()) {
				throw new BankException("Could not connect. CSRF token was not found.");
			}
			String csrftoken = matcher.group(1);
			List <NameValuePair> postData = new ArrayList <NameValuePair>();
			postData.add(new BasicNameValuePair("xyz", username));
			postData.add(new BasicNameValuePair("zyx", password));
			postData.add(new BasicNameValuePair("_csrf_token", csrftoken));
			response = urlopen.open("https://mobilbank.swedbank.se/banking/swedbank/login.html", postData);

			if (response.contains("misslyckats")) {
				throw new BankException("Personnummer och l�senord st�mmer ej.");
			}
			response = urlopen.open("https://mobilbank.swedbank.se/banking/swedbank-light/accounts.html");
			matcher = reAccounts.matcher(response);
			while (matcher.find()) {
				accounts.add(new Account(Html.fromHtml(matcher.group(2)).toString(), Helpers.parseBalance(matcher.group(3)), matcher.group(1).trim()));
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
