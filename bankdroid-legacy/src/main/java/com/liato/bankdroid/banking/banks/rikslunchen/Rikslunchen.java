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

import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.banks.rikslunchen.model.Envelope;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.legacy.R;

import org.apache.http.entity.StringEntity;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.content.Context;
import android.text.InputType;
import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class Rikslunchen extends Bank {

    private static final String TAG = "Rikslunchen";

    private static final String NAME = "Rikslunchen";

    private static final String NAME_SHORT = "rikslunchen";

    private static final String URL = "http://www.rikslunchen.se/index.html";

    private static final int BANKTYPE_ID = Bank.RIKSLUNCHEN;

    private String myResponse = "";

    public Rikslunchen(Context context) {
        super(context, R.drawable.logo_rikslunchen);
        super.TAG = TAG;
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
        try {
            urlopen = new Urllib(context,
                    CertificateReader.getCertificates(context, R.raw.cert_rikslunchen));
            urlopen.addHeader("Authorization", "basic Q0g6ODlAUHFqJGw4NyMjTVM=");
            urlopen.addHeader("SOAPAction", "");
            urlopen.addHeader("Content-Type", "text/xml;charset=UTF-8");
            StringEntity body = new StringEntity(String.format(
                    "<v:Envelope xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:d=\"http://www.w3.org/2001/XMLSchema\" xmlns:c=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:v=\"http://schemas.xmlsoap.org/soap/envelope/\"><v:Header /><v:Body><n0:getBalance id=\"o0\" c:root=\"1\" xmlns:n0=\"urn:PhoneService\"><cardNo i:type=\"d:string\">%s</cardNo></n0:getBalance></v:Body></v:Envelope>",
                    getUsername()), "UTF-8");
            InputStream is = urlopen.openStream("https://www.rikslunchen.se/rkchws/PhoneService",
                    body, true);

            Serializer serializer = new Persister();
            Envelope resp = null;
            try {
                resp = serializer.read(Envelope.class, is, false);
            } catch (Exception e) {
                throw new BankException(e.getMessage(), e);
            }

            if (resp != null && resp.body != null && resp.body.fault != null && !TextUtils.isEmpty(
                    resp.body.fault.faultstring)) {
                throw new LoginException(context.getString(R.string.invalid_card_number));
                //faultString isn't always very descriptive
                //throw new BankException(resp.body.fault.faultstring);
            } else if (resp == null || resp.body == null || resp.body.getBalanceResponse == null
                    || resp.body.getBalanceResponse.responseReturn == null
                    || resp.body.getBalanceResponse.responseReturn.amount == null) {
                throw new LoginException(context.getString(R.string.invalid_card_number));
            }
            BigDecimal balance = Helpers.parseBalance(
                    resp.body.getBalanceResponse.responseReturn.amount);
            accounts.add(new Account("Rikslunchen", balance, "1"));
        } catch (UnsupportedEncodingException e) {
            throw new BankException(e.getMessage(), e);
        }
        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found)
                    .toString());
        }
        super.updateComplete();
    }
}
