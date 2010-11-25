package com.liato.bankdroid.banks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.liato.urllib.Urllib;

public class Lansforsakringar extends Bank {
	private static final String TAG = "Lansforsakringar";
	private static final String NAME = "Länsförsäkringar";
	private static final String NAME_SHORT = "lansforsakringar";
	private static final String URL = "https://secure246.lansforsakringar.se/lfportal/login/privat";
	private static final int BANKTYPE_ID = Bank.LANSFORSAKRINGAR;
    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;
    private static final int INPUT_TYPE_PASSWORD = InputType.TYPE_CLASS_PHONE;
    private static final String INPUT_HINT_USERNAME = "ÅÅMMDD-XXXX";

	private Pattern reEventValidation = Pattern.compile("__EVENTVALIDATION\"\\s+value=\"([^\"]+)\"");
	private Pattern reViewState = Pattern.compile("__VIEWSTATE\"\\s+value=\"([^\"]+)\"");
	private Pattern reBalance = Pattern.compile("AccountNumber=([0-9]+)[^>]+><span[^>]+>([^<]+)</.*?span></td.*?<span[^>]+>([0-9 .,-]+)</span", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private Pattern reToken = Pattern.compile("var\\s+token\\s*=\\s*'([^']+)'", Pattern.CASE_INSENSITIVE);
	private Pattern reUrl = Pattern.compile("<li class=\"bank\">\\s*<a href=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
	private String accountsUrl = null;
	public Lansforsakringar(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = URL;
		super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
		super.INPUT_TYPE_PASSWORD = INPUT_TYPE_PASSWORD;
		super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
	}

	public Lansforsakringar(String username, String password, Context context) throws BankException, LoginException {
		this(context);
		this.update(username, password);
	}

	
	public Urllib login() throws LoginException, BankException {
		urlopen = new Urllib();
		String response = null;
		Matcher matcher;
		try {
			response = urlopen.open("https://secure246.lansforsakringar.se/lfportal/login/privat");
			matcher = reViewState.matcher(response);
			if (!matcher.find()) {
				throw new BankException(res.getText(R.string.unable_to_find).toString()+" ViewState.");
			}
			String strViewState = matcher.group(1);
			matcher = reEventValidation.matcher(response);
			if (!matcher.find()) {
				throw new BankException(res.getText(R.string.unable_to_find).toString()+" EventValidation.");
			}
			String strEventValidation = matcher.group(1);

			List <NameValuePair> postData = new ArrayList <NameValuePair>();
			postData.add(new BasicNameValuePair("inputPersonalNumber", username));
			postData.add(new BasicNameValuePair("inputPinCode", password));
			postData.add(new BasicNameValuePair("selMechanism", "PIN-kod"));
			postData.add(new BasicNameValuePair("__VIEWSTATE", strViewState));
			postData.add(new BasicNameValuePair("__EVENTVALIDATION", strEventValidation));
			postData.add(new BasicNameValuePair("__LASTFOCUS", ""));
			postData.add(new BasicNameValuePair("__EVENTTARGET", ""));
			postData.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
			postData.add(new BasicNameValuePair("btnLogIn.x", "12"));
			postData.add(new BasicNameValuePair("btnLogIn.y", "34"));
			Log.d("Bankdroid", "Posting data to: " + urlopen.getCurrentURI());
			response = urlopen.open(urlopen.getCurrentURI(), postData);
			String a = "https://secure246.lansforsakringar.se:443/lfportal/appmanager/privat/main?_nfpb=true&amp;_pageLabel=bank&newUc=true&isTopLevel=true";

			if (response.contains("Felaktig inloggning")) {
				throw new LoginException(res.getText(R.string.invalid_username_password).toString());
			}

			matcher = reToken.matcher(response);
			if (!matcher.find()) {
				throw new BankException(res.getText(R.string.unable_to_find).toString()+" token.");
			}
			String token = matcher.group(1);

			matcher = reUrl.matcher(response);
			if (!matcher.find()) {
				throw new BankException(res.getText(R.string.unable_to_find).toString()+" accounts url.");
			}
			accountsUrl = Html.fromHtml(matcher.group(1)).toString() + "&_token=" + token;
			if (!accountsUrl.contains("https://")) {
			    accountsUrl = "https://" + urlopen.getCurrentURI().split("/")[2] + accountsUrl;
			}
			Log.d("Bankdroid", "Accounts url: " + accountsUrl);
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
			if (accountsUrl == null) {
				Log.d(TAG, "accountsUrl is null, unable to update.");
				return;
			}
			response = urlopen.open(accountsUrl);
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
            super.updateComplete();
        }
	}
}