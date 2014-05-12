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

package com.liato.bankdroid.banking.banks.coop;

import android.content.Context;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liato.bankdroid.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.banks.coop.model.AuthenticateRequest;
import com.liato.bankdroid.banking.banks.coop.model.AuthenticateResponse;
import com.liato.bankdroid.banking.banks.coop.model.RefundSummaryRequest;
import com.liato.bankdroid.banking.banks.coop.model.RefundSummaryResponse;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class Coop extends Bank {
    private static final String TAG = "Coop";
    private static final String NAME = "Coop";
    private static final String NAME_SHORT = "coop";
    private static final String URL = "https://www.coop.se/mina-sidor/oversikt/";
    private static final int BANKTYPE_ID = IBankTypes.COOP;
    private static final String APPLICATION_ID = "17B2F3F1-841B-40B5-B91C-A5F33DE73C18";

    private Pattern reViewState = Pattern.compile("__VIEWSTATE\"\\s+value=\"([^\"]+)\"");
 //   private Pattern reEventValidation = Pattern.compile("__EVENTVALIDATION\"\\s+value=\"([^\"]+)\"");
    private Pattern reBalance = Pattern.compile("saldo\">([^<]+)<", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private Pattern reTransactions = Pattern.compile("<td>\\s*(\\d{4}-\\d{2}-\\d{2})\\s*</td>\\s*<td>([^<]+)</td>\\s*<td>([^<]*)</td>\\s*<td>([^<]*)</td>\\s*<td[^>]*>(?:\\s*<a[^>]+>)?([^<]+)(?:</a>\\s*)?</td>", Pattern.CASE_INSENSITIVE);
    private ObjectMapper mObjectMapper;
    private String response;
    private String mToken;
    private String mUserId;

    public Coop(Context context) {
        super(context);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
    }

    public Coop(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
        urlopen = new Urllib(context, CertificateReader.getCertificates(context, R.raw.cert_coop));
        urlopen.addHeader("Origin", "https://www.coop.se");
        urlopen.addHeader("Referer", "https://www.coop.se/Mina-sidor/Logga-in-puffsida/?li=True");
        response = urlopen.open("https://www.coop.se/Mina-sidor/Logga-in-puffsida/?li=True");
        
        Matcher matcher = reViewState.matcher(response);
        if (!matcher.find()) {
            throw new BankException(res.getText(R.string.unable_to_find).toString()+" viewstate.");
        }
        String strViewState = matcher.group(1);
        List <NameValuePair> postData = new ArrayList <NameValuePair>();
        postData.add(new BasicNameValuePair("TextBoxUserName", username));
        postData.add(new BasicNameValuePair("TextBoxPassword", password));
        postData.add(new BasicNameValuePair("__VIEWSTATE", strViewState));
        postData.add(new BasicNameValuePair("ButtonLogin", ""));
        return new LoginPackage(urlopen, postData, response, "https://www.coop.se/Mina-sidor/Logga-in-puffsida/?li=True");
    }


    @Override
    public Urllib login() throws LoginException, BankException {
        try {
            AuthenticateRequest authReq = new AuthenticateRequest(username, password, APPLICATION_ID);
            HttpEntity e = new StringEntity(getObjectmapper().writeValueAsString(authReq));
            urlopen = new Urllib(context, CertificateReader.getCertificates(context, R.raw.cert_coop));
            urlopen.addHeader("Content-Type", "application/json");
            InputStream is = urlopen.openStream("https://www.coop.se/ExternalServices/UserService.svc/Authenticate", e, true);
            AuthenticateResponse authResponse = readJsonValue(is, AuthenticateResponse.class);
            if (authResponse == null) {
                throw new BankException(res.getString(R.string.unable_to_login));
            }
            if (authResponse.getAuthenticateResult() == null || authResponse.getErrorid() != null || authResponse.getAuthenticateResult() == null) {
                throw new LoginException(res.getString(R.string.invalid_username_password));
            }
            mToken = authResponse.getAuthenticateResult().getToken();
            mUserId = Integer.toString(authResponse.getAuthenticateResult().getUserID());
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

        login();

        try {
            RefundSummaryRequest refsumReq = new RefundSummaryRequest(mUserId, mToken, APPLICATION_ID);
            HttpEntity e = new StringEntity(getObjectmapper().writeValueAsString(refsumReq));
            InputStream is = urlopen.openStream("https://www.coop.se/ExternalServices/RefundService.svc/RefundSummary", e, true);
            RefundSummaryResponse refsumResp = readJsonValue(is, RefundSummaryResponse.class);
            if (refsumResp != null && refsumResp.getRefundSummaryResult() != null) {
                Account a = new Account("Återbäring på ditt kort", BigDecimal.valueOf(refsumResp.getRefundSummaryResult().getPeriodRefund()), "1");
                a.setCurrency("SEK");
                accounts.add(a);
            }
        } catch (JsonParseException e) {
            e.printStackTrace();
            throw new BankException(e.getMessage());
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            throw new BankException(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new BankException(e.getMessage());
        }

        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
        super.updateComplete();
    }

    private ObjectMapper getObjectmapper() {
        if (mObjectMapper == null) {
            mObjectMapper = new ObjectMapper();
            mObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mObjectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        }
        return mObjectMapper;
    }

    private <T> T readJsonValue(InputStream is, Class<T> valueType) throws BankException {
        try {
            return getObjectmapper().readValue(is, valueType);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}