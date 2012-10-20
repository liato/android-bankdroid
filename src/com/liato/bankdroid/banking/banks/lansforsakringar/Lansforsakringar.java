/*
 * Copyright (C) 2010 Nullbyte <http://nullbyte.eu>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liato.bankdroid.banking.banks.lansforsakringar;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.text.Html;
import android.text.InputType;
import android.util.Log;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.Transaction;
import com.liato.bankdroid.banking.banks.lansforsakringar.model.request.AccountsRequest;
import com.liato.bankdroid.banking.banks.lansforsakringar.model.request.ChallengeRequest;
import com.liato.bankdroid.banking.banks.lansforsakringar.model.request.LoginRequest;
import com.liato.bankdroid.banking.banks.lansforsakringar.model.request.TransactionsRequest;
import com.liato.bankdroid.banking.banks.lansforsakringar.model.response.AccountsResponse;
import com.liato.bankdroid.banking.banks.lansforsakringar.model.response.ChallengeResponse;
import com.liato.bankdroid.banking.banks.lansforsakringar.model.response.LoginResponse;
import com.liato.bankdroid.banking.banks.lansforsakringar.model.response.NumberResponse;
import com.liato.bankdroid.banking.banks.lansforsakringar.model.response.TransactionsResponse;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;

import eu.nullbyte.android.urllib.Urllib;

public class Lansforsakringar extends Bank {
    private static final String TAG = "Lansforsakringar";
    private static final String NAME = "Länsförsäkringar";
    private static final String NAME_SHORT = "lansforsakringar";
    private static final String URL = "https://mobil.lansforsakringar.se/lf-mobile/pages/login.faces";
    private static final int BANKTYPE_ID = IBankTypes.LANSFORSAKRINGAR;
    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;
    private static final int INPUT_TYPE_PASSWORD = InputType.TYPE_CLASS_PHONE;
    private static final String INPUT_HINT_USERNAME = "ÅÅMMDD-XXXX";

    private Pattern reEventValidation = Pattern.compile("__EVENTVALIDATION\"\\s+value=\"([^\"]+)\"");
    private Pattern reViewState = Pattern.compile("(?:__|javax\\.faces\\.)VIEWSTATE\"\\s+.*?value=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
    private Pattern reAccountsReg = Pattern.compile("AccountNumber=([0-9]+)[^>]+>([^<]+)<.*?<td class=\"right\"[^>]+>([0-9 .,-]+)</td", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private Pattern reAccountsFunds = Pattern.compile("fundsDataTable[^>]+>([^<]+)</span></a></td><td[^>]+></td><td[^>]+><span\\sid=\"fundsDataTable:\\d{1,}:bankoverview_\\d{1,}_([^\"]+)\">([0-9 .,-]+)</span", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private Pattern reAccountsLoans = Pattern.compile("LoanNumber=[^>]+>([^<]+)</a></td><td class=\"left\" width=\"25%\">([0-9.]+)</td><td class=\"right\" width=\"25%\">([0-9 .,-]+)</td><td class=\"right\" width=\"25%\">([0-9 .,-]+)</td", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private Pattern rePension = Pattern.compile("class=\"portlet-menu-item\" [^>]+>(.*)</a></td><td class=\"left\" width=\"25%\">([^>]+)</td><td class=\"right\" width=\"25%\">([^>]+)</td><td class=\"right\" width=\"25%\">([^>]+)</td>", Pattern.CASE_INSENSITIVE);
    private Pattern reToken = Pattern.compile("var\\s+token\\s*=\\s*'([^']+)'", Pattern.CASE_INSENSITIVE);
    private Pattern reHiddenToken = Pattern.compile("name=\"_token\" value=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
    private Pattern reUrl = Pattern.compile("<li class=\"bank\\s*\">\\s*<a href=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
    private Pattern reTransactions = Pattern.compile("[^>]+>(\\d{4}-\\d{2}-\\d{2})<.*(\\d{4}-\\d{2}-\\d{2})<.*<span title=\"([^\"]+)\".*</span></td><td class=\"left\"><span title=\"([^\"]+)\".*</span></td><td class=\"right nowrap\" style=\"width:90px\">([^<]+)</td>.*style=\"width:90px\">([^<]+)</td>", Pattern.CASE_INSENSITIVE);
    private String accountsUrl = null;
    private String mRequestToken = null;
    private String mViewState = null;
    private String host = null;
    private boolean mFirstTransactionPage = true;
    
    
    
    private ObjectMapper mObjectMapper = new ObjectMapper();

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
        mObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mObjectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        mObjectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }

    public Lansforsakringar(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
        this(context);
        this.update(username, password);
    }


    
    @Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
        urlopen = new Urllib(false, true);
        String response = urlopen.open("https://secure246.lansforsakringar.se/lfportal/login/privat");
        Matcher matcher = reViewState.matcher(response);
        if (!matcher.find()) {
            throw new BankException(res.getText(R.string.unable_to_find).toString()+" ViewState.");
        }
        mViewState = matcher.group(1);
        matcher = reEventValidation.matcher(response);
        if (!matcher.find()) {
            throw new BankException(res.getText(R.string.unable_to_find).toString()+" EventValidation.");
        }
        String strEventValidation = matcher.group(1);

        List <NameValuePair> postData = new ArrayList <NameValuePair>();
        postData.add(new BasicNameValuePair("inputPersonalNumber", username));
        postData.add(new BasicNameValuePair("inputPinCode", password));
        postData.add(new BasicNameValuePair("selMechanism", "PIN-kod"));
        postData.add(new BasicNameValuePair("__VIEWSTATE", mViewState));
        postData.add(new BasicNameValuePair("__EVENTVALIDATION", strEventValidation));
        postData.add(new BasicNameValuePair("__LASTFOCUS", ""));
        postData.add(new BasicNameValuePair("__EVENTTARGET", ""));
        postData.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
        postData.add(new BasicNameValuePair("btnLogIn.x", "12"));
        postData.add(new BasicNameValuePair("btnLogIn.y", "34"));
        return new LoginPackage(urlopen, postData, response, urlopen.getCurrentURI());
    }

    public Urllib login() throws LoginException, BankException {
        try {
            LoginPackage lp = preLogin();
            String response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
            if (response.contains("Felaktig inloggning")) {
                throw new LoginException(res.getText(R.string.invalid_username_password).toString());
            }

            Matcher matcher = reToken.matcher(response);
            if (!matcher.find()) {
                //throw new BankException(res.getText(R.string.unable_to_find).toString()+" token0.");
            } else {
            	mRequestToken = matcher.group(1);
            }

            matcher = reUrl.matcher(response);
            if (!matcher.find()) {
                throw new BankException(res.getText(R.string.unable_to_find).toString()+" accounts url.");
            }

            host = urlopen.getCurrentURI().split("/")[2];
            accountsUrl = Html.fromHtml(matcher.group(1)).toString() + "&_token=" + getRequestToken();
            if (!accountsUrl.contains("https://")) {
                accountsUrl = "https://" + host + accountsUrl;
            }

        }
        catch (ClientProtocolException e) {
            throw new BankException(e.getMessage());
        }
        catch (IOException e) {
            throw new BankException(e.getMessage());
        }
        return urlopen;
    }
    
    private String getRequestToken() {
    	return mRequestToken != null ? mRequestToken : "";
    }
    
    private <T> T readJsonValue(InputStream is, Class<T> valueType) throws BankException {
        try {
			return mObjectMapper.readValue(is, valueType);
		} catch (Exception e) {
			e.printStackTrace();
			throw new BankException(e.getMessage());
		}
    }

    private <T> T readJsonValue(String url, String postData, Class<T> valueType) throws BankException {
    	try {
			return readJsonValue(urlopen.openStream(url, postData, false), valueType);
		} catch (Exception e) {
			e.printStackTrace();
			throw new BankException(e.getMessage());
		}
    }
    
    public String objectAsJson(Object value) {
    	try {
			return mObjectMapper.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
    	return null;
    }
    
    private String generateChallenge(int originalChallenge) {
		try {
			String h = Integer.toHexString(originalChallenge + (1000 * 20 / 4) + 100 * (18 / 3) + 10 * (2 / 2) + 6);
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] messageDigest = md.digest(h.getBytes());
			BigInteger number = new BigInteger(1,messageDigest);
			String md5 = number.toString(16);
			while(md5.length() < 40)
				md5 = "0" + md5;        
			return md5;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
    		
    }
    
    @Override
    public void update() throws BankException, LoginException, BankChoiceException {
        super.update();
        if (username == null || password == null || username.length() == 0 || password.length() == 0) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }

        urlopen = new Urllib();
        urlopen.addHeader("Content-Type", "application/json; charset=UTF-8");
        urlopen.addHeader("DeviceId", UUID.randomUUID().toString());//"9ba6991346f2f8e9");
        urlopen.addHeader("deviceInfo", "Galaxy Nexus;4.1.1;1.8;Portrait");
        urlopen.setUserAgent("bankdroid"); // Android app uses "lf-android-app"

        NumberResponse nr = readJsonValue("https://mobil.lansforsakringar.se/appoutlet/security/client", null, NumberResponse.class);
        ChallengeResponse cr = readJsonValue("https://mobil.lansforsakringar.se/appoutlet/security/client", objectAsJson(new ChallengeRequest(nr.getNumber(), nr.getNumberPair(), generateChallenge(nr.getNumber()))), ChallengeResponse.class);
        urlopen.addHeader("Ctoken", cr.getToken());
        LoginResponse lr = readJsonValue("https://mobil.lansforsakringar.se/appoutlet/security/user", objectAsJson(new LoginRequest(username, password)), LoginResponse.class);
        urlopen.addHeader("Utoken", lr.getTicket());

        AccountsResponse ar = readJsonValue("https://mobil.lansforsakringar.se/appoutlet/account/bytype", objectAsJson(new AccountsRequest(AccountsRequest.Type.CHECKING)), AccountsResponse.class);
        for (com.liato.bankdroid.banking.banks.lansforsakringar.model.response.Account a : ar.getAccounts()) {
        	accounts.add(new Account(a.getAccountName(), new BigDecimal(a.getDispoibleAmount()), a.getAccountNumber()));
        	//a.getLedger() should be saved to database, used when fetching transactions
        	balance = balance.add(new BigDecimal(a.getDispoibleAmount()));
        }
        ar = readJsonValue("https://mobil.lansforsakringar.se/appoutlet/account/bytype", objectAsJson(new AccountsRequest(AccountsRequest.Type.SAVING)), AccountsResponse.class);
        for (com.liato.bankdroid.banking.banks.lansforsakringar.model.response.Account a : ar.getAccounts()) {
        	accounts.add(new Account(a.getAccountName(), new BigDecimal(a.getDispoibleAmount()), a.getAccountNumber()));
        	balance = balance.add(new BigDecimal(a.getDispoibleAmount()));
        }        
        if (accounts.isEmpty()) {
        	throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
        super.updateComplete();
    }

    @Override
    public void updateTransactions(Account account, Urllib urlopen) throws LoginException, BankException {
        super.updateTransactions(account, urlopen);
        // No transaction history for funds and loans
        if (account.getType() != Account.REGULAR) return;
        
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        //TODO: Get upcoming transactions?
        //TransactionsResponse tr = readJsonValue("https://mobil.lansforsakringar.se/appoutlet/account/upcoming", objectAsJson(new UpcomingTransactionsRequest(account.getId())), TransactionsResponse.class);
        TransactionsResponse tr = readJsonValue("https://mobil.lansforsakringar.se/appoutlet/account/transaction", objectAsJson(new TransactionsRequest(0, "DEPIOSIT", account.getId())), TransactionsResponse.class);
        
        for (com.liato.bankdroid.banking.banks.lansforsakringar.model.response.Transaction t : tr.getTransactions()) {
        	//TODO: Set locale on date?
        	transactions.add(new Transaction(Helpers.formatDate(new Date(t.getTransactiondate())), t.getText(), new BigDecimal(t.getAmmount())));
        }

        account.setTransactions(transactions);        
        super.updateComplete();
    }       	
}