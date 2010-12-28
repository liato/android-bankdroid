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

import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;

import eu.nullbyte.android.urllib.Urllib;

public class Jojo extends Bank {
    private static final String TAG = "Jojo";
    private static final String NAME = "Jojo Reskassa";
    private static final String NAME_SHORT = "jojo";
    private static final String URL = "https://www.skanetrafiken.se/templates/MSRootPage.aspx?id=2935&epslanguage=SV";
    private static final int BANKTYPE_ID = Bank.JOJO;

    private Pattern reViewState = Pattern.compile("__VIEWSTATE\"\\s+value=\"([^\"]+)\"");
    private Pattern reAccounts = Pattern.compile("1_mRepeaterMyCards_ctl(\\d{2,3})_LinkButton\\d{1,3}\"[^>]+>([^<]+)</a>\\s*</td>\\s*<td[^>]+>\\s*<a\\s*id=\"ctl00_fullRegion_mainRegion_CardInformation1_mRepeaterMyCards_ctl\\d{2,3}_LinkButton\\d{1,3}\"[^>]+>([^<]+)</a>", Pattern.CASE_INSENSITIVE);
    private Pattern reBalance = Pattern.compile("labelsaldoinfo\">([^<]+)<", Pattern.CASE_INSENSITIVE);
    private String response = null;

    public Jojo(Context context) {
        super(context);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
    }

    public Jojo(String username, String password, Context context) throws BankException, LoginException {
        this(context);
        this.update(username, password);
    }


    @Override
    protected LoginPackage preLogin() throws BankException,
    ClientProtocolException, IOException {
        urlopen = new Urllib(true);
        response = urlopen.open("http://www.skanetrafiken.se/templates/StartPage.aspx?id=2182&epslanguage=SV");
        Matcher matcher = reViewState.matcher(response);
        if (!matcher.find()) {
            throw new BankException(res.getText(R.string.unable_to_find).toString()+" ViewState.");
        }
        String strViewState = matcher.group(1);
        List <NameValuePair> postData = new ArrayList <NameValuePair>();
        postData.add(new BasicNameValuePair("__EVENTTARGET", ""));
        postData.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
        postData.add(new BasicNameValuePair("__VIEWSTATE", strViewState));
        postData.add(new BasicNameValuePair("ctl00$LoginBlob1$Username", username));
        postData.add(new BasicNameValuePair("ctl00$LoginBlob1$Password", password));
        postData.add(new BasicNameValuePair("ctl00$LoginBlob1$LoginButton", "Logga in"));

        return new LoginPackage(urlopen, postData, response, "https://www.skanetrafiken.se/templates/StartPage.aspx?id=2182&epslanguage=SV");
    }

    public Urllib login() throws LoginException, BankException {
        try {
            LoginPackage lp = preLogin();
            response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
            if (response.contains("Inloggningen misslyckades")) {
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
    public void update() throws BankException, LoginException {
        super.update();
        if (username == null || password == null || username.length() == 0 || password.length() == 0) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }
        urlopen = login();
        try {
            response = urlopen.open("https://www.skanetrafiken.se/templates/CardInformation.aspx?id=26957&epslanguage=SV");
            Matcher matcher;
            Matcher matcher_b;
            matcher = reViewState.matcher(response);
            if (!matcher.find()) {
                throw new BankException(res.getText(R.string.unable_to_find).toString()+" ViewState.");
            }			
            String strViewState = matcher.group(1);

            matcher = reAccounts.matcher(response);
            while (matcher.find()) {
                /*
                 * Capture groups:
                 * GROUP                EXAMPLE DATA
                 * 1: ID                01
                 * 2: Name              Nytt
                 * 3: Card number       1111111111
                 * 
                 */

                List <NameValuePair> postData = new ArrayList <NameValuePair>();
                postData.add(new BasicNameValuePair("__EVENTTARGET", ""));
                postData.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
                postData.add(new BasicNameValuePair("__VIEWSTATE", strViewState));
                postData.add(new BasicNameValuePair("ctl00$fullRegion$mainRegion$CardInformation1$mRepeaterMyCards$ctl" + matcher.group(1) + "$Button", "Kortinfo"));
                String accinfo = urlopen.open("https://www.skanetrafiken.se/templates/CardInformation.aspx?id=26957&epslanguage=SV", postData);

                matcher_b = reBalance.matcher(accinfo);
                if (matcher_b.find()) {
                    /*
                     * Capture groups:
                     * GROUP                EXAMPLE DATA
                     * 1: Amount            592,80 kr
                     * 
                     */

                    accounts.add(new Account(Html.fromHtml(matcher.group(2)).toString().trim() , Helpers.parseBalance(matcher_b.group(1)), matcher.group(1)));
                    balance = balance.add(Helpers.parseBalance(matcher_b.group(1)));
                }
            }

            if (accounts.isEmpty()) {
                throw new BankException(res.getText(R.string.no_accounts_found).toString());
            }
        }
        catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }		
        finally {
            super.updateComplete();
        }
    }
}
