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

package com.liato.bankdroid.banking.banks;

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
import android.text.InputType;

import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.Transaction;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;

import eu.nullbyte.android.urllib.Urllib;

public abstract class IkanoPartnerBase extends Bank {
    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;
    private static final int INPUT_TYPE_PASSWORD = InputType.TYPE_CLASS_PHONE;
    private static final String INPUT_HINT_USERNAME = "ÅÅÅÅMMDDXXXX";
    

    private Pattern reEventValidation = Pattern.compile("__EVENTVALIDATION\"\\s+value=\"([^\"]+)\"");
    private Pattern reViewState = Pattern.compile("(?:__|javax\\.faces\\.)VIEWSTATE\"\\s+.*?value=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
    private Pattern reCtl = Pattern.compile("(ctl\\d{1,})_CustomValidationSummary", Pattern.CASE_INSENSITIVE);
    private Pattern reTransactionsUrl = Pattern.compile("(page___\\d{1,}\\.aspx)\"><span[^>]+>Transaktioner</span>", Pattern.CASE_INSENSITIVE);
    private Pattern reAccounts = Pattern.compile("captionLabel\">([^<]+)</span>\\s*</span>\\s*<span\\s*id=\"[^\"]+ReadOnlyValueSpan\">([^<]+)</span>\\s*<span\\s*id=\"[^\"]+currencyTextLiteralSpan\">([^<]+)</span>");
    private Pattern reTransactions = Pattern.compile("<td\\s*class=\"TransactionDateRow\">([^>]+)</td><td[^>]+>(.+?)</td><td[^>]+>([^<]+)</td><td[^>]+>([^<]+)</td>");
    private String response = null;
	protected String structId;
	

	public IkanoPartnerBase(Context context) {
		super(context);
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
        super.INPUT_TYPE_PASSWORD = INPUT_TYPE_PASSWORD;
        super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
	}

	public IkanoPartnerBase(String username, String password, Context context) throws BankException, LoginException {
		this(context);
		this.update(username, password);
	}

    @Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
        urlopen = new Urllib(true);
        response = urlopen.open("https://partner.ikanobank.se/web/engines/page.aspx?structid="+structId);
        Matcher matcher = reViewState.matcher(response);
        if (!matcher.find()) {
            throw new BankException(res.getText(R.string.unable_to_find).toString()+" ViewState.");
        }
        String viewState = matcher.group(1);
        matcher = reEventValidation.matcher(response);
        if (!matcher.find()) {
            throw new BankException(res.getText(R.string.unable_to_find).toString()+" EventValidation.");
        }
        String eventValidation = matcher.group(1);
        matcher = reCtl.matcher(response);
        if (!matcher.find()) {
            throw new BankException(res.getText(R.string.unable_to_find).toString()+" ctl.");
        }
        String ctl = matcher.group(1);
        List <NameValuePair> postData = new ArrayList <NameValuePair>();
        postData.add(new BasicNameValuePair("__VIEWSTATE", viewState));
        postData.add(new BasicNameValuePair("__EVENTVALIDATION", eventValidation));
        postData.add(new BasicNameValuePair(ctl+"$LoginWebUserControl$SSNControl$SSNSimpleValueUsercontrol$editValueTextbox", username));
        postData.add(new BasicNameValuePair(ctl+"$LoginWebUserControl$passwordSimpleValueControl$passwordSimpleValueControl$editValueTextbox", password));
        postData.add(new BasicNameValuePair(ctl+"$LoginButton", "Logga in"));
        return new LoginPackage(urlopen, postData, response, "https://partner.ikanobank.se/web/engines/page.aspx?structid="+structId);
    }

	@Override
	public Urllib login() throws LoginException, BankException {
		try {
		    LoginPackage lp = preLogin();
			response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
		
			if (response.contains("eller personnumme") || response.contains("elaktigt personnummer")
			        || response.contains("ontrollera personnummer") || response.contains("elaktig inloggningskod")) {
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
		Matcher matcher;
		try {
	        matcher = reTransactionsUrl.matcher(response);
	        if (!matcher.find()) {
	            throw new BankException(res.getText(R.string.unable_to_find).toString()+" transactions url.");
	        }
		    
			response = urlopen.open("https://partner.ikanobank.se/web/engines/"+matcher.group(1));
			matcher = reAccounts.matcher(response);
			int accId = 0;
			while (matcher.find()) {
	            /*
	             * Capture groups:
	             * GROUP                EXAMPLE DATA
	             * 1: Name              Kvar att handla för
	             * 2: Amount            17&nbsp;229,85
	             * 3: Currency          &nbsp;kr 
	             *   
	             */
				accounts.add(new Account(
				        Html.fromHtml(matcher.group(1)).toString().trim(),
				        Helpers.parseBalance(matcher.group(2)),
				        Integer.toString(accId)));
				accId++;
			}
			
			if (accounts.isEmpty()) {
				throw new BankException(res.getText(R.string.no_accounts_found).toString());
			}
			 // Use the amount from "Kvar att handla för" which should be the last account in the list.
		    this.balance = accounts.get(accounts.size()-1).getBalance();
		    
            ArrayList<Transaction> transactions = new ArrayList<Transaction>();
            matcher = reTransactions.matcher(response);
            while (matcher.find()) {
                /*
                 * Capture groups:
                 * GROUP                EXAMPLE DATA
                 * 1: Date              2011-02-27
                 * 2: Specification     Best Buy<br>
                 * 3: Amount            143,07 kr
                 * 4: Currency          SEK
                 *   
                 */                
                Transaction transaction = new Transaction(
                        matcher.group(1).trim(),
                        Html.fromHtml(matcher.group(2).replaceAll("<br>", " ")).toString().trim(),
                        Helpers.parseBalance(matcher.group(3)));
                transactions.add(transaction);
            }
            accounts.get(0).setTransactions(transactions);
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