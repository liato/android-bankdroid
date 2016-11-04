/*
 * Copyright (C) 2014 Nullbyte <http://nullbyte.eu>
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class Ostgotatrafiken extends Bank {

    private static final String NAME = "Östgötatrafiken";

    private static final int BANKTYPE_ID = IBankTypes.OSTGOTATRAFIKEN;

    private Pattern reViewState = Pattern.compile(
            "<input [^>]+ id=\"javax.faces.ViewState\"[^>]* value=\"([^\"]+)\"",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    private Pattern reMoreCards = Pattern.compile(
            "<li><a [^>]+ id=\"(form1cardOverviewTabs[^\"]+)\"",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    private Pattern reCardNumber = Pattern.compile(">Kortnummer: (\\d+)<",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    private Pattern reCardName = Pattern.compile("<li class=\"selected\">.*?>(\\w+?)</span>",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    private Pattern reCardBalance = Pattern.compile(">Saldo.*?>\\s*(\\d+)\\s*kr\\s*</span>",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

    private String response = null;

    public Ostgotatrafiken(Context context) {
        super(context, R.drawable.logo_ogt);
    }

    @Override
    public int getBanktypeId() {
        return BANKTYPE_ID;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public Ostgotatrafiken(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        urlopen = new Urllib(context, CertificateReader.getCertificates(context,
                R.raw.cert_ostgotatrafiken_login, R.raw.cert_ostgotatrafiken_overview));

        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("", "{\"authSource\":10," +
                                            "\"keepMeLimitedLoggedIn\":true," +
                                            "\"userName\":\"" + getUsername() + "\"," +
                                            "\"password\":\"" + getPassword() + "\"," +
                                            "\"impersonateUserName\":\"\"}"));

        return new LoginPackage(urlopen, postData, response, "https://www.ostgotatrafiken.se/ajax/Login/Attempt");
    }

    @Override
    public Urllib login() throws LoginException, BankException, IOException {
        LoginPackage lp = preLogin();
        response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
        if (!response.contains("presentationUserName")) {
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
        String cardOverviewUrl
                = "https://webtick.ostgotatrafiken.se/webtick/user/pages/CardOverview.iface";
        response = urlopen.open(cardOverviewUrl);
        parseTravelCardBalanceFromServerResponse(response);

        Matcher viewStateMatcher = reViewState.matcher(response);
        if (!viewStateMatcher.find()) {
            throw new BankException(res.getText(R.string.unable_to_find).toString() + " ViewState");
        }
        Matcher moreCardsMatcher = reMoreCards.matcher(response);
        while (moreCardsMatcher.find()) {
            List<NameValuePair> postData = new ArrayList<NameValuePair>();
            postData.add(new BasicNameValuePair("form1cardOverviewTabs:j_idcl",
                    moreCardsMatcher.group(1)));
            postData.add(new BasicNameValuePair("ice.focus", moreCardsMatcher.group(1)));
            postData.add(
                    new BasicNameValuePair(moreCardsMatcher.group(1), moreCardsMatcher.group(1)));
            postData.add(new BasicNameValuePair("form1cardOverviewTabs", "form1cardOverviewTabs"));
            postData.add(new BasicNameValuePair("ice.event.captured", moreCardsMatcher.group(1)));
            postData.add(new BasicNameValuePair("javax.faces.source", moreCardsMatcher.group(1)));
            postData.add(
                    new BasicNameValuePair("javax.faces.ViewState", viewStateMatcher.group(1)));

            postData.add(new BasicNameValuePair("icefacesCssUpdates", ""));
            postData.add(new BasicNameValuePair("javax.faces.partial.event", "click"));
            postData.add(new BasicNameValuePair("javax.faces.partial.execute", "@all"));
            postData.add(new BasicNameValuePair("javax.faces.partial.render", "@all"));
            postData.add(new BasicNameValuePair("ice.event.type", "onclick"));
            postData.add(new BasicNameValuePair("ice.event.alt", "false"));
            postData.add(new BasicNameValuePair("ice.event.ctrl", "false"));
            postData.add(new BasicNameValuePair("ice.event.shift", "false"));
            postData.add(new BasicNameValuePair("ice.event.meta", "false"));
            postData.add(new BasicNameValuePair("ice.event.x", "606"));
            postData.add(new BasicNameValuePair("ice.event.y", "362"));
            postData.add(new BasicNameValuePair("ice.event.left", "true"));
            postData.add(new BasicNameValuePair("ice.event.right", "false"));
            postData.add(new BasicNameValuePair("ice.submit.type", "ice.s"));
            postData.add(new BasicNameValuePair("ice.submit.serialization", "form"));
            postData.add(new BasicNameValuePair("javax.faces.partial.ajax", "true"));

            // ice.event.target is sent by browser, but not needed by
            // server so don't bother parsing response for its correct value
            //postData.add(new BasicNameValuePair("ice.event.target", "form1cardOverviewTabs:j_idt240:1:j_idt243"));

            // ice.window and ice.view are sent by browser, but by not sending
            // these to server we get an HTML response which can be parsed
            // just like the initial response. If including ice.window and
            // ice.view in POST the server will give us XML data back which
            // would need separate parsing.
            //postData.add(new BasicNameValuePair("ice.window", "p7htbwx9t8"));
            //postData.add(new BasicNameValuePair("ice.view", "vcuag6esom"));

            urlopen.addHeader("Faces-Request", "partial/ajax");
            response = urlopen.open(cardOverviewUrl, postData);
            parseTravelCardBalanceFromServerResponse(response);
        }

        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
        super.updateComplete();
    }

    private void parseTravelCardBalanceFromServerResponse(String response) {
        Matcher cardNameMatcher = reCardName.matcher(response);
        Matcher cardNumberMatcher = reCardNumber.matcher(response);
        Matcher balanceMatcher = reCardBalance.matcher(response);

        if (cardNameMatcher.find() && cardNumberMatcher.find() && balanceMatcher.find()) {
            String cardName = cardNameMatcher.group(1);
            String cardNumber = cardNumberMatcher.group(1);
            String cardBalance = balanceMatcher.group(1);

            accounts.add(new Account(cardName, Helpers.parseBalance(cardBalance), cardNumber));
            balance = balance.add(Helpers.parseBalance(cardBalance));
        }
    }
}
