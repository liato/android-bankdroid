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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.text.Html;
import android.text.InputType;

import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.BankChoice;
import com.liato.bankdroid.banking.Transaction;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class Swedbank extends Bank {
	private static final String TAG = "Swedbank";
	private static final String NAME = "Swedbank";
	private static final String NAME_SHORT = "swedbank";
	private static final String URL = "https://mobilbank.swedbank.se/";
	private static final int BANKTYPE_ID = IBankTypes.SWEDBANK;
    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;
    private static final String INPUT_HINT_USERNAME = "ÅÅÅÅMMDDXXXX";
    
	private Pattern reCSRF = Pattern.compile("csrf_token\".*?value=\"([^\"]+)\"");
	private Pattern reAccounts = Pattern.compile("(account|loan)\\.html\\?id=([^\"]+)\"[^>]*>\\s*(?:<span\\sclass=\"icon\">[^<]*</span>\\s*)?<span\\s*class=\"name\">([^<]+)</span>\\s*(?:<br/>\\s*)?<span\\s*class=\"amount\">([^<]+)</");
	private Pattern reLinklessAccounts = Pattern.compile("fix\">\\s*<span\\sclass=\"name\">([^<]+)</span>\\s*<br\\s?/>\\s*<span\\sclass=\"amount\">([^<]+)</span>\\s*</div>", Pattern.CASE_INSENSITIVE);
	private Pattern reTransactions = Pattern.compile("date\">([^<]+)</span>\\s*<br\\s?/>\\s*<span\\s*class=\"receiver\">([^<]+)</span>\\s*<br\\s?/>\\s*<span\\s*class=\"amount\">([^<]+)</span>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	private Pattern reLoanData = Pattern.compile("<li[^>]*>([^<]+)<br/><span\\s*class=\"secondary\">([^<]+)</span></li>");
	private Pattern reMultipleBanks = Pattern.compile("menu\\.html\\?bank=(\\d{1,})\">([^<]+)</");
	
	String response;
	public Swedbank(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = URL;
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
        super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
	}

	public Swedbank(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
		this(context);
		this.update(username, password);
	}

	@Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
        urlopen = new Urllib(context, CertificateReader.getCertificates(context, R.raw.cert_swedbank));
        urlopen.setContentCharset(HTTP.ISO_8859_1);
        Matcher matcher;
        response = urlopen.open("https://mobilbank.swedbank.se/banking/swedbank/login.html");
        matcher = reCSRF.matcher(response);
        if (!matcher.find()) {
            throw new BankException(res.getText(R.string.unable_to_find).toString()+" CSRF token.");
        }
        String csrftoken = matcher.group(1);
        List <NameValuePair> postData = new ArrayList <NameValuePair>();
        postData.add(new BasicNameValuePair("xyz", username));
        postData.add(new BasicNameValuePair("auth-method", "code"));
        postData.add(new BasicNameValuePair("_csrf_token", csrftoken));
        response = urlopen.open("https://mobilbank.swedbank.se/banking/swedbank/loginNext.html", postData);

        matcher = reCSRF.matcher(response);
        if (!matcher.find()) {
            throw new BankException(res.getText(R.string.unable_to_find).toString()+" CSRF token.");
        }
        csrftoken = matcher.group(1);
        postData.clear();
        postData.add(new BasicNameValuePair("zyx", password));
        postData.add(new BasicNameValuePair("_csrf_token", csrftoken));
        String banknr = (getExtras() != null && getExtras().length() > 0) ? "?bank="+getExtras() : "";
        return new LoginPackage(urlopen, postData, response, "https://mobilbank.swedbank.se/banking/swedbank/login.html"+banknr);
    }

    @Override
	public Urllib login() throws LoginException, BankException {
	    
	    try {
	        LoginPackage lp = preLogin();
	        response = urlopen.open(lp.getLoginTarget(), lp.getPostData());

			if (response.contains("misslyckats")) {
				throw new LoginException(res.getText(R.string.invalid_username_password).toString());
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

	@Override
	public void update() throws BankException, LoginException, BankChoiceException {
		super.update();
		if (username == null || password == null || username.length() == 0 || password.length() == 0) {
			throw new LoginException(res.getText(R.string.invalid_username_password).toString());
		}
		urlopen = login();

		Matcher matcher;
		if (getExtras() == null || getExtras().length() == 0) {
    		ArrayList<BankChoice> banks = new ArrayList<BankChoice>();
    		matcher = reMultipleBanks.matcher(response);
    		while (matcher.find()) {
                /*
                 * Capture groups:
                 * GROUP                EXAMPLE DATA
                 * 1: ID                0 | 1 | ..
                 * 2: Name              Ostlands Sparbank
                 *  
                 */ 		    
    		    banks.add(new BankChoice(Html.fromHtml(matcher.group(2)).toString().trim(), matcher.group(1)));
    		}
    		if (!banks.isEmpty()) {
    		    throw new BankChoiceException("Select a bank.", banks);
    		}
		}
		
        String banknr = (getExtras() != null && getExtras().length() > 0) ? "?bank="+getExtras() : "";
		try {
	        response = urlopen.open("https://mobilbank.swedbank.se/banking/swedbank/menu.html"+banknr);
			response = urlopen.open("https://mobilbank.swedbank.se/banking/swedbank/accounts.html");
			matcher = reAccounts.matcher(response);
			while (matcher.find()) {
                /*
                 * Capture groups:
                 * GROUP                EXAMPLE DATA
                 * 1: Type              account|loan
                 * 2: ID                0
                 * 3: Name              Privatkonto
                 * 4: Amount            5 678 
                 *  
                 */			    
				Account account = new Account(Html.fromHtml(matcher.group(3)).toString(), Helpers.parseBalance(matcher.group(4)), "loan".equalsIgnoreCase(matcher.group(1).trim()) ? "l:" + matcher.group(2).trim() : matcher.group(2).trim());
				if ("loan".equalsIgnoreCase(matcher.group(1).trim())) {
				    account.setType(Account.LOANS);
				}
				else {
				    balance = balance.add(Helpers.parseBalance(matcher.group(4)));
				}
				accounts.add(account);
			}
			matcher = reLinklessAccounts.matcher(response);
			int accid = 0;
			while (matcher.find()) {
				Account account = new Account(Html.fromHtml(matcher.group(1)).toString(), Helpers.parseBalance(matcher.group(2)), "ll:"+accid);
				account.setType(Account.OTHER);
                accounts.add(account);
                accid++;
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
        // Demo account to use with screenshots
        //accounts.add(new Account("Personkonto", Helpers.parseBalance("8351"), "0"));
        //accounts.add(new Account("Sparkonto", Helpers.parseBalance("59070"), "1"));
	}
	
	@Override
	public void updateTransactions(Account account, Urllib urlopen) throws LoginException, BankException {
		super.updateTransactions(account, urlopen);
		if (account.getType() == Account.OTHER) return;

		String response = null;
		Matcher matcher;
		try {
            ArrayList<Transaction> transactions = new ArrayList<Transaction>();
		    if (account.getType() == Account.LOANS) {
		        String [] accountId = account.getId().split(":", 2);
		        if (accountId.length < 2) return;
	            response = urlopen.open("https://mobilbank.swedbank.se/banking/swedbank/loan.html?id="+accountId[1]);
	            matcher = reLoanData.matcher(response);
	            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	            Calendar cal = Calendar.getInstance(); 
	            String date = sdf.format(cal.getTime());
	            while (matcher.find()) {
	                /*
	                 * Capture groups:
	                 * GROUP                    EXAMPLE DATA
	                 * 1: Title                 Totalt | Clearingnummer
	                 * 2: Value                 12 345 | 8032-5
	                 * 
	                 */
	                Transaction transaction = new Transaction(date, Html.fromHtml(matcher.group(1)).toString().trim(), Helpers.parseBalance(matcher.group(2)));
	                transaction.setCurrency("");
	                transactions.add(transaction);
	            }
		    }
		    else {
	            response = urlopen.open("https://mobilbank.swedbank.se/banking/swedbank/account.html?id="+account.getId());
	            matcher = reTransactions.matcher(response);
	            while (matcher.find()) {
                    /*
                     * Capture groups:
                     * GROUP                    EXAMPLE DATA
                     * 1: Date                  11-04-06
                     * 2: Specification         Pressbyran
                     * 3: Amount                -20
                     * 
                     */	                
	                transactions.add(new Transaction("20"+matcher.group(1).trim(), Html.fromHtml(matcher.group(2)).toString().trim(), Helpers.parseBalance(matcher.group(3))));
	            }
		    }
			account.setTransactions(transactions);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Demo transactions to use with screenshots
		/*
		try {
            response = IOUtils.toString(context.getResources().openRawResource(R.raw.swedbank_transactions));
            matcher = reTransactions.matcher(response);
            ArrayList<Transaction> transactions = new ArrayList<Transaction>();
            while (matcher.find()) {
                transactions.add(new Transaction("20"+matcher.group(1).trim(), Html.fromHtml(matcher.group(2)).toString().trim(), Helpers.parseBalance(matcher.group(3))));
            }
            account.setTransactions(transactions);
        }
        catch (NotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        */
	}
}
