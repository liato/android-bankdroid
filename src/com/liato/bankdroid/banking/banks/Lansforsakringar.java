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
import android.util.Log;

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

public class Lansforsakringar extends Bank {
    private static final String TAG = "Lansforsakringar";
    private static final String NAME = "Länsförsäkringar";
    private static final String NAME_SHORT = "lansforsakringar";
    private static final String URL = "https://secure246.lansforsakringar.se/lfportal/login/privat";
    private static final int BANKTYPE_ID = IBankTypes.LANSFORSAKRINGAR;
    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;
    private static final int INPUT_TYPE_PASSWORD = InputType.TYPE_CLASS_PHONE;
    private static final String INPUT_HINT_USERNAME = "ÅÅMMDD-XXXX";

    private Pattern reEventValidation = Pattern.compile("__EVENTVALIDATION\"\\s+value=\"([^\"]+)\"");
    private Pattern reViewState = Pattern.compile("(?:__|javax\\.faces\\.)VIEWSTATE\"\\s+.*?value=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
    private Pattern reAccountsReg = Pattern.compile("AccountNumber=([0-9]+)[^>]+><span[^>]+>([^<]+)</.*?span></td.*?<span[^>]+>([0-9 .,-]+)</span", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private Pattern reAccountsFunds = Pattern.compile("fundsDataTable[^>]+>([^<]+)</span></a></td><td[^>]+></td><td[^>]+><span\\sid=\"fundsDataTable:\\d{1,}:bankoverview_\\d{1,}_([^\"]+)\">([0-9 .,-]+)</span", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private Pattern reAccountsLoans = Pattern.compile("internalLoanDataTable[^>]+>([^<]+)</span></a></span></td><td[^>]+><span[^>]+>[^<]+</span></td><td[^>]+><span\\sid=\"internalLoanDataTable:\\d{1,}:bankoverview_\\d{1,}_([^\"]+)\">([0-9 .,-]+)</spa.*?internalLoanDataTable:\\d{1,}:bankoverview_\\d{1,}_(?:[^\"]+)\">([0-9 .,-]+)</spa", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private Pattern rePension = Pattern.compile("AvtalsID=([0-9_]+)[^<]+><span\\s*id=\"occupationalPensionDataTable:\\d{1,}:pension_overview_\\d{1,}_[^>]+>([^<]+)</span></a></span><span[^>]+>\\s*<sup>\\s*</span><span[^>]+></span><span[^>]+>\\s*</sup>\\s*</span>\\s*<table[^>]+>\\s*<tbody[^>]+></tbody></table>\\s*</td><td[^>]+><span[^>]+>([^<]+)</span>", Pattern.CASE_INSENSITIVE);
    private Pattern reToken = Pattern.compile("var\\s+token\\s*=\\s*'([^']+)'", Pattern.CASE_INSENSITIVE);
    private Pattern reUrl = Pattern.compile("<li class=\"bank\">\\s*<a href=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
    private Pattern reTransactions = Pattern.compile("td\\s*class=\"leftpadding\"[^>]+>(?:<a[^>]+>)?<span[^>]+>(\\d{4}-\\d{2}-\\d{2})</span>(?:</a>)?\\s*<a.*?</a></td><td[^>]+><span[^>]+>(\\d{4}-\\d{2}-\\d{2})</span></td><td[^>]+><span[^>]+>([^<]+)</span></td><td[^>]+><span[^>]+><span[^>]+>([^<]*)</span></span></td><td[^>]+><span[^>]+>([^<]+)</span></td><td[^>]+><span[^>]+>([^<]+)<", Pattern.CASE_INSENSITIVE);
    private String accountsUrl = null;
    private String mRequestToken = null;
    private String mViewState = null;
    private String host = null;
    private boolean mFirstTransactionPage = true;

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

    public Lansforsakringar(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
        this(context);
        this.update(username, password);
    }


    
    @Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
        urlopen = new Urllib();
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
        Log.d(TAG, "Posting to: "+urlopen.getCurrentURI());
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
                throw new BankException(res.getText(R.string.unable_to_find).toString()+" token.");
            }
            mRequestToken = matcher.group(1);

            matcher = reUrl.matcher(response);
            if (!matcher.find()) {
                throw new BankException(res.getText(R.string.unable_to_find).toString()+" accounts url.");
            }

            host = urlopen.getCurrentURI().split("/")[2];
            accountsUrl = Html.fromHtml(matcher.group(1)).toString() + "&_token=" + mRequestToken;
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
    public void update() throws BankException, LoginException, BankChoiceException {
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
                 * 4: Debt                  1,00
                 * 
                 */                
                accounts.add(new Account(Html.fromHtml(matcher.group(1)).toString().trim(), Helpers.parseBalance(matcher.group(4).trim()).negate(), matcher.group(2).trim(), Account.LOANS));
            }

            // Save token for next request
            matcher = reToken.matcher(response);
            if (!matcher.find()) {
                throw new BankException(res.getText(R.string.unable_to_find).toString()+" token.");
            }
            mRequestToken = matcher.group(1);

            response = urlopen.open("https://" + host + "/lfportal/privat.portal?_nfpb=true&_pageLabel=pension_undermenyosynlig&newUc=true&isTopLevel=true&_token=" + mRequestToken);
            matcher = rePension.matcher(response);
            while (matcher.find()) {
                /*
                 * Capture groups:
                 * GROUP                    EXAMPLE DATA
                 * 1: ID                    00835742_0
                 * 2: Name                  Avtalspension ITP - Fond
                 * 3: Amount                10 587,40
                 * 
                 */
                Account account = new Account(Html.fromHtml(matcher.group(2)).toString().trim(), Helpers.parseBalance(matcher.group(3).trim()), matcher.group(1).trim());
                account.setType(Account.OTHER);
                accounts.add(account);
            }            

            // Save token for next request
            matcher = reToken.matcher(response);
            if (!matcher.find()) {
                throw new BankException(res.getText(R.string.unable_to_find).toString()+" token.");
            }
            mRequestToken = matcher.group(1);
            
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

        if (mFirstTransactionPage) {
            try {
                response = urlopen.open("https://" + host + "/lfportal/privat.portal?_nfpb=true&_pageLabel=bank_konto&dialog=dialog:account.viewAccountTransactions&webapp=edb-account-web&stickyMenu=false&newUc=true&isPortalLogLink=true&AccountNumber=" + account.getId() + "&_token=" + mRequestToken);
                matcher = reViewState.matcher(response);
                if (!matcher.find()) {
                    Log.d(TAG,res.getText(R.string.unable_to_find).toString()+" ViewState. L237.");
                    return;
                }
                mViewState = matcher.group(1);

                matcher = reToken.matcher(response);
                if (!matcher.find()) {
                    Log.d(TAG,res.getText(R.string.unable_to_find).toString()+" token. L244.");
                    return;
                }
                mRequestToken = matcher.group(1);                  
            }
            catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }            
        }

        try {
            List <NameValuePair> postData = new ArrayList <NameValuePair>();
            if (mFirstTransactionPage) {
                postData.add(new BasicNameValuePair("dialog-account_viewAccountTransactions", "Submit Query"));            
                postData.add(new BasicNameValuePair("_token", mRequestToken));            
                postData.add(new BasicNameValuePair("loginForm_SUBMIT", "1"));            
                postData.add(new BasicNameValuePair("loginForm:_idcl", ""));            
                postData.add(new BasicNameValuePair("loginForm:_link_hidden_", ""));            
                postData.add(new BasicNameValuePair("javax.faces.ViewState", mViewState));            
                response = urlopen.open("https://" + host + "/lfportal/privat.portal?_nfpb=true&_windowLabel=account_1&_nffvid=%2Flfportal%2Findex_account.faces", postData);
                mFirstTransactionPage = false;
            }
            else {
                postData.add(new BasicNameValuePair("_token", mRequestToken));            
                postData.add(new BasicNameValuePair("viewAccountListTransactionsForm_SUBMIT", "1"));            
                postData.add(new BasicNameValuePair("viewAccountListTransactionsForm:_idcl", ""));            
                postData.add(new BasicNameValuePair("viewAccountListTransactionsForm:_link_hidden_", ""));            
                postData.add(new BasicNameValuePair("javax.faces.ViewState", mViewState));            
                postData.add(new BasicNameValuePair("accountList", account.getId()));
                response = urlopen.open("https://" + host + "/lfportal/privat.portal?_nfpb=true&_windowLabel=account_1&_nffvid=%2Flfportal%2Fjsp%2Faccount%2Fview%2FviewAccountTransactions.faces", postData);
            }
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
                transactions.add(new Transaction(matcher.group(2).trim(),
                        Html.fromHtml(matcher.group(3)).toString().trim()+(matcher.group(4).trim().length() > 0 ? " (" + Html.fromHtml(matcher.group(4)).toString().trim() + ")" : ""),
                        Helpers.parseBalance(matcher.group(5))));
            }
            account.setTransactions(transactions);

            // Save token and viewstate for next request
            matcher = reViewState.matcher(response);
            // We need the second match, disregard the first one.
            matcher.find();
            if (!matcher.find()) {
                Log.d(TAG, res.getText(R.string.unable_to_find).toString()+" ViewState. L334.");
                return;
            }
            mViewState = matcher.group(1);


            matcher = reToken.matcher(response);
            if (!matcher.find()) {
                Log.d(TAG, res.getText(R.string.unable_to_find).toString()+" token. L342.");
                return;
            }
            mRequestToken = matcher.group(1);            
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