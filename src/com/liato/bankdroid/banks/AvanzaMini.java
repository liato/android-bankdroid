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
import com.liato.urllib.Urllib;

public class AvanzaMini extends Bank {
	private static final String TAG = "AvanzaMini";
	private static final String NAME = "Avanza Mini";
	private static final String NAME_SHORT = "avanzamini";
	private static final String URL = "https://www.avanza.se/mini/hem/";
	private static final int BANKTYPE_ID = Bank.AVANZAMINI;
	
    private Pattern reAvanzaMini = Pattern.compile("w100\\s+azatable\"[^>]+>\\s*<tbody>\\s*(.*)</tbody>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private Pattern accountsAvanzaMini = Pattern.compile("<tr>\\s*<td>([^<]+)</td>\\s*<td\\s+class=\"tright\">([^<]+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	public AvanzaMini(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = URL;
	}

	public AvanzaMini(String username, String password, Context context) throws BankException, LoginException {
		this(context);
		this.update(username, password);
	}

	@Override
	public Urllib login() throws LoginException, BankException {
		urlopen = new Urllib(true, true);
		String response = null;
		try {
			List <NameValuePair> postData = new ArrayList <NameValuePair>();
			postData.add(new BasicNameValuePair("username", username));
			postData.add(new BasicNameValuePair("password", password));

			Log.d("BankAvanza", "Posting to https://www.avanza.se/aza/login/login.jsp");
			response = urlopen.open("https://www.avanza.se/aza/login/login.jsp", postData);
			Log.d("BankAvanza", "Url after post: "+urlopen.getCurrentURI());
			if (response.contains("Felaktigt") && !response.contains("Logga ut")) {
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
		String response = null;
		Matcher matcher;
		try {
			Log.d("BankAvanza", "Opening: https://www.avanza.se/mini/mitt_konto/index.html");
			response = urlopen.open("https://www.avanza.se/mini/mitt_konto/index.html");
			matcher = reAvanzaMini.matcher(response);
	        if (matcher.find()) {
	            int count = 1;
	            Matcher submatcher = accountsAvanzaMini.matcher(matcher.group(1));
	            while (submatcher.find()){
	                accounts.add(new Account(Html.fromHtml(submatcher.group(1)).toString().trim(), Helpers.parseBalance(submatcher.group(2)), Integer.toString(count)));
	                balance = balance.add(Helpers.parseBalance(submatcher.group(2)));
	                count++;
	            }
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