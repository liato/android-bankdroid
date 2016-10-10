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

import android.content.Context;
import android.text.Html;
import android.text.InputType;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.nullbyte.android.urllib.Urllib;

public class TestBank extends Bank {

    private static final String TAG = "TestBank";

    private static final String NAME = "Testbank";

    private static final String NAME_SHORT = "testbank";

    private static final String URL = "http://www.nullbyte.eu/";

    private static final int BANKTYPE_ID = IBankTypes.TESTBANK;

    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;

    private static final int INPUT_TYPE_PASSWORD = InputType.TYPE_CLASS_PHONE;

    private static final String INPUT_HINT_USERNAME = "ÅÅMMDD-XXXX";

    private static final String VOLATILE_ACCOUNT_NAME = "Volatile";

    private final Random random = new Random();

    private Pattern reAccounts = Pattern.compile(
            "<div>\\s*<span>([^<]+)</span>\\s*<span>([^<]+)</span>\\s*<span>([^<]+)</span>\\s*<span>([^<]+)</",
            Pattern.CASE_INSENSITIVE);

    private Pattern reTransactions = Pattern.compile(
            "(\\d{4}-\\d{2}-\\d{2})\\s</dt>[^>]+>([^<]+)[^>]+>.*?(?:Positive|Negative)\">([^<]+)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public TestBank(Context context) {
        super(context, R.drawable.logo_bankdroid);

        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
        super.INPUT_TYPE_PASSWORD = INPUT_TYPE_PASSWORD;
        super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
    }

    public TestBank(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }

    @Override
    public Urllib login() throws LoginException, BankException {
        urlopen = new Urllib(context);
        return urlopen;
    }

    /**
     * Adds a new account to the accounts list.
     * <p/>
     * If the account is already there it will have its balance updated.
     * <p/>
     * The purpose of this account is to test updates and how they propagate in the UI.
     */
    private void addVolatileAccount() {
        Account volatileAccount = null;
        for (Account account: accounts) {
            if (VOLATILE_ACCOUNT_NAME.equals(account.getName())) {
                volatileAccount = account;
            }
        }

        if (volatileAccount == null) {
            volatileAccount =
                new Account(VOLATILE_ACCOUNT_NAME, BigDecimal.ZERO, VOLATILE_ACCOUNT_NAME);
            accounts.add(volatileAccount);
        }
        double balance = Math.round(random.nextDouble() * 1000000) / 100;
        volatileAccount.setBalance(BigDecimal.valueOf(balance));
    }

    @Override
    public void update() throws BankException, LoginException, BankChoiceException, IOException {
        super.update();
        if (getUsername().isEmpty() || getPassword().isEmpty()) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }
        urlopen = login();
        String response = urlopen.open(
                "http://www.nullbyte.eu/bankdroid/tests/testbank/accounts.htm");
        Matcher matcher = reAccounts.matcher(response);
        while (matcher.find()) {
            /*
             * Capture groups:
             * GROUP                EXAMPLE DATA
             * 1: Name              Sparkonto
             * 2: Amount            83553,70
             * 3: ID                1
             * 4: Type              trans|fund
             */
            Account acc = new Account(Html.fromHtml(matcher.group(1)).toString().trim(),
                    Helpers.parseBalance(matcher.group(2)),
                    ("fund".equals(matcher.group(4)) ? "f:" : "") + matcher.group(3).trim());
            if ("fund".equals(matcher.group(4))) {
                acc.setType(Account.FUNDS);
            } else {
                balance = balance.add(Helpers.parseBalance(matcher.group(3)));
            }
            accounts.add(acc);
        }

        addVolatileAccount();

        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
        super.updateComplete();
    }

    @Override
    public void updateTransactions(Account account, Urllib urlopen) throws LoginException,
            BankException, IOException {
        super.updateTransactions(account, urlopen);

        //No transaction history for loans, funds and credit cards.
        int accType = account.getType();
        if (accType == Account.LOANS || accType == Account.FUNDS || accType == Account.CCARD) {
            return;
        }

        Matcher matcher;
        String response = urlopen.open(
                "http://www.nullbyte.eu/bankdroid/tests/testbank/transactions_" + account.getId()
                        + ".htm");
        matcher = reTransactions.matcher(response);
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        while (matcher.find()) {
            transactions.add(new Transaction(Html.fromHtml(matcher.group(1)).toString().trim(),
                    Html.fromHtml(matcher.group(2)).toString().trim(),
                    Helpers.parseBalance(matcher.group(3))));
        }
        account.setTransactions(transactions);
    }
}
