/*
 * Copyright (C) 2010 Nullbyte <http://nullbyte.eu>
 * Contributors: firetech
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
import java.util.Calendar;
import java.util.GregorianCalendar;
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
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;

import eu.nullbyte.android.urllib.Urllib;

public class Hemkop extends Bank {
    private static final String TAG = "Hemkop";
    private static final String NAME = "Hemköp Kundkort";
    private static final String NAME_SHORT = "hemkop";
    private static final String URL = "http://www.hemkop.se/showdoc.asp?docid=780&show=minasidor";
    private static final int BANKTYPE_ID = IBankTypes.HEMKOP;
    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;
    private static final int INPUT_TYPE_PASSWORD = InputType.TYPE_CLASS_PHONE;
    private static final String INPUT_HINT_USERNAME = "ÅÅÅÅMMDDXXXX";

    private Pattern reBalance = Pattern.compile("<span id=\"ctl00_cpTop_lblAktuelltSaldoRubrik\">(.*):</span>\\s*<strong><span id=\"ctl00_cpTop_lbl(AktuelltSaldo)\">(.*)</span></strong>");
    private Pattern reBonusIn = Pattern.compile("<span id=\"ctl00_cpTop_lblBonusInfoRubrik\">(.*):</span>\\s*<strong><span id=\"ctl00_cpTop_lbl(BonusInfo)\">(.*)</span></strong>");
    private Pattern reBonusMonth = Pattern.compile("<span id=\"ctl00_cpTop_lblBonusInfoSumRubrik\">(.*):</span>\\s*<strong><span id=\"ctl00_cpTop_lbl(BonusInfoSum)\">(.*)</span></strong>");
    private Pattern reTransaction = Pattern.compile("<tr class=\\s*\"transaction_row\">\\s*<td class=\"date\">\\s*(.*)\\s*</td>\\s*<td class=\"event\">\\s*(.*)\\s*</td>\\s*(<td class=\"currency\">\\s*(.*)\\s*</td>\\s*)?<td class=\"value\">\\s*(.*)\\s*</td>\\s*</tr>");
    // space here due to a bug on the bonus transactions page -^^^^

    private String response = null;

    public Hemkop(Context context) {
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

    public Hemkop(String username, String password, Context context) throws BankException, LoginException {
        this(context);
        this.update(username, password);
    }


    
    @Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
        urlopen = new Urllib(true);
        List <NameValuePair> postData = new ArrayList <NameValuePair>();
        postData.add(new BasicNameValuePair("hemkop_personnummer", username));
        postData.add(new BasicNameValuePair("hemkop_password", password));
        return new LoginPackage(urlopen, postData, response, "https://www.hemkop.se/scripts/cgiip.exe/WService=axfood/axfood/common/loginhemkopkundkort.p");
    }

    public Urllib login() throws LoginException, BankException {
        try {
            LoginPackage lp = preLogin();
            response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
            if (response.contains("status=failed")) {
                throw new LoginException(res.getText(R.string.invalid_username_password).toString());
            }
            //Login result contains a meta redirect to this page.
            response = urlopen.open("https://www.hemkop.se/showdoc.asp?docid=780&show=minasidor");
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
        
        ArrayList<Pattern> arrPat = new ArrayList<Pattern>();
        arrPat.add(reBalance);    // Balance (for VISA only)
        arrPat.add(reBonusIn);    // Collected bonus before this month
        arrPat.add(reBonusMonth); // Collected bonus this month
        
        for (Pattern p : arrPat) {
            Matcher matcher = p.matcher(response);
            if (matcher.find()) {
                accounts.add(new Account(Html.fromHtml(matcher.group(1)).toString().trim(), Helpers.parseBalance(matcher.group(3).trim()), matcher.group(2).trim()));
                balance = balance.add(Helpers.parseBalance(matcher.group(3)));
            }
        }

        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
        
        super.updateComplete();
    }

    @Override
    public void updateTransactions(Account account, Urllib urlopen) throws LoginException, BankException {
        super.updateTransactions(account, urlopen);
     
        try {
            String url = null;
            GregorianCalendar from = new GregorianCalendar();
            GregorianCalendar tom = new GregorianCalendar();
            if (account.getId().equals("AktuelltSaldo")) {
                // Get a year's worth of card transactions (VISA only)
                from.set(Calendar.YEAR, from.get(Calendar.YEAR) - 1);
                url = String.format("https://www.hemkop.se/showdoc.asp?docid=785&hemkop_datumFrom=%tF&hemkop_datumTom=%tF", from, tom);
            } else if (account.getId().equals("BonusInfo")) {
                // Get a year's worth of bonus transactions (shopping within Hemköp) until the current month.
                from.set(Calendar.YEAR, from.get(Calendar.YEAR) - 1);
                tom.set(Calendar.DATE, 0);
                url = String.format("https://www.hemkop.se/showdoc.asp?docid=849&hemkop_datumFrom=%tF&hemkop_datumTom=%tF", from, tom);
            } else if (account.getId().equals("BonusInfoSum")) {
                // Get a bonus transactions (shopping within Hemköp) during the current month.
                from.set(Calendar.DATE, 1);
                url = String.format("https://www.hemkop.se/showdoc.asp?docid=849&hemkop_datumFrom=%tF&hemkop_datumTom=%tF", from, tom);
            }
            
            if (url != null) {
                Log.d(TAG, "Opening "+url);
                response = urlopen.open(url);
    
                Matcher matcher = reTransaction.matcher(response);
                ArrayList<Transaction> transactions = new ArrayList<Transaction>();
                while (matcher.find()) {
                    Transaction t = new Transaction(matcher.group(1).trim(),
                            Html.fromHtml(matcher.group(2)).toString().trim(),
                            Helpers.parseBalance(matcher.group(5)));
                    if (matcher.group(4) != null && matcher.group(4).length() > 0) {
                        t.setCurrency(Html.fromHtml(matcher.group(4)).toString().trim());
                    }
                    transactions.add(t);
                }
                account.setTransactions(transactions);
            }
        } catch (ClientProtocolException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        finally {
            super.updateComplete();
        }
    }
}