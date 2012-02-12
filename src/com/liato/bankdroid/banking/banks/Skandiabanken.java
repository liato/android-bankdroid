package com.liato.bankdroid.banking.banks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.provider.Settings.Secure;
import android.text.InputType;

import com.liato.bankdroid.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;

import eu.nullbyte.android.urllib.Urllib;

public class Skandiabanken extends Bank {
	private static final String NAME = "Skandiabanken";
	private static final String NAME_SHORT = "skandiabanken";
	private static final int BANKTYPE_ID = IBankTypes.SKANDIABANKEN;
	private static final String URL = "http://www.skandiabanken.se/hem/";
	private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;
	private static final String INPUT_HINT_USERNAME = "ÅÅMMDDXXXX";
	private static final int INPUT_TYPE_PASSWORD = InputType.TYPE_CLASS_PHONE;

	private static final String BASE_URL = "https://smartrefill.se/BankServices";

	private static final String HTTP_HEADER_SMARTREFILL_APPLICATION_ID = "x-smartrefill-application";
	private static final String HTTP_HEADER_SMARTREFILL_APPLICATION_VERSION = "x-smartrefill-version";
	private static final String HTTP_HEADER_SMARTREFILL_COUNTRY_CODE = "x-smartrefill-country-code";
	private static final String HTTP_HEADER_SMARTREFILL_CUSTOMER_OWNER = "x-smartrefill-customer-owner";
	private static final String HTTP_HEADER_SMARTREFILL_DEVICE_ID = "x-smartrefill-device";
	private static final String HTTP_HEADER_SMARTREFILL_INFLOW = "x-smartrefill-inflow";

	private static final String INFLOW_ANDROID = "Android";
	private final static String customerOwner = "SKANDIABANKEN";
	private final static String countryCode = "SE";
	private final static String SERVICE_NAME = "bank";

	private int customerId = 0;

	public Skandiabanken(Context context) {
		super(context);
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = URL;
		super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
		super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
		super.INPUT_TYPE_PASSWORD = INPUT_TYPE_PASSWORD;
	}

	public Skandiabanken(String username, String password, Context context)
			throws BankException, LoginException, BankChoiceException {
		this(context);
		this.update(username, password);
	}

	@Override
	public Urllib login() throws LoginException, BankException {
		urlopen = new Urllib();

		HashMap<String, String> headers = urlopen.getHeaders();
		headers.put(HTTP_HEADER_SMARTREFILL_APPLICATION_ID,
				"se.skandiabanken.android.wallet");
		headers.put(HTTP_HEADER_SMARTREFILL_APPLICATION_VERSION, "9");
		headers.put(HTTP_HEADER_SMARTREFILL_COUNTRY_CODE, countryCode);
		headers.put(HTTP_HEADER_SMARTREFILL_CUSTOMER_OWNER, customerOwner);
		headers.put(HTTP_HEADER_SMARTREFILL_DEVICE_ID, getDeviceId());
		headers.put(HTTP_HEADER_SMARTREFILL_INFLOW, INFLOW_ANDROID);
		
		urlopen.setUserAgent(null);

		String loginUrl = getBaseUrlWithCustomerOwner() + "/login";
		List<NameValuePair> postData = new ArrayList<NameValuePair>();
		postData.add(new BasicNameValuePair("username", username));
		postData.add(new BasicNameValuePair("password", password));

		try {
			String loginResponse = urlopen.open(loginUrl, postData);
			JSONObject obj = new JSONObject(loginResponse);
			customerId = (int) obj.getLong("id");
		} catch (HttpResponseException e) {
			if (e.getStatusCode() == 401)
				throw new BankException(
						"Inloggning misslyckad fel användarnamn eller lösenord");
			else
				throw new BankException("Http fel (" + e.getStatusCode() + ") "
						+ e.getMessage());
		} catch (ClientProtocolException e) {
			throw new BankException("ClientProtocolException " + e.getMessage());
		} catch (IOException e) {
			throw new BankException("IOException " + e.getMessage());
		} catch (JSONException e) {
			throw new BankException("Oväntat svarsformat " + e.getMessage());
		}

		return urlopen;
	}

	private String getBaseUrlWithCustomerOwner() {
		return BASE_URL + "/rest/" + SERVICE_NAME + "/" + countryCode + "/"
				+ customerOwner;
	}

	private void logout() {
		String logoutUrl = getBaseUrlWithCustomerOwner() + "/customer/"
				+ customerId + "/logout";
		try {
			urlopen.post(logoutUrl);
		} catch (IOException e) {
		}
	}

	@Override
	public void update() throws BankException, LoginException,
			BankChoiceException {
		super.update();

		if (username == null || password == null || username.length() == 0
				|| password.length() == 0)
			throw new LoginException(res.getText(
					R.string.invalid_username_password).toString());

		login();

		String accountsUrl = getBaseUrlWithCustomerOwner() + "/customer/"
				+ customerId + "/accounts";
		String accountsJsonString;
		try {
			accountsJsonString = urlopen.open(accountsUrl);
			JSONObject json = new JSONObject(accountsJsonString);
			// TODO Handle accounts
		} catch (IOException e) {
			throw new BankException("IOException " + e.getMessage());
		} catch (JSONException e) {
			throw new BankException("Oväntat svarsformat " + e.getMessage());
		}
	}

	@Override
	public void updateTransactions(Account account, Urllib urlopen)
			throws LoginException, BankException {
		super.updateTransactions(account, urlopen);

		String accountTransactionsUrl = getBaseUrlWithCustomerOwner()
				+ "/customer/" + customerId + "/account/" + account.getId();
		// TODO Get transactions
	}

	@Override
	public void closeConnection() {
		logout();
		super.closeConnection();
	}

	public String getDeviceId() {
		// TelephonyManager localTelephonyManager =
		// (TelephonyManager)context.getSystemService("phone");
		// if (localTelephonyManager.getDeviceId() != null) // null for emulator
		// return localTelephonyManager.getDeviceId();
		// else
		// return "000000000000000";
		// We should return the imei of the phone (se code above)
		// As we would need permission to read imei we use something else that
		// is unique and constant
		// Bankdroid should have as less permissions as possible...
		String test = Secure.getString(context.getContentResolver(),
				Secure.ANDROID_ID);

		if (test == null) // null for emulator
			test = "0";

		// convert to decimal string (imei is decimal)
		try {
			test = String.valueOf(Integer.parseInt(test, 16));
		} catch (NumberFormatException e) {
		}

		while (test.length() < 16)
			test += "0";

		return test.substring(0, 15);
	}
}
