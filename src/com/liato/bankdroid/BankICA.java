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
import android.util.Log;

import com.liato.urllib.Urllib;

public class BankICA implements Bank {

	private Context context;
	private Resources res;
	private String username;
	private String password;
	private Banks banktype = Banks.ICA;
	private Pattern reEventValidation = Pattern.compile("__EVENTVALIDATION\"\\s+value=\"([^\"]+)\"");
	private Pattern reViewState = Pattern.compile("__VIEWSTATE\"\\s+value=\"([^\"]+)\"");
	private Pattern reError = Pattern.compile("<label\\s+class=\"error\">(.+?)</label>",Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private Pattern reBalanceDisp = Pattern.compile("account\\.aspx\\?id=([^\"]+).+?>([^<]+)</a.+?Disponibelt([0-9 .,-]+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private Pattern reBalanceSald = Pattern.compile("account\\.aspx\\?id=([^\"]+).+?>([^<]+)</a[^D]*Saldo([0-9 .,-]+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private ArrayList<Account> accounts = new ArrayList<Account>();
	private BigDecimal balance = new BigDecimal(0);

	public BankICA() {
	}

	public BankICA(String username, String password, Context context) throws BankException {
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
			response = urlopen.open("https://mobil.icabanken.se/login/login.aspx");
			matcher = reViewState.matcher(response);
			if (!matcher.find()) {
				throw new BankException(res.getText(R.string.unable_to_find).toString()+" viewstate.");
			}
			String strViewState = matcher.group(1);
			matcher = reEventValidation.matcher(response);
			if (!matcher.find()) {
				throw new BankException(res.getText(R.string.unable_to_find).toString()+" eventvalidation.");
			}
			String strEventValidation = matcher.group(1);
			List <NameValuePair> postData = new ArrayList <NameValuePair>();
			postData.add(new BasicNameValuePair("pnr_phone", username));
			postData.add(new BasicNameValuePair("pwd_phone", password));
			postData.add(new BasicNameValuePair("btnLogin", "Logga in"));
			postData.add(new BasicNameValuePair("__VIEWSTATE", strViewState));
			postData.add(new BasicNameValuePair("__EVENTVALIDATION", strEventValidation));
			response = urlopen.open("https://mobil.icabanken.se/login/login.aspx", postData);
			Log.d("BankICA", urlopen.getCurrentURI());
			matcher = reError.matcher(response);
			if (matcher.find()) {
				throw new BankException(Html.fromHtml(matcher.group(1).trim()).toString());
			}
			response = urlopen.open("https://mobil.icabanken.se/account/overview.aspx");
			Log.d("BankICA", urlopen.getCurrentURI());
			//response = urlopen.open("http://x.x00.us/android/bankdroid/icabanken_oversikt.htm");
			matcher = reBalanceSald.matcher(response);
			while (matcher.find()) {
				accounts.add(new Account(Html.fromHtml(matcher.group(2)).toString().trim(), Helpers.parseBalance(matcher.group(3).trim()), matcher.group(1).trim()));
				balance = balance.add(Helpers.parseBalance(matcher.group(3)));
			}
			matcher = reBalanceDisp.matcher(response);
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