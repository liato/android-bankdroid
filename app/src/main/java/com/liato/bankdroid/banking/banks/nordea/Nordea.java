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

package com.liato.bankdroid.banking.banks.nordea;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.text.Html;
import android.text.InputType;

import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.Transaction;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;

import eu.nullbyte.android.urllib.Urllib;

public class Nordea extends Bank {
	private static final String TAG = "Nordea";
	private static final String NAME = "Nordea";
	private static final String NAME_SHORT = "nordea";
	private static final String BASE_URL = "https://internetbanken.privat.nordea.se/nsp/";
	private static final String LOGIN_URL = BASE_URL + "login";
	private static final int BANKTYPE_ID = IBankTypes.NORDEA;
    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;
    private static final int INPUT_TYPE_PASSWORD = InputType.TYPE_CLASS_PHONE;
    private static final String INPUT_HINT_USERNAME = "ÅÅÅÅMMDDXXXX";
    
    private static final int MAX_TRANSACTIONS = 50;

    private Pattern reSimpleLoginLink = Pattern.compile(
            "href=\"(login\\?" +
            "(?=[^\"]*usecase=commonlogin)" +
            "(?=[^\"]*command=commonlogintabcommand)" +
            "(?=[^\"]*guid=([\\w]*))" +
            "(?=[^\"]*fpid=([\\w]*))" +
            "(?=[^\"]*commonlogintab=2)" +
            "(?=[^\"]*hash=([\\w]*))" +
            "[^\"]*)",
            Pattern.CASE_INSENSITIVE
    );
    private Pattern reLoginFormContents = Pattern.compile("<form[^>]+id=\"commonlogin\"[^>]*>(.*?)</form>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private Pattern reNonTextInputField = Pattern.compile("<input(?=[^>]+type=\"((?!text)[^\"]*)\")(?=[^>]+name=\"([^\"]+)\")(?=[^>]+value=\"([^\"]+)\")", Pattern.CASE_INSENSITIVE);
    private Pattern reNonTelInputField = Pattern.compile("<input(?=[^>]+type=\"((?!tel)[^\"]*)\")(?=[^>]+name=\"([^\"]+)\")(?=[^>]+value=\"([^\"]+)\")", Pattern.CASE_INSENSITIVE);

    // Link to home/overview - PageType.ENTRY
    private Pattern reHomeLink = Pattern.compile(
            "href=\"(core[^\"#]*)#?\"" + // The actual url (trim the '#')
            "[^>]*>" +
            "[^<]*" +
            "<img[^>]+id=\"home\"" // Identificator
            );

    private Pattern reTransactionFormContents = Pattern.compile("<form(?=[^>]+id=\"accountTransactions\")(?=[^>]+action=\"([^\"]*)\").*?>(.*?)</form>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private Pattern reAccountLink = Pattern.compile(
            "href=\"(core\\?" +
            "(?=[^\"]*usecase=accountsoverview)" +
            "(?=[^\"]*command=getcurrenttransactions)" +
            "(?=[^\"]*currentaccountsoverviewtable=([\\d]+))" +
            "[^\"]*)[^>]*>" + // End of link attributes
            "(.*?)" + // Link contents - account name
            "</a>" +
            ".*?" + // fast forward
            "([*\\d]+)" + // censured account number
            ".*?" + // fast forward
            "([\\d\\.,]+)", // account balance
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private Pattern reTransaction = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})[\n\r <].*?<td.*?>(.*?)</td>.*?<td.*?>.*?</td>.*?<td.*?>([\\s\\d-+,.]*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private Pattern reCurrency = Pattern.compile("Saldo:.*?[\\d\\.,-]+[\\s]*</td>[\\s]*<td[^>]*>([^<]*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    // The link to go to the loans overview page
    private Pattern reLoansLink = Pattern.compile("<a href=\"([^\"#]*)#?\">Lån<");
    // Link to specific loan
    private Pattern reLoanLink = Pattern.compile(
            "href=\"" +
            "(" + // Start group 1: link url
            "engine\\?" +
            "(?=[^\"]*usecase=loansoverview)" +
            "(?=[^\"]*command=get_loan_details_command)" +
            "[^\"#]*" + // Rest of link url
            ")" + // End group 1
            "#?" + // Trim off a padded #
            "[^>]*>" + // Rest of link attributes
            "(.*?)" + // Group 2: Link contents - Loan type (Eg. "Bolån")
            "</a>" +
            ".*?" + // Fast forward
            "\\*+(\\d+)" + // Group 3: Censured loan number
            ".*?" + // Fast forward
            "(\\d{4}-\\d{2}-\\d{2})" + // Group 4: "Transaction date" - Latest interest payment date
            ".*?" + // Fast forward
            "([\\d\\.,]+)", // Group 5: Loan amount
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    
    private Pattern reAccountSelect = Pattern.compile("<select[^>]+name=\"transactionaccount\"[^>]*>(.*?)</select>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private Pattern reAccountOption = Pattern.compile("<option[^>]+value=\"([\\d]+)\"[^>]*>.*?([*\\d]+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    private String lastResponse;    // Nordea has variables that needs to be sent between every single page
    private int currentPageType;	// Depending on what kind of page we're currently on, the variables will have to be retrieved differently
    
	public Nordea(Context context) {
		super(context);
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
		super.URL = BASE_URL;
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
        super.INPUT_TYPE_PASSWORD = INPUT_TYPE_PASSWORD;
        super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
	}

	public Nordea(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
		this(context);
		this.update(username, password);
	}

    @Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
		urlopen = new Urllib(context);
		Matcher matcher;
		// Find "simple login" link
		this.lastResponse = urlopen.open(LOGIN_URL);
		this.currentPageType = PageType.LOGIN;
		matcher = reSimpleLoginLink.matcher(this.lastResponse);
		if (!matcher.find()) {
			throw new BankException(res.getText(R.string.unable_to_find).toString()+" login link.");
		}
		// Visit login link
		String link = BASE_URL + matcher.group(1);
		this.lastResponse = urlopen.open(link);
		this.currentPageType = PageType.SIMPLE_LOGIN;
		matcher = reLoginFormContents.matcher(this.lastResponse);
		if (!matcher.find()) {
			throw new BankException(res.getText(R.string.unable_to_find).toString()+" login form.");
		}
		// Extract hidden fields
		String formContents = matcher.group(1);
		matcher = reNonTelInputField.matcher(formContents);
		if (!matcher.find()) {
			throw new BankException(res.getText(R.string.unable_to_find).toString()+" login fields.");
		}
		matcher.reset();
		List <NameValuePair> postData = new ArrayList <NameValuePair>();
		while (matcher.find()) {
			String name  = matcher.group(2);
			String value = matcher.group(3);
			// The non-mobile page requires javascript, so we'd best pretend we have it
			if ("JAVASCRIPT_DETECTED".equals(name)) {
				value = "true";
			}
			postData.add(new BasicNameValuePair(name, value));
		}
		// Login information
		postData.add(new BasicNameValuePair("userid", username));
		postData.add(new BasicNameValuePair("pin", password));
		// Submit button is not contained within the form and thus cannot (should not) be found with the InputField matcher
		postData.add(new BasicNameValuePair("commonlogin$loginLight", "Logga in"));
		return new LoginPackage(urlopen, postData, this.lastResponse, LOGIN_URL);
    }

	@Override
	public Urllib login() throws LoginException, BankException {
		try {
		    LoginPackage lp = preLogin();
		    this.lastResponse = urlopen.open(lp.getLoginTarget(), lp.getPostData());
		    this.currentPageType = PageType.ENTRY;
			if (this.lastResponse.contains("fel uppgifter")) {
				throw new LoginException(res.getText(R.string.invalid_username_password).toString());
			}
			
		} catch (HttpResponseException e) {
			throw new BankException(String.valueOf(e.getStatusCode()));
		} catch (ClientProtocolException e) {
			throw new BankException(e.getMessage());
		} catch (IOException e) {
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
        String link = null;
        String loanName = null;
		Matcher matcher;
		try {
			matcher = reAccountLink.matcher(this.lastResponse);
			while (matcher.find()) {
				accounts.add(new Account(
						Html.fromHtml(matcher.group(3)).toString().trim(), 
						Helpers.parseBalance(matcher.group(5)), 
						Html.fromHtml(matcher.group(4)).toString().trim()
						));
			}

            // Get loans link
            matcher = reLoansLink.matcher(this.lastResponse);
            if (matcher.find()) {
                link = matcher.group(1);
                // Go to Loans
                this.lastResponse = urlopen.open(BASE_URL + link);
                this.currentPageType = PageType.LOANS;
                matcher = reLoanLink.matcher(this.lastResponse);
                // Add loans
                while (matcher.find()) {
                    loanName = matcher.group(2) + ' ' + matcher.group(3);
                    accounts.add(new Account(
                            loanName,
                            Helpers.parseBalance(matcher.group(5)),
                            "l:"+matcher.group(3).trim(),
                            -1L,
                            Account.LOANS
                            ));
                }
            }
			// TODO: Code for funds and credit cards

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
        //accounts.add(new Account("Personkonto", Helpers.parseBalance("7953.37"), "1"));
        //accounts.add(new Account("Kapitalkonto", Helpers.parseBalance("28936.08"), "0"));

	}

	@Override
	public void updateTransactions(Account account, Urllib urlopen) throws LoginException, BankException {
		super.updateTransactions(account, urlopen);

		//No transaction history for loans, funds and credit cards.
		int accType = account.getType();
		if (accType == Account.LOANS || accType == Account.FUNDS || accType == Account.CCARD) return;

		Matcher matcher;
		try {
			// We must never browse to a random page without keeping the hashes and stuff from the current page.
			// Thus, we need to handle it separately depending on if we're still on the entry page or not.
			
			String link = null;
			List<NameValuePair> postData = new ArrayList<NameValuePair>();
			if(this.currentPageType == PageType.LOANS) {
                goHome(); // Go to PageType.ENTRY
            }
            if(currentPageType == PageType.ENTRY) {
				// Find the link to the transaction page
				matcher = reAccountLink.matcher(this.lastResponse);
				while (matcher.find()) {
					if (Html.fromHtml(matcher.group(4)).toString().trim().equals(account.getId())) {
						link = matcher.group(1);
						break;
					}
				}
				if (link == null) {
					throw new BankException(res.getText(R.string.unable_to_find).toString()+" transactions link.");
				}
			}
			else if(currentPageType == PageType.TRANSACTIONS) {
				// Find the account dropdown form
				matcher = reTransactionFormContents.matcher(this.lastResponse);
				if (!matcher.find()) {
					throw new BankException(res.getText(R.string.unable_to_find).toString()+" account form.");
				}
				link = matcher.group(1);
				matcher = reNonTextInputField.matcher(matcher.group(2));
				if (!matcher.find()) {
					throw new BankException(res.getText(R.string.unable_to_find).toString()+" input fields.");
				}
				matcher.reset();
				// Input fields
				while (matcher.find()) {
					// For some odd reason, it does not like us sending the submit button... So don't.
					if (!matcher.group(1).equals("submit")) {
						postData.add(new BasicNameValuePair(matcher.group(2), matcher.group(3)));
					}
				}
				postData.add(new BasicNameValuePair("transactionPeriod", "0"));
				// Account id
				matcher = reAccountSelect.matcher(this.lastResponse);
				if (!matcher.find()) {
					throw new BankException(res.getText(R.string.unable_to_find).toString()+" account selection.");
				}
				matcher = reAccountOption.matcher(matcher.group(1));
				String id = null;
				while (matcher.find()) {
					if(matcher.group(2).equals(account.getId())) {
						id = matcher.group(1);
						break;
					}
				}
				if (id == null) {
					throw new BankException(res.getText(R.string.unable_to_find).toString()+" account id.");
				}
				postData.add(new BasicNameValuePair("transactionaccount", id));
			}
			else {
				throw new BankException("This should never happen. If it does: Grats, you broke it.");
			}
			// Navigate to it, and parse the results
			this.lastResponse = urlopen.open(BASE_URL + link, postData);
			this.currentPageType = PageType.TRANSACTIONS;
			matcher = reTransaction.matcher(this.lastResponse);
			ArrayList<Transaction> transactions = new ArrayList<Transaction>();
			while (matcher.find() && transactions.size() < MAX_TRANSACTIONS) {
				String date = Html.fromHtml(matcher.group(1)).toString().trim();
				String text = Html.fromHtml(matcher.group(2)).toString().trim();
				BigDecimal amount = Helpers.parseBalance(matcher.group(3));
				Transaction transaction = new Transaction(date, text, amount);
				transactions.add(transaction);
			}
			account.setTransactions(transactions);
			// Currency
			matcher = reCurrency.matcher(this.lastResponse);
			if (matcher.find()) {
				account.setCurrency(Html.fromHtml(matcher.group(1)).toString().trim());
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    private void goHome() {
        String homeLink;
        Matcher matcher;
        // Find home link
        matcher = reHomeLink.matcher(this.lastResponse);
        if (matcher.find()) {
            homeLink = matcher.group(1);
            try {
                this.lastResponse = urlopen.open(BASE_URL + homeLink);
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.currentPageType = PageType.ENTRY;
        }
    }
	
	private static class PageType {
		public static final int LOGIN = 0;
		public static final int SIMPLE_LOGIN = 1;
		public static final int ENTRY = 2;
		public static final int TRANSACTIONS = 3;
		public static final int LOANS = 4;
	}
}