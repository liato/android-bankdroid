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

import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.legacy.R;
import com.liato.bankdroid.provider.IBankTypes;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.text.Html;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class Vasttrafik extends Bank {

    private static final String TAG = "Västtrafik";

    private static final String NAME = "Västtrafik";

    private static final String NAME_SHORT = "vasttrafik";

    private static final String URL = "https://www.vasttrafik.se/mina-sidor/";

    private static final int BANKTYPE_ID = IBankTypes.VASTTRAFIK;

    private Pattern reViewState = Pattern.compile("__VIEWSTATE\"\\s+value=\"([^\"]+)\"");

    private Pattern reAccounts = Pattern
            .compile("<h3 class=\"cardName\">(.*?)</h3>(.*?)<span class=\"isAccount hidden\">",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    private Pattern reBalance = Pattern.compile(
            "<span class=\"chargeType\"><span class='col1'>(.*?):</span><span class='col2 boldType'>(.*?)</span></span>",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    private String response = null;

    public Vasttrafik(Context context) {
        super(context);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
    }

    public Vasttrafik(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        urlopen = new Urllib(context,
                CertificateReader.getCertificates(context, R.raw.cert_vasttrafik));
        response = urlopen.open("https://www.vasttrafik.se/mina-sidor/logga-in/");

        Matcher matcher = reViewState.matcher(response);
        if (!matcher.find()) {
            throw new BankException(
                    res.getText(R.string.unable_to_find).toString() + " ViewState.");
        }
        String strViewState = matcher.group(1);

        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("__VIEWSTATE", strViewState));
        postData.add(new BasicNameValuePair(
                "ctl00$ctl00$SiteRegion$SiteRegion$ContentRegion$MainContentRegion$AddContentRegion$ctl00$TextBoxUserName",
                getUsername()));
        postData.add(new BasicNameValuePair(
                "ctl00$ctl00$SiteRegion$SiteRegion$ContentRegion$MainContentRegion$AddContentRegion$ctl00$TextBoxPassword",
                getPassword()));
        postData.add(new BasicNameValuePair(
                "ctl00$ctl00$SiteRegion$SiteRegion$ContentRegion$MainContentRegion$AddContentRegion$ctl00$CheckBoxPersistent",
                "on"));
        postData.add(new BasicNameValuePair(
                "ctl00$ctl00$SiteRegion$SiteRegion$ContentRegion$MainContentRegion$AddContentRegion$ctl00$ButtonLogin",
                "Logga in"));

        return new LoginPackage(urlopen, postData, response,
                "https://www.vasttrafik.se/mina-sidor/logga-in/?ReturnUrl=/mina-sidor-inloggad/mina-kort/");
    }

    @Override
    public Urllib login() throws LoginException, BankException, IOException {
        LoginPackage lp = preLogin();
        response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
        if (!response.contains("<span class=\"loggedInAs\">")) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }
        return urlopen;
    }

    @Override
    public void update() throws BankException, LoginException, BankChoiceException, IOException {
        super.update();
        if (getUsername().isEmpty() || getPassword().isEmpty()) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }
        urlopen = login();
        response = urlopen.open("https://www.vasttrafik.se/mina-sidor-inloggad/mina-kort/");
        Matcher matcher;
        Matcher matcher_b;

        matcher = reAccounts.matcher(response);
        while (matcher.find()) {
            /*
             * Capture groups:
             * GROUP                EXAMPLE DATA
             * 1: Name              Nytt
             * 2: Balance information
             */

            if ("".equals(matcher.group(1))) {
                continue;
            }

            matcher_b = reBalance.matcher(matcher.group(2));
            if (matcher_b.find()) {
                /*
                 * Capture groups:
                 * GROUP                EXAMPLE DATA
                 * 1: Type              Kontoladdning
                 * 2: Amount            592,80 kr
                 */

                String balanceString = matcher_b.group(2).replaceAll("\\<a[^>]*>", "")
                        .replaceAll("\\<[^>]*>", "").trim();

                accounts.add(new Account(Html.fromHtml(matcher.group(1)).toString().trim(),
                        Helpers.parseBalance(balanceString), matcher.group(1)));
                balance = balance.add(Helpers.parseBalance(balanceString));
            }
        }

        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
        super.updateComplete();
    }
}
