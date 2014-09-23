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
            "([*\\d]+)" + // censured account number (account identifier)
            ".*?" + // fast forward
            "([\\d\\.,]+)", // account balance
            Pattern.DOTALL
    );
    private Pattern reTransaction = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})[\n\r <].*?<td.*?>(.*?)</td>.*?<td.*?>.*?</td>.*?<td.*?>([\\s\\d+,.-]*)", Pattern.DOTALL);
    private Pattern reCurrency = Pattern.compile("Saldo:.*?[\\d\\.,-]+[\\s]*</td>[\\s]*<td[^>]*>([^<]*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    // The link to go to the credit cards overview page
    private Pattern reCreditCardsLink = Pattern.compile("<a href=\"([^\"#]*)#?\">Kort<");
    // Link to specific credit card
    private Pattern reCreditCardLink = Pattern.compile(
            "href=\"" +
            "(" + // Start group 1: link url
            "engine\\?" +
            "(?=[^\"]*usecase=viewallcards)" +
            "(?=[^\"]*command=gettransactionscredit)" + // debit cards have "debit" - but we don't need those
            "[^\"#]*" + // Rest of link url
            ")" + // End group 1
            "[^>]*>" + // Rest of link attributes
            "(.*?)" + // Group 2: Link contents - Credit card type (Eg. "Nordea Gold")
            "</a>" +
            ".*?" + // Fast forward
            "\\*+(\\d+)", // Group 3: Censured credit card number (account identifier)
            Pattern.DOTALL
    );
    // Credit card transaction entry
    private Pattern reCreditCardTransaction = Pattern.compile(
            "(\\d{4}-\\d{2}-\\d{2})</a>" + // Group 1: Transaction date
            "[^<]*</td>" + // End date col
            "[^<]*<td[^>]*>" + // Start transaction name col
            "\\s*([^<]*)\\s*</td>" + // Group 2: (trimmed) Transaction name
            "[^<]*<td[^>]*>" + // Start recipient name col (same as transaction name?)
            "[^<]*</td>" + // Transaction name
            "[^<]*<td[^>]*>" + // Start currency col
            "\\s*([^<]*)\\s*</td>" + // Group 3: (trimmed) Currency (Empty when SEK?)
            "[^<]*<td[^>]*>" + // Start amount col
            "\\s*([\\d,.-]+)", // Group 4: Transaction amount
            Pattern.DOTALL
    );

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
            "\\*+(\\d+)" + // Group 3: Censured loan number (account identifier)
            ".*?" + // Fast forward
            "(\\d{4}-\\d{2}-\\d{2})" + // Group 4: "Transaction date" - Latest interest payment date
            ".*?" + // Fast forward
            "([\\d\\.,]+)", // Group 5: Loan amount
            Pattern.DOTALL
    );
    
    private Pattern reAccountSelect = Pattern.compile("<select[^>]+name=\"transactionaccount\"[^>]*>(.*?)</select>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private Pattern reAccountOption = Pattern.compile("<option[^>]+value=\"([\\d]+)\"[^>]*>.*?([*\\d]+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    // Nordea generates unique urls on each page load and serves content from session info,
    // so we need to find new links in lastResponse after each page load.
    private String lastResponse;
    private int currentPageType;
    
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
		
		// This puts us at PageType.ENTRY
        urlopen = login();
        String loanName;
		Matcher matcher;
		try {
			// Add regular accounts
            matcher = reAccountLink.matcher(this.lastResponse);
			while (matcher.find()) {
				accounts.add(new Account(
                        // Account name
						Html.fromHtml(matcher.group(3)).toString().trim(), 
						// Balance
                        Helpers.parseBalance(matcher.group(5)),
                        // Account identifier - half censured account number: "************1234"
						Html.fromHtml(matcher.group(4)).toString().trim()
						));
			}

            // TODO: Code for funds

            goToPage(PageType.CREDIT_CARDS);
            matcher = reCreditCardLink.matcher(this.lastResponse);
            // Add credit cards
            while (matcher.find()) {
                accounts.add(new Account(
                        // Account/Credit card name
                        matcher.group(2),
                        // Balance (not available through simple login)
                        new BigDecimal(0),
                        // Account/Credit card identifier
                        "c:" + matcher.group(3),
                        -1L,
                        Account.CCARD
                        ));
            }

            goToPage(PageType.LOANS);
            matcher = reLoanLink.matcher(this.lastResponse);
            // Add loans
            while (matcher.find()) {
                loanName = matcher.group(2) + ' ' + matcher.group(3);
                accounts.add(new Account(
                        loanName,
                        Helpers.parseBalance(matcher.group(5)),
                        "l:" + matcher.group(3).trim(),
                        -1L,
                        Account.LOANS
                ));
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
        //accounts.add(new Account("Personkonto", Helpers.parseBalance("7953.37"), "1"));
        //accounts.add(new Account("Kapitalkonto", Helpers.parseBalance("28936.08"), "0"));

	}

	@Override
	public void updateTransactions(Account account, Urllib urlopen) throws LoginException, BankException {
		super.updateTransactions(account, urlopen);

        int accType = account.getType();

        try {
            switch (accType) {
                case Account.REGULAR:
                    updateRegularTransactions(account, urlopen);
                    break;
                case Account.CCARD:
                    updateCreditTransactions(account, urlopen);
                    break;
                default:
                    break;
            }
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}

    private void goToPage(int pageType) throws ClientProtocolException, IOException {
        // Convenience method for going to an overview page
        Matcher matcher;
        String link;
        switch (pageType) {
            case PageType.ENTRY:
                // Find home link
                matcher = reHomeLink.matcher(this.lastResponse);
                break;
            case PageType.LOANS:
                // Find loans link
                matcher = reLoansLink.matcher(this.lastResponse);
                break;
            case PageType.CREDIT_CARDS:
                // Get credit cards link
                matcher = reCreditCardsLink.matcher(this.lastResponse);
                break;
            default:
                return;
        }
        // Find link to page
        if (matcher.find()) {
            link = matcher.group(1);
            // Go to page
            this.lastResponse = urlopen.open(BASE_URL + link);
            this.currentPageType = pageType;
        }
    }

    public void updateRegularTransactions(Account account, Urllib urlopen)
            throws LoginException, BankException, IOException {
        // If we're on the entry page it's easy to just find the link to the account and navigate to it,
        // If we're already on a transaction page we use the account-switching form instead of going
        // back to the entry page and starting over. This saves us 1 request.

        Matcher matcher;
        String link = null;
        List<NameValuePair> postData = new ArrayList<NameValuePair>();

        if(this.currentPageType != PageType.ENTRY && this.currentPageType != PageType.TRANSACTIONS) {
            goToPage(PageType.ENTRY);
        }
        if(currentPageType == PageType.ENTRY) {
            // Find the link to the transaction page for this account
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
            // Find account to switch to in dropdown
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

        // URL established. Either we have a simple URL parsed from ENTRY-page or a base URL +
        // a populated postData variable. This works with both.
        this.lastResponse = urlopen.open(BASE_URL + link, postData);
        this.currentPageType = PageType.TRANSACTIONS;

        // Match up transactions for this account
        matcher = reTransaction.matcher(this.lastResponse);
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        while (matcher.find() && transactions.size() < MAX_TRANSACTIONS) {
            String date = Html.fromHtml(matcher.group(1)).toString().trim();
            String text = Html.fromHtml(matcher.group(2)).toString().trim();
            BigDecimal amount = Helpers.parseBalance(matcher.group(3));
            Transaction transaction = new Transaction(date, text, amount);
            transactions.add(transaction);
        }
        // Add the transactions to this account
        account.setTransactions(transactions);
        // Set currency for this account
        matcher = reCurrency.matcher(this.lastResponse);
        if (matcher.find()) {
            account.setCurrency(Html.fromHtml(matcher.group(1)).toString().trim());
        }
    }

    public void updateCreditTransactions(Account account, Urllib urlopen)
            throws LoginException, BankException, IOException {
        Matcher matcher;
        String link = null;
        String currency = "";
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();

        if (this.currentPageType != PageType.CREDIT_CARDS) {
            goToPage(PageType.CREDIT_CARDS);
        }

        // Find the link to the transaction page for this credit card
        matcher = reCreditCardLink.matcher(this.lastResponse);
        while (matcher.find()) {
            if (("c:" + matcher.group(3)).equals(account.getId())) {
                link = matcher.group(1);
                break;
            }
        }
        if (link == null) {
            throw new BankException(res.getText(R.string.unable_to_find).toString() + " transactions link.");
        }

        this.lastResponse = urlopen.open(BASE_URL + link);
        this.currentPageType = PageType.CREDIT_CARD_TRANSACTIONS;

        matcher = reCreditCardTransaction.matcher(this.lastResponse);
        while (matcher.find() && transactions.size() < MAX_TRANSACTIONS) {
            String date = matcher.group(1);
            String text = matcher.group(2);
            currency = matcher.group(3);
            BigDecimal amount = Helpers.parseBalance(matcher.group(4));
            Transaction transaction = new Transaction(date, text, amount);
            transactions.add(transaction);
        }
        // Add the transactions to this account
        account.setTransactions(transactions);
        // Set currency for this account
        if (currency.length() > 0) {
            account.setCurrency(Html.fromHtml(matcher.group(1)).toString().trim());
        }
    }
	
	private static class PageType {
		public static final int LOGIN = 0;
		public static final int SIMPLE_LOGIN = 1;
		public static final int ENTRY = 2;
		public static final int TRANSACTIONS = 3;
		public static final int LOANS = 4;
		public static final int CREDIT_CARDS = 5;
		public static final int CREDIT_CARD_TRANSACTIONS = 6;
	}
}