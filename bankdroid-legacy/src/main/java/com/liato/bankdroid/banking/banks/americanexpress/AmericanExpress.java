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

package com.liato.bankdroid.banking.banks.americanexpress;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.Transaction;
import com.liato.bankdroid.banking.banks.americanexpress.model.Card;
import com.liato.bankdroid.banking.banks.americanexpress.model.LoginRequest;
import com.liato.bankdroid.banking.banks.americanexpress.model.LoginResponse;
import com.liato.bankdroid.banking.banks.americanexpress.model.TransactionDetails;
import com.liato.bankdroid.banking.banks.americanexpress.model.TransactionsResponse;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.legacy.R;
import com.liato.bankdroid.provider.IAccountTypes;
import com.liato.bankdroid.provider.IBankTypes;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.joda.time.format.DateTimeFormat;

import android.content.Context;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class AmericanExpress extends Bank {

    private static final String TAG = "AmericanExpress";

    private static final String NAME = "American Express";

    private static final String NAME_SHORT = "americanexpress";

    private static final int BANKTYPE_ID = IBankTypes.AMERICANEXPRESS;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Nullable
    private LoginResponse loginResponse;

    public AmericanExpress(Context context) {
        super(context, R.drawable.logo_americanexpress);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.WEB_VIEW_ENABLED = false;
    }

    public AmericanExpress(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        urlopen = new Urllib(context, CertificateReader.getCertificates(context,
                R.raw.cert_americanexpress_global));
        urlopen.setAllowCircularRedirects(true);
        urlopen.setContentCharset(HTTP.UTF_8);
        urlopen.addHeader("Face", "sv_SE");
        return new LoginPackage(urlopen, null, null,
                "https://global.americanexpress.com/myca/intl/moblclient/emea/svc/v1/loginSummary.do");
    }

    @Override
    public Urllib login() throws LoginException, BankException, IOException {
        LoginPackage lp = preLogin();
        loginResponse = parseLoginResponse(urlopen.openAsHttpResponse(
                lp.getLoginTarget(),
                new StringEntity(
                        objectAsJson(new LoginRequest(
                                getUsername(),
                                getPassword())),
                        HTTP.UTF_8),
                true));

        urlopen.addHeader("cupcake", loginResponse.getLogonData().getCupcake());
        return urlopen;
    }

    @Override
    public void update() throws BankException, LoginException, BankChoiceException, IOException {
        super.update();
        if (getUsername().isEmpty() || getPassword().isEmpty()) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }

        urlopen = login();

        for(Card card : loginResponse.getCards()) {
            Account account = asAccount(card);
            if(card.isTransactionsEnabled()) {
                account.setTransactions(fetchTransactionsFor(card));
            }
            accounts.add(account);
        }
        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
        super.updateComplete();
    }

    private List<Transaction> fetchTransactionsFor(Card card) throws IOException, BankException {
        HttpResponse response = urlopen.openAsHttpResponse(
                "https://global.americanexpress.com/myca/intl/moblclient/emea/svc/v1/transaction.do",
                new StringEntity("{" +
                        "\"billingIndexList\": [\"0\"]," +
                        "\"sortedIndex\":" + card.getSortedIndex() +
                        "}",
                        HTTP.UTF_8), true);
        if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            response.getEntity().consumeContent();
            throw new BankException(
                    res.getText(R.string.update_transactions_error).toString());
        }

        TransactionsResponse details = MAPPER.reader()
                .withType(TransactionsResponse.class)
                .readValue(response.getEntity().getContent());

        if(details.getTransactionDetails() == null) {
            throw new BankException(res.getText(R.string.server_error_try_again).toString());
        }
        if(details.getTransactionDetails().getStatus() != 0) {
            throw new BankException(details.getTransactionDetails().getMessage());
        }

        return transactionsOf(details.getTransactionDetails());
    }

    private List<Transaction> transactionsOf(@Nullable TransactionDetails details) {
        List<Transaction> transactions = new ArrayList<>();
        if(details != null) {
            for(com.liato.bankdroid.banking.banks.americanexpress.model.Transaction transaction : details.getTransactions()) {
                transactions.add(asTransaction(transaction));
            }
        }
        return transactions;
    }

    private Transaction asTransaction(com.liato.bankdroid.banking.banks.americanexpress.model.Transaction transaction) {
        return new Transaction(
                DateTimeFormat.forPattern("yyyyMMdd")
                        .parseDateTime(
                                Long.toString(transaction.getChargeDate().getRawValue())
                        ).toString("yyyy-MM-dd"),
                transaction.getDescription().get(0),
                new BigDecimal(transaction.getAmount().getRawValue()).negate()
        );
    }

    private Account asAccount(Card card) {
        Account account = new Account(
                card.getCardProductName(),
                card.getBalance(),
                card.getCardKey());
        account.setType(IAccountTypes.CCARD);
        return account;
    }

    private String objectAsJson(Object value) throws BankException {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BankException(e.getMessage(), e);
        }
    }

    private LoginResponse parseLoginResponse(HttpResponse response) throws IOException, BankException {
        if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            response.getEntity().consumeContent();
            throw new BankException(res.getText(R.string.server_error_try_again).toString());
        }
        LoginResponse loginResponse = MAPPER.reader()
                .withType(LoginResponse.class)
                .readValue(response.getEntity().getContent());
        if(loginResponse == null || loginResponse.getLogonData() == null) {
            throw new BankException(res.getText(R.string.server_error_try_again).toString());
        }
        if(loginResponse.getLogonData().getStatus() != 0) {
            throw new BankException(loginResponse.getLogonData().getMessage());
        }

        return loginResponse;
    }
}
