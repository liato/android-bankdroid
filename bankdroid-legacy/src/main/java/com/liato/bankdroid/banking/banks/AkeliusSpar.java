/* Copyright (C) 2012 Nullbyte <http://nullbyte.eu>, first version by Snah@Swedroid 2012-01-06
 * Modified for Akelius Spar by Per Wigren <per.wigren@gmail.com>
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

import android.content.Context;
import android.text.Html;
import android.text.InputType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class AkeliusSpar extends Bank {

    private static final String NAME = "Akelius Spar";

    private static final String URL = "https://www.online.akeliusspar.se/";

    private static final int BANKTYPE_ID = IBankTypes.AKELIUSSPAR;

    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;

    private static final int INPUT_TYPE_PASSWORD = InputType.TYPE_CLASS_TEXT;

    private static final String INPUT_HINT_USERNAME = "YYYYMMDDNNNN";

    private static final boolean STATIC_BALANCE = true;

    private Pattern reLogintoken = Pattern.compile("logintoken\"\\s+value=\"([^\"]+)\"");

    private Pattern reError = Pattern.compile("<label\\s+class=\"error\">(.+?)</label>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private Pattern reAccounts = Pattern.compile(
            "<tr>\\s*<td[^>]*>([^<]+)</td>\\s*<td[^>]*><a[^?]+\\?selectedaccount=([^\"]+)\"[^>]*>([^<]+)</a></td>\\s*<td[^>]+>([^<]+)</td>\\s*<td[^>]+>([^<]+)</td>\\s*<td[^>]+>([^<]+)</td>",
            Pattern.CASE_INSENSITIVE);

    //private Pattern reBalanceDisp = Pattern.compile("account\\.aspx\\?id=([^\"]+).+?>([^<]+)</a.+?Disponibelt([0-9 .,-]+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    //private Pattern reBalanceSald = Pattern.compile("account\\.aspx\\?id=([^\"]+).+?>([^<]+)</a[^D]*Saldo([0-9 .,-]+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    // private Pattern reBalance = Pattern.compile("AccountStatement\\.mws\\?selectedaccount=([^\"]+).+?>([^<]+)</a.+?Disponibelt([0-9 .,-]+)[^<]*<br/>.+?Saldo([0-9 .,-]+)", Pattern.CASE_INSENSITIVE);
    private Pattern reTransactions = Pattern
            .compile("top\">([^<]+)</td>\\s*<td[^>]+>([^<]+)</td>\\s*<td[^>]+>([^<]+)</td>",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private HashMap<String, String> mIdMappings = new HashMap<String, String>();

    public AkeliusSpar(Context context) {
        super(context, R.drawable.logo_akeliusspar);
        super.url = URL;
        super.inputTypeUsername = INPUT_TYPE_USERNAME;
        super.inputTypePassword = INPUT_TYPE_PASSWORD;
        super.inputHintUsername = INPUT_HINT_USERNAME;
        super.staticBalance = STATIC_BALANCE;
    }

    @Override
    public int getBanktypeId() {
        return BANKTYPE_ID;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public AkeliusSpar(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }


    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        urlopen = new Urllib(context,
                CertificateReader.getCertificates(context, R.raw.cert_akeliusspar));
        String response = urlopen.open("https://www.online.akeliusspar.se/login.mws");
        Matcher matcher = reLogintoken.matcher(response);
        if (!matcher.find()) {
            throw new BankException(
                    res.getText(R.string.unable_to_find).toString() + " logintoken.");
        }
        String strLogintoken = matcher.group(1);

        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("action", "login"));
        postData.add(new BasicNameValuePair("logintoken", strLogintoken));
        postData.add(new BasicNameValuePair("df_username", getUsername()));
        postData.add(new BasicNameValuePair("df_password", getPassword()));
        postData.add(new BasicNameValuePair("Language", "SV"));
        postData.add(new BasicNameValuePair("IdleTime", "900"));

        return new LoginPackage(urlopen, postData, response,
                "https://www.online.akeliusspar.se/login.mws");
    }

    public Urllib login() throws LoginException, BankException, IOException {

        LoginPackage lp = preLogin();
        String response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
        Matcher matcher = reError.matcher(response);
        if (matcher.find()) {
            String errormsg = Html.fromHtml(matcher.group(1).trim()).toString();
            if (errormsg.contains("ord eller personnummer") || errormsg.contains("et alternativ")
                    || errormsg.contains("fyra siffror")) {
                throw new LoginException(errormsg);
            } else {
                throw new BankException(errormsg);
            }
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

        String response = urlopen.open("https://www.online.akeliusspar.se/AccountPortfolio.mws");
        Matcher matcher = reAccounts.matcher(response);
        int accId = 0;
        while (matcher.find()) {
            /*
             * Capture groups:
             * GROUP                ICA                    AKELIUSINVEST
             * 1: ID                0000000000            Kontonamn
             * 2: Name              ICA KONTO            KontoID
             * 3: Disponibelt       00.000,00            Kontonummer
             * 4: Saldo             1.655,71            Valuta
             * 5:                                         Tillgängligt belopp
             * 6:                                         Saldo
             */
//            Försök att lösa problemet med för långa, icke radbrytande kontonamn:
//                if (matcher.group(1).length() > 24)  {
//                    matcher.group(1).replaceFirst("(", "(\n");
//                }

            mIdMappings.put(Integer.toString(accId), matcher.group(2).trim());
            accounts.add(new Account(
                    Html.fromHtml(matcher.group(1)).toString().trim() + " (Tillgängligt belopp)",
                    Helpers.parseBalance(matcher.group(5).trim()), Integer.toString(accId)));
            Account account = new Account(
                    Html.fromHtml(matcher.group(1)).toString().trim() + " (Saldo)",
                    Helpers.parseBalance(matcher.group(6).trim()), "a:" + accId);
            account.setAliasfor(matcher.group(1).trim());

            accounts.add(account);
            balance = balance.add(Helpers.parseBalance(matcher.group(5)));
            accId++;
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
        if (account.getId().startsWith("a:") || !mIdMappings.containsKey(account.getId())) {
            return; // No transactions for "saldo"-accounts
        }
        String accountId = mIdMappings.get(account.getId());
        String response = null;
        Matcher matcher;

        response = urlopen
                .open("https://www.online.akeliusspar.se/AccountStatement.mws?selectedaccount="
                        + accountId);
        matcher = reTransactions.matcher(response);
                /*                 ICA-banken    Akelius Invest
         * Beskrivning    1            2
         * Datum        2            1
         * Belopp        3            3
         */

        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        while (matcher.find()) {
            transactions.add(new Transaction(matcher.group(1).trim(),
                    Html.fromHtml(matcher.group(2)).toString().trim(),
                    Helpers.parseBalance(matcher.group(3))));
        }

        account.setTransactions(transactions);
    }
}
