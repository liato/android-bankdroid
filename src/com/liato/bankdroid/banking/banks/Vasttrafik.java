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
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;

import eu.nullbyte.android.urllib.Urllib;

public class Vasttrafik extends Bank {
    private static final String TAG = "Västtrafik";
    private static final String NAME = "Västtrafik";
    private static final String NAME_SHORT = "vasttrafik";
    private static final String URL = "http://vasttrafik.se/Mina-Sidor/";
    private static final int BANKTYPE_ID = IBankTypes.VASTTRAFIK;

    private Pattern reViewState = Pattern.compile("__VIEWSTATE\"\\s+value=\"([^\"]+)\"");
	private Pattern reAccounts = Pattern.compile("<td class=\"HeadingTop Col2of3\">\\s+([^<]+)\\s+</td>.*?<td class=\"Col2of3\">\\s+([^<]+)\\s+</td>.*?<div id=\"ctl00_FullRegion_MainAndFooterRegion_MainRegion_HandleCardsFormControl_TabContainerCards_TabPanelCards_ListViewActiveCards_ctrl\\d{0,3}_PanelDetail\" class=\"RowColor collapsePanel\" style=\"height:0px;\">(.*?)</div>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);;
	private Pattern reBalance = Pattern.compile("<tr class=\"(RowColor|AlternatingColor)\">\\s+<td>\\s+Kontoladdning\\s+</td>.*?<td>(.*?)<br />.*?</td>.*?</tr>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private String response = null;

    public Vasttrafik(Context context) {
        super(context);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
    }

    public Vasttrafik(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
        this(context);
        this.update(username, password);
    }


    @Override
    protected LoginPackage preLogin() throws BankException,
    ClientProtocolException, IOException {
        urlopen = new Urllib(true);
        response = urlopen.open("https://www.vasttrafik.se/CustomerUtil/Common/Security/Login.aspx");
        Matcher matcher = reViewState.matcher(response);
        if (!matcher.find()) {
            throw new BankException(res.getText(R.string.unable_to_find).toString()+" ViewState.");
        }
        String strViewState = matcher.group(1);
        List <NameValuePair> postData = new ArrayList <NameValuePair>();
		postData.add(new BasicNameValuePair("__EVENTTARGET", ""));
		postData.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
		postData.add(new BasicNameValuePair("__VIEWSTATE", strViewState));
		postData.add(new BasicNameValuePair("LoginFormControl$TextBoxUsername", username));
		postData.add(new BasicNameValuePair("LoginFormControl$TextBoxPassword", password));
		postData.add(new BasicNameValuePair("LoginFormControl_CheckBoxPersistantLogin", ""));
		postData.add(new BasicNameValuePair("LoginFormControl$LoginButton", "Logga in"));

        return new LoginPackage(urlopen, postData, response, "https://www.vasttrafik.se/CustomerUtil/Common/Security/Login.aspx");
    }

    public Urllib login() throws LoginException, BankException {
        try {
            LoginPackage lp = preLogin();
            response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
            if (response.contains("Felaktig inloggning")) {
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
        try {
            response = urlopen.open("https://www.vasttrafik.se/sv/Mina-sidor-inloggad/Mina-Vasttrafikskort/");
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
                 * 1: Name              Nytt
                 * 2: Card number       1111111111
                 * 3: Balance information
                 * 
                 */

                matcher_b = reBalance.matcher(matcher.group(3));
                if (matcher_b.find()) {
                    /*
                     * Capture groups:
                     * GROUP                EXAMPLE DATA
                     * 2: Amount            592,80 kr
                     * 
                     */

					String balanceString = matcher_b.group(2).replaceAll("\\<a[^>]*>","").replaceAll("\\<[^>]*>","").trim();
						
                    accounts.add(new Account(Html.fromHtml(matcher.group(1)).toString().trim() , Helpers.parseBalance(balanceString), matcher.group(2)));
                    balance = balance.add(Helpers.parseBalance(balanceString));
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
