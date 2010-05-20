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
import android.util.Log;

import com.liato.urllib.Urllib;

public class BankLansforsakringar implements Bank {

	private String username;
	private String password;
	private Banks banktype = Banks.LANSFORSAKRINGAR;
	private Pattern reEventValidation = Pattern.compile("__EVENTVALIDATION\"\\s+value=\"([^\"]+)\"");
	private Pattern reViewState = Pattern.compile("__VIEWSTATE\"\\s+value=\"([^\"]+)\"");
	private Pattern reError = Pattern.compile("ErrMessage.+?>([^<]+)",Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private Pattern reBalance = Pattern.compile("<a.+?DataTable1:(\\d+):account[^>]+>([^<]+)</a>.+?<spa.+?>([0-9 -,.]+)</span></td>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private Pattern reToken = Pattern.compile("var\\s+token\\s*=\\s*'([^']+)'", Pattern.CASE_INSENSITIVE);
	
	private ArrayList<Account> accounts = new ArrayList<Account>();
	private BigDecimal balance = new BigDecimal(0);

	public BankLansforsakringar() {
	}

	public BankLansforsakringar(String username, String password) throws BankException {
		this.update(username, password);
	}

	public void update(String username, String password) throws BankException {
		this.username = username;
		this.password = password;
		this.update();
	}
	public void update() throws BankException {
		if (username == null || password == null || username.length() == 0 || password.length() == 0) {
			throw new BankException("Personnummer och lösenord stämmer ej.");//res.getText(R.string.invalid_username_password));
		}

		Urllib urlopen = new Urllib();
		String response = null;
		Matcher matcher;
		try {
			response = urlopen.open("https://secure246.lansforsakringar.se/lfportal/login/privat");
			matcher = reViewState.matcher(response);
			if (!matcher.find()) {
				throw new BankException("Could not connect to the bank. Unable to parse ViewState.");
			}
			String strViewState = matcher.group(1);
			matcher = reEventValidation.matcher(response);
			if (!matcher.find()) {
				throw new BankException("Could not connect to the bank. Unable to parse EventValidation.");
			}
			String strEventValidation = matcher.group(1);
			
			matcher = reEventValidation.matcher(response);
			if (!matcher.find()) {
				throw new BankException("Could not connect to the bank. Unable to parse EventValidation.");
			}			
			List <NameValuePair> postData = new ArrayList <NameValuePair>();
			postData.add(new BasicNameValuePair("inputPersonalNumber", username));
			postData.add(new BasicNameValuePair("inputPinCode", password));
			postData.add(new BasicNameValuePair("selMechanism", "PIN-kod"));
			postData.add(new BasicNameValuePair("__VIEWSTATE", strViewState));
			postData.add(new BasicNameValuePair("__EVENTVALIDATION", strEventValidation));
			postData.add(new BasicNameValuePair("__LASTFOCUS", ""));
			postData.add(new BasicNameValuePair("__EVENTTARGET", ""));
			postData.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
			
			response = urlopen.open(urlopen.getCurrentURI(), postData);
			
			matcher = reError.matcher(response);
			if (matcher.find()) {
				throw new BankException("Personnummer och lösenord stämmer ej.");
			}

			response = urlopen.open("https://secure246.lansforsakringar.se/lfportal/appmanager/privat/main?_nfpb=true&_pageLabel=bank_konto&newUc=true&isTopLevel=true");
			matcher = reBalance.matcher(response);
			while (matcher.find()) {
				accounts.add(new Account(Html.fromHtml(matcher.group(2)).toString().trim(), Helpers.parseBalance(matcher.group(3).trim()), matcher.group(1).trim()));
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