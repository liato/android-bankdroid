/*
 * Copyright (C) 2011 Nullbyte <http://nullbyte.eu>
 * Copyright (C) 2011, 2012 Andreas Gunnerås
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
import android.text.InputType;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.nullbyte.android.urllib.Urllib;

public class Everydaycard extends Bank {

    private static final String NAME = "Everydaycard";

    private static final String NAME_SHORT = "everydaycard";

    private static final String URL = "http://www.everydaycard.se/mobil/";

    private static final int BANKTYPE_ID = IBankTypes.EVERYDAYCARD;

    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;

    private static final String INPUT_HINT_USERNAME = "ÅÅMMDDXXXX";

    private Pattern reSaldo = Pattern.compile(
            "Utnyttjad kredit \\(sek\\)</td>\\s*<td></td>\\s*<td>([^<]+)<",
            Pattern.CASE_INSENSITIVE);

    private Pattern reBonus = Pattern.compile(
            "Aktuell bonus \\(sek\\)</td>\\s*<td>.*</td>\\s*<td>([^<]+)<",
            Pattern.CASE_INSENSITIVE);

    private String response = null;

    public Everydaycard(Context context) {
        super(context, R.drawable.logo_everydaycard);
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
        super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
    }

    public Everydaycard(String username, String password, Context context)
            throws BankException, LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        return preLoginInternal("http://valuta.g2solutions.se/mobil/web/logonSubmit.do");
    }

    private LoginPackage preLoginInternal(String url) throws BankException, IOException {
        urlopen = new Urllib(context);
        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("nextPage", "firstPage"));
        postData.add(new BasicNameValuePair("username", getUsername()));
        postData.add(new BasicNameValuePair("password", getPassword()));
        return new LoginPackage(urlopen, postData, response, url);
    }

    @Override
    public Urllib login() throws LoginException, BankException, IOException {
        LoginPackage lp = preLogin();
        response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
        if (response.contains("Felaktigt Login")) {
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

        Matcher matcher = reBonus.matcher(response);
        if (matcher.find()) {
            BigDecimal bonusBalance = Helpers.parseBalance(matcher.group(1));
            Account account = new Account("Bonus", bonusBalance, "Bonus", Account.OTHER);
            balance = balance.add(bonusBalance);
            accounts.add(account);
        }

        matcher = reSaldo.matcher(response);
        if (matcher.find()) {
            BigDecimal accountBalance = Helpers.parseBalance(matcher.group(1)).negate();
            Account account = new Account("Everydaycard", accountBalance, "1", Account.CCARD);
            balance = balance.add(accountBalance);
            accounts.add(account);
        }
        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
        super.updateComplete();
    }
}
