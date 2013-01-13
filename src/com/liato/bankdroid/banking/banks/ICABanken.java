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
import java.util.HashMap;
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

public class ICABanken extends Bank {
    private static final String TAG = "ICABanken";
    private static final String NAME = "ICA Banken";
    private static final String NAME_SHORT = "icabanken";
    private static final String URL = "https://mobil.icabanken.se/";
    private static final int BANKTYPE_ID = IBankTypes.ICABANKEN;
    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;
    private static final int INPUT_TYPE_PASSWORD = InputType.TYPE_CLASS_PHONE;
    private static final String INPUT_HINT_USERNAME = "ÅÅMMDD-XXXX";
    private static final boolean STATIC_BALANCE = false;

    private Pattern reEventValidation = Pattern.compile("__EVENTVALIDATION\"\\s+value=\"([^\"]+)\"");
    private Pattern reViewState = Pattern.compile("__VIEWSTATE\"\\s+value=\"([^\"]+)\"");
    private Pattern reError = Pattern.compile("<label\\s+class=\"error\">(.+?)</label>",Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private Pattern reBalance = Pattern.compile("account\\.aspx\\?id=([^\"]+).+?>([^<]+)</a.+?Disponibelt([0-9 .,-]+)[^<]*<br/>.+?Saldo([0-9 .,-]+)", Pattern.CASE_INSENSITIVE);
    private Pattern reTransactions = Pattern.compile("<label>(.+?)</label>\\s*<[^>]+(.+?)</div>\\s*<[^>]+>-\\s*Belopp(.+?)<", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private HashMap<String, String> mIdMappings = new HashMap<String, String>();

    public ICABanken(Context context) {
        super(context);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
        super.INPUT_TYPE_PASSWORD = INPUT_TYPE_PASSWORD;
        super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
        super.STATIC_BALANCE = STATIC_BALANCE;
    }

    public ICABanken(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
        this(context);
        this.update(username, password);
    }

    
    @Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
        urlopen = new Urllib();
        String response = urlopen.open("https://mobil2.icabanken.se/login/login.aspx");
        Matcher matcher = reViewState.matcher(response);
        if (!matcher.find()) {
            throw new BankException(res.getText(R.string.unable_to_find).toString()+" viewstate.");
        }
        String strViewState = matcher.group(1);
        matcher = reEventValidation.matcher(response);
        if (!matcher.find()) {
            throw new BankException(res.getText(R.string.unable_to_find).toString()+" eventvalidation.");
        }
        String strEventValidation = matcher.group(1);
        List <NameValuePair> postData = new ArrayList <NameValuePair>();
        postData.add(new BasicNameValuePair("pnr_phone", username));
        postData.add(new BasicNameValuePair("pwd_phone", password));
        postData.add(new BasicNameValuePair("btnLogin", "Logga in"));
        postData.add(new BasicNameValuePair("__VIEWSTATE", strViewState));
        postData.add(new BasicNameValuePair("__EVENTVALIDATION", strEventValidation));
        return new LoginPackage(urlopen, postData, response, "https://mobil2.icabanken.se/login/login.aspx");
    }

    public Urllib login() throws LoginException, BankException {
        try {
            LoginPackage lp = preLogin();
            String response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
            Matcher matcher = reError.matcher(response);
            if (matcher.find()) {
                String errormsg = Html.fromHtml(matcher.group(1).trim()).toString();
                if (errormsg.contains("ord eller personnummer") || errormsg.contains("et alternativ") || errormsg.contains("fyra siffror")) {
                    throw new LoginException(errormsg);    
                }
                else {
                     throw new BankException(errormsg);    
                }
            }
        }
        catch (ClientProtocolException e) {
            Log.e(TAG, "ClientProtocolException: "+e.getMessage());
            throw new BankException(e.getMessage());
        }
        catch (IOException e) {
            Log.e(TAG, "IOException: "+e.getMessage());
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
            response = urlopen.open("https://mobil2.icabanken.se/account/overview.aspx");
            matcher = reBalance.matcher(response);
            int accid = 0;
            while (matcher.find()) {
                /*
                 * Capture groups:
                 * GROUP                EXAMPLE DATA
                 * 1: ID                0000000000 - not static?
                 * 2: Name              ICA KONTO
                 * 3: Disponibelt       00.000,00
                 * 4: Saldo             1.655,71
                 *  
                 */                
                mIdMappings.put(Integer.toString(accid), matcher.group(1).trim());           
                accounts.add(new Account(Html.fromHtml(matcher.group(2)).toString().trim() + " (Disponibelt)", Helpers.parseBalance(matcher.group(3).trim()), Integer.toString(accid)));
                Account account = new Account(Html.fromHtml(matcher.group(2)).toString().trim() + " (Saldo)", Helpers.parseBalance(matcher.group(4).trim()), "a:" + accid);
                account.setAliasfor(Integer.toString(accid));
                accounts.add(account);

                balance = balance.add(Helpers.parseBalance(matcher.group(3)));
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
    }

    @Override
    public void updateTransactions(Account account, Urllib urlopen) throws LoginException, BankException {
        super.updateTransactions(account, urlopen);
        if (account.getId().startsWith("a:") || !mIdMappings.containsKey(account.getId())) return; // No transactions for "saldo"-accounts
        String accountId = mIdMappings.get(account.getId());
        String response = null;
        Matcher matcher;
        try {
            response = urlopen.open("https://mobil2.icabanken.se/account/account.aspx?id="+accountId);
            matcher = reTransactions.matcher(response);
            ArrayList<Transaction> transactions = new ArrayList<Transaction>();
            while (matcher.find()) {
                transactions.add(new Transaction(matcher.group(2).trim().substring(8), Html.fromHtml(matcher.group(1)).toString().trim(), Helpers.parseBalance(matcher.group(3))));
            }
            account.setTransactions(transactions);
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