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
import com.liato.bankdroid.banking.Transaction;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.legacy.R;
import com.liato.bankdroid.provider.IBankTypes;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.text.Html;
import android.text.InputType;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class CSN extends Bank {

    private static final String TAG = "CSN";

    private static final String NAME = "CSN";

    private static final String NAME_SHORT = "csn";

    private static final String URL = "https://www.csn.se/bas/inloggning/pinkod.do";

    private static final int BANKTYPE_ID = IBankTypes.CSN;

    private static final int INPUT_TYPE_PASSWORD = InputType.TYPE_CLASS_PHONE;

    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;

    private static final String INPUT_HINT_USERNAME = "ÅÅMMDDXXXX";

    private static final boolean STATIC_BALANCE = true;

    private Pattern reLoginError = Pattern.compile("<h3>Observera</h3>\\s*<ul>\\s*<li>([^<]+)</li>",
            Pattern.CASE_INSENSITIVE);

    private Pattern reBalance = Pattern.compile(
            "aktuellStudieskuld\\.do\\?metod=init&(?:amp;)?SpecNr=(\\d{1,})\">([^<]+)</a>\\s*</td>\\s*<td[^>]+>([^<]+)</td>",
            Pattern.CASE_INSENSITIVE);

    private Pattern reTransactions = Pattern.compile(
            "<td>\\s*(\\d{4}-\\d{2}-\\d{2})\\s*</td>\\s*<td>([^<]+)</td>\\s*<td>([^<]+)</td>.*?startHideInfoBoxTimer\\(\\d{1,}\\);\">([^<]+)</",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private Pattern reCompletedPayments = Pattern.compile(
            "<td>\\s*(\\d{4}-\\d{2}-\\d{2})\\s*</td>\\s*<td>([^<]+)</td>.*?startHideInfoBoxTimer\\(\\d{1,}\\);\"[^>]+>([^<]+)</",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private String response = null;

    public CSN(Context context) {
        super(context);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
        super.INPUT_TYPE_PASSWORD = INPUT_TYPE_PASSWORD;
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
        super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
        super.STATIC_BALANCE = STATIC_BALANCE;
    }

    public CSN(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }


    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        urlopen = new Urllib(context, CertificateReader.getCertificates(context, R.raw.cert_csn));
        urlopen.setAllowCircularRedirects(true);
        urlopen.setContentCharset(HTTP.ISO_8859_1);
        urlopen.addHeader("Referer", "https://www.csn.se/bas/");
        response = urlopen.open("https://www.csn.se/bas/inloggning/pinkod.do");
        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("javascript", "on"));

        response = urlopen.open("https://www.csn.se/bas/javascript", postData);

        postData.clear();

        postData.add(new BasicNameValuePair("metod", "validerapinkod"));
        postData.add(new BasicNameValuePair("pnr", getUsername()));
        postData.add(new BasicNameValuePair("pinkod", getPassword()));
        return new LoginPackage(urlopen, postData, response,
                "https://www.csn.se/bas/inloggning/Pinkod.do");
    }

    @Override
    public Urllib login() throws LoginException, BankException, IOException {
        LoginPackage lp = preLogin();
        response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
        Matcher matcher = reLoginError.matcher(response);
        if (matcher.find()) {
            throw new LoginException(Html.fromHtml(matcher.group(1)).toString().trim());
        }
        if (!response.contains("Inloggad&nbsp;som")) {
            throw new BankException(res.getText(R.string.unable_to_login).toString());
        }
        return urlopen;
    }

    @Override
    public void update() throws BankException, LoginException, BankChoiceException, IOException {
        super.update();
        if (getUsername() == null || getPassword() == null || getUsername().length() == 0
                || getPassword().length() == 0) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }
        urlopen = login();

        response = urlopen.open(
                "https://www.csn.se/aterbetalning/hurStorArMinSkuld/aktuellStudieskuld.do?javascript=off");
        Matcher matcher;
        matcher = reBalance.matcher(response);
        int i = 0;
        while (matcher.find()) {
            /*
             * Capture groups:
             * GROUP                EXAMPLE DATA
             * 1: ID                0
             * 2: Name              Lån efter 30 juni 2001 (annuitetslån)
             * 3: Amount            123,456
             *
             */
            BigDecimal amount = Helpers.parseBalance(matcher.group(3).replace(",", "")).negate();
            Account account = new Account(
                    Html.fromHtml(matcher.group(2)).toString().trim(),
                    amount,
                    matcher.group(1).trim(),
                    Account.LOANS);
            if (i > 0) {
                account.setAliasfor("0");
            }
            accounts.add(account);
            balance = balance.add(amount);
            i++;
        }

        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
        super.updateComplete();
    }

    @Override
    public void updateTransactions(Account account, Urllib urlopen) throws LoginException,
            BankException, IOException {
        super.updateTransactions(account, urlopen);
        if (account.getAliasfor() == null || account.getAliasfor().length() == 0) {
            return;
        }

        Matcher matcher;
        response = urlopen.open(
                "https://www.csn.se/studiemedel/utbetalningar/utbetalningar.do?javascript=off");
        matcher = reTransactions.matcher(response);
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        while (matcher.find()) {
            /*
             * Capture groups:
             * GROUP                        EXAMPLE DATA
             * 1: Date                      2010-11-25
             * 2: Specification             Vecka 47-50
             * 3: Status                    Utbetald
             * 4: Amount                    8,140
             *
             */
            transactions.add(new Transaction(matcher.group(1).trim(),
                    Html.fromHtml(matcher.group(2)).toString().trim() + " (" + Html
                            .fromHtml(matcher.group(3)).toString().trim() + ")",
                    Helpers.parseBalance(matcher.group(4).replace(",", ""))));
        }
        response = urlopen.open(
                "https://www.csn.se/aterbetalning/vadSkallJagBetalUnderAret/betalningstillfallen.do?javascript=off");
        matcher = reTransactions.matcher(response);
        while (matcher.find()) {
            /*
             * Capture groups:
             * GROUP                        EXAMPLE DATA
             * 1: Date                      2010-11-25
             * 2: Specification             Bankgiro 5580-3084
             * 3: OCR-number                4576225900
             * 4: Amount                    1,234
             *
             */
            transactions.add(new Transaction(matcher.group(1).trim(),
                    Html.fromHtml(matcher.group(2)).toString().trim() + " (" + Html
                            .fromHtml(matcher.group(3)).toString().trim() + ")",
                    Helpers.parseBalance(matcher.group(4).replace(",", "")).negate()));
        }

        response = urlopen.open(
                "https://www.csn.se/aterbetalning/harMinaInbetalningarKommitIn/registreradeInbetalningar.do?javascript=off");
        matcher = reCompletedPayments.matcher(response);
        while (matcher.find()) {
            /*
             * Capture groups:
             * GROUP                        EXAMPLE DATA
             * 1: Date                      2006-08-21
             * 2: Specification             Återkrav första halvåret 2006 lån 1
             * 3: Amount                    1,050
             *
             */
            transactions.add(new Transaction(matcher.group(1).trim(),
                    Html.fromHtml(matcher.group(2)).toString().trim(),
                    Helpers.parseBalance(matcher.group(3).replace(",", "")).negate()));
        }

        Collections.sort(transactions, Collections.reverseOrder());
        account.setTransactions(transactions);
    }
}
