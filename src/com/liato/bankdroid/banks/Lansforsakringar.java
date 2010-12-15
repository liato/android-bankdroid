package com.liato.bankdroid.banks;

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
import android.util.Log;

import com.liato.bankdroid.Account;
import com.liato.bankdroid.Bank;
import com.liato.bankdroid.BankException;
import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.LoginException;
import com.liato.bankdroid.R;
import com.liato.bankdroid.Transaction;
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
	private Pattern reAccountsReg = Pattern.compile("AccountNumber=([0-9]+)[^>]+><span[^>]+>([^<]+)</.*?span></td.*?<span[^>]+>([0-9 .,-]+)</span", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private Pattern reAccountsFunds = Pattern.compile("fundsDataTable[^>]+>([^<]+)</span></a></td><td[^>]+></td><td[^>]+><span\\sid=\"fundsDataTable:\\d{1,}:bankoverview_\\d{1,}_([^\"]+)\">([0-9 .,-]+)</span", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private Pattern reAccountsLoans = Pattern.compile("internalLoanDataTable[^>]+>([^<]+)</span></a></span></td><td[^>]+><span[^>]+>[^<]+</span></td><td[^>]+><span\\sid=\"internalLoanDataTable:\\d{1,}:bankoverview_\\d{1,}_([^\"]+)\">([0-9 .,-]+)</spa", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private Pattern reToken = Pattern.compile("var\\s+token\\s*=\\s*'([^']+)'", Pattern.CASE_INSENSITIVE);
    private Pattern reUrl = Pattern.compile("<li class=\"bank\">\\s*<a href=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
    private Pattern reTransactions = Pattern.compile("td\\s*class=\"leftpadding\"[^>]+><span[^>]+>(\\d{4}-\\d{2}-\\d{2})</span>\\s*<a.*?</a></td><td[^>]+><span[^>]+>(\\d{4}-\\d{2}-\\d{2})</span></td><td[^>]+><span[^>]+>([^<]+)</span></td><td[^>]+><span[^>]+><span[^>]+>([^<]*)</span></span></td><td[^>]+><span[^>]+>([^<]+)</span></td><td[^>]+><span[^>]+>([^<]+)<", Pattern.CASE_INSENSITIVE);
	private String accountsUrl = null;
	private String token = null;
	private String host = null;
	
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
			response = urlopen.open(urlopen.getCurrentURI(), postData);

			if (response.contains("Felaktig inloggning")) {
				throw new LoginException(res.getText(R.string.invalid_username_password).toString());
			}

			matcher = reToken.matcher(response);
			if (!matcher.find()) {
				throw new BankException(res.getText(R.string.unable_to_find).toString()+" token.");
			}
			token = matcher.group(1);

			matcher = reUrl.matcher(response);
			if (!matcher.find()) {
				throw new BankException(res.getText(R.string.unable_to_find).toString()+" accounts url.");
			}
			
			host = urlopen.getCurrentURI().split("/")[2];
			accountsUrl = Html.fromHtml(matcher.group(1)).toString() + "&_token=" + token;
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
			matcher = reAccountsReg.matcher(response);
			while (matcher.find()) {
                /*
                 * Capture groups:
                 * GROUP                    EXAMPLE DATA
                 * 1: Account number        125486547
                 * 2: Name                  Personkonto
                 * 3: Amount                25 000 000
                 * 
                 */    
			    accounts.add(new Account(Html.fromHtml(matcher.group(2)).toString().trim(), Helpers.parseBalance(matcher.group(3).trim()), matcher.group(1).trim()));
				balance = balance.add(Helpers.parseBalance(matcher.group(3)));
			}
            matcher = reAccountsFunds.matcher(response);
            while (matcher.find()) {
                /*
                 * Capture groups:
                 * GROUP                    EXAMPLE DATA
                 * 1: Name                  Fonder
                 * 2: ID                    idJsp165
                 * 3: Amount                0,00
                 * 
                 */
                accounts.add(new Account(Html.fromHtml(matcher.group(1)).toString().trim(), Helpers.parseBalance(matcher.group(3).trim()), matcher.group(2).trim(), Account.FUNDS));
            }
            matcher = reAccountsLoans.matcher(response);
            while (matcher.find()) {
                /*
                 * Capture groups:
                 * GROUP                    EXAMPLE DATA
                 * 1: Name                  Privatl&#229;n
                 * 2: ID                    idJsp207
                 * 3: Amount                25 000 000
                 * 
                 */                
                accounts.add(new Account(Html.fromHtml(matcher.group(1)).toString().trim(), Helpers.parseBalance(matcher.group(3).trim()), matcher.group(2).trim(), Account.LOANS));
            }

            // Save token for next request
            matcher = reToken.matcher(response);
            if (!matcher.find()) {
                throw new BankException(res.getText(R.string.unable_to_find).toString()+" token.");
            }
            token = matcher.group(1);
            
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
	
    @Override
    public void updateTransactions(Account account, Urllib urlopen) throws LoginException, BankException {
        super.updateTransactions(account, urlopen);
        // No transaction history for funds and loans
        if (account.getType() != Account.REGULAR) return;
        String response = null;
        Matcher matcher;
        try {
            response = urlopen.open("https://" + host + "/lfportal/appmanager/privat/main?_nfpb=true&_pageLabel=bank_konto&dialog=dialog:account.viewAccountTransactions&webapp=edb-account-web&stickyMenu=false&newUc=true&AccountNumber=" + account.getId() + "&_token=" + token);
            matcher = reTransactions.matcher(response);
            ArrayList<Transaction> transactions = new ArrayList<Transaction>();
            while (matcher.find()) {
                /*
                 * Capture groups:
                 * GROUP                EXAMPLE DATA
                 * 1: Book. date        2009-05-03
                 * 2: Trans. date       2009-05-03
                 * 3: Specification     &Ouml;verf&ouml;ring internet ...
                 * 4: Note              829909945928712
                 * 5: Amount            -54,00
                 * 6: Remaining         0,00
                 *   
                 */                    
                transactions.add(new Transaction(matcher.group(2).trim().substring(8),
                                    Html.fromHtml(matcher.group(3)).toString().trim()+(matcher.group(4).trim().length() > 0 ? " (" + Html.fromHtml(matcher.group(3)).toString().trim() + ")" : ""),
                                    Helpers.parseBalance(matcher.group(5))));
            }
            account.setTransactions(transactions);
            
            // Save token for next request
            matcher = reToken.matcher(response);
            if (!matcher.find()) {
                throw new BankException(res.getText(R.string.unable_to_find).toString()+" token.");
            }
            token = matcher.group(1);            
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            super.updateComplete();
        }
    }       	
}