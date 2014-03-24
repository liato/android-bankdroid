package com.liato.bankdroid.banking.banks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.text.InputType;

import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;

public class BlekingeTrafiken extends Bank {
	private static final String TAG = "BlekingeTrafiken";
	private static final String NAME = "BlekingeTrafiken";
	private static final String NAME_SHORT = "blekingetrafiken";
	private static final int BANKTYPE_ID = IBankTypes.BLEKINGETRAFIKEN;

	private static Pattern rePattern = Pattern.compile("Namn: <strong>(.*?)</strong>(.|\\n)*?Giltighetstid:(.|\\n)*?<strong>(.*?)</strong><br />(.|\\n)*?reskassa: <strong>(.*?)</strong>");

	public BlekingeTrafiken(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.INPUT_TITLETEXT_USERNAME = R.string.card_number;
		super.INPUT_HINT_USERNAME = "XXXXXXXXXX";
		super.INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_NUMBER;
		super.INPUT_HIDDEN_PASSWORD = true;
	}

	public BlekingeTrafiken(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
		this(context);
		this.update(username, password);
	}

	@Override
	public void update() throws BankException, LoginException, BankChoiceException {
		super.update();

		if (username == null || username.length() == 0) {
			throw new LoginException(res.getText(R.string.invalid_username_password).toString());
		}

		try {
			List<NameValuePair> postData = new ArrayList<NameValuePair>();
			postData.add(new BasicNameValuePair("card_serial_nr", username));

			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost("http://webshop.blekingetrafiken.se/card/checkcard?user_action=check");
			httppost.setEntity(new UrlEncodedFormEntity(postData));
			HttpResponse response = httpclient.execute(httppost);

			HttpEntity entity = response.getEntity();
			if (entity == null)
				throw new BankException("Couldn't connect!");

			String html = EntityUtils.toString(entity);
			
			Matcher matcher = rePattern.matcher(html);

			if (!matcher.find()) {
				if (html.contains("Kortet finns ej i kortdatabasen"))
					throw new BankException(res.getText(R.string.invalid_card_number).toString());
				else
					throw new BankException("Couldn't parse value!");
			}

			accounts.add(new Account("Reskassa", Helpers.parseBalance(matcher.group(6)), "1"));

			if (!matcher.group(1).contains("Finns ingen")) {
				int pos = matcher.group(4).length() - 10;
				String periodEnd = matcher.group(4).substring(pos).replaceAll("-", "");
				
				accounts.add(new Account(matcher.group(1) + ", Periodslut:", Helpers.parseBalance(periodEnd),
							"2", Account.PERIOD, ""));
			}
		} catch (ClientProtocolException e) {
			throw new BankException(e.getMessage());
		} catch (IOException e) {
			throw new BankException(e.getMessage());
		} finally {
			super.updateComplete();
		}
	}
}