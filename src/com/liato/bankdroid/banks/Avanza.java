package com.liato.bankdroid.banks;

import java.io.IOException;
import java.text.SimpleDateFormat;
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
import android.util.Log;

import com.liato.bankdroid.Account;
import com.liato.bankdroid.Bank;
import com.liato.bankdroid.BankException;
import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.LoginException;
import com.liato.bankdroid.R;
import com.liato.bankdroid.Transaction;
import com.liato.urllib.Urllib;

public class Avanza extends Bank {
	private static final String TAG = "Avanza";
	private static final String NAME = "Avanza";
	private static final String NAME_SHORT = "avanza";
	private static final String URL = "https://www.avanza.se/";
	private static final int BANKTYPE_ID = Bank.AVANZA;
	
	private Pattern reAccounts = Pattern.compile("depa\\.jsp\\?depotnr=(\\d*).*?(?:width=\"235\">*?)(.*?)<.*?(?:looser|winner).*?>(.*?)<.*?(?:looser|winner).*?>(.*?)<", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private Pattern reTransactions = Pattern.compile("(?:warrantguide\\.jsp|aktie\\.jsp)(?:.*?)orderbookId=(?:.*?)>(.*?)<(?:.*?)<nobr>(?:.*?)<nobr>(?:.*?)<nobr>(.*?)<", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	public Avanza(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = URL;
	}

	public Avanza(String username, String password, Context context) throws BankException, LoginException {
		this(context);
		this.update(username, password);
	}

	@Override
	public Urllib login() throws LoginException, BankException {
		urlopen = new Urllib(true, true);
		String response = null;
		try {
			//response = urlopen.open("https://www.avanza.se/aza/login/logout.jsp");

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
		Urllib urlopen = login();
		String response = null;
		Matcher matcher;
		try {
			Log.d("BankAvanza", "Opening: https://www.avanza.se/aza/depa/sammanfattning/sammanfattning.jsp");
			response = urlopen.open("https://www.avanza.se/aza/depa/sammanfattning/sammanfattning.jsp");
						
			matcher = reAccounts.matcher(response);
			while (matcher.find()) {
				accounts.add(new Account(Html.fromHtml(matcher.group(1)).toString().trim(), Helpers.parseBalance(matcher.group(4)), matcher.group(1).trim()));
				balance = balance.add(Helpers.parseBalance(matcher.group(4)));
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

	@Override
	public void updateTransactions(Account account, Urllib urlopen) throws LoginException, BankException {
		super.updateTransactions(account, urlopen);

		String response = null;
		Matcher matcher;
		try {						
			response = urlopen.open("https://www.avanza.se/aza/depa/depa.jsp?depotnr="+account.getId());
			matcher = reTransactions.matcher(response);
			ArrayList<Transaction> transactions = new ArrayList<Transaction>();
			String strDate = null;
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			strDate = sdf.format(cal.getTime());

			while (matcher.find()) {
				//Log.d(TAG, "Date: "+strDate+"; Trans: "+Html.fromHtml(matcher.group(2)).toString().trim()+"; Amount: "+Helpers.parseBalance(matcher.group(3)).toString());
				transactions.add(new Transaction(strDate, Html.fromHtml(matcher.group(1)).toString().trim(), Helpers.parseBalance(matcher.group(2))));
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