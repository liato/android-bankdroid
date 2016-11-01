/*
 * Copyright (C) 2014 Nullbyte <http://nullbyte.eu>
 * Contributors: PMC
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
package com.liato.bankdroid.banking.banks.rikslunchen;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.legacy.R;

import org.apache.http.HttpResponse;
import android.content.Context;
import android.text.InputType;

import java.io.IOException;
import java.math.BigDecimal;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class Rikslunchen extends Bank {

    private static final String NAME = "Rikslunchen";

    private static final String NAME_SHORT = "rikslunchen";

    private static final String URL = "http://www.rikslunchen.se/index.html";

    private static final int BANKTYPE_ID = Bank.RIKSLUNCHEN;

    private static final String BASE_URL = "http://www.rikslunchen.se/isr/isr/services/bankdroid/getbalance";

    private static final ObjectReader READER = new ObjectMapper().reader();

    public Rikslunchen(Context context) {
        super(context, R.drawable.logo_rikslunchen);

        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
        super.INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;
        super.INPUT_TITLETEXT_USERNAME = R.string.card_id;
        super.INPUT_HIDDEN_PASSWORD = true;
    }

    public Rikslunchen(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }

    @Override
    public Urllib login() throws LoginException, BankException {
        return urlopen;
    }

    @Override
    public void update() throws BankException, LoginException, BankChoiceException, IOException {
        super.update();
        if (getUsername().isEmpty()) {
            throw new LoginException(res.getText(R.string.invalid_card_number).toString());
        }

        urlopen = new Urllib(context,
                CertificateReader.getCertificates(context, R.raw.cert_rikslunchen));

        HttpResponse response = urlopen.openAsHttpResponse(
                BASE_URL + "?cardid=" + getUsername(),
                false);
        if (response.getStatusLine().getStatusCode() != 200) {
            response.getEntity().consumeContent();
            throw new LoginException(context.getString(R.string.invalid_card_number));
        }

        JsonNode node = READER.readTree(response.getEntity().getContent());

        BigDecimal balance = new BigDecimal(node.get("balance").asDouble());
            accounts.add(new Account("Rikslunchen", balance, "1"));

        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found)
                    .toString());
        }
        super.updateComplete();
    }
}
