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

package com.liato.bankdroid.banking.banks.seb;

import android.content.Context;
import android.text.InputType;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liato.bankdroid.legacy.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.banks.seb.model.DEVID;
import com.liato.bankdroid.banking.banks.seb.model.HWINFO;
import com.liato.bankdroid.banking.banks.seb.model.Request;
import com.liato.bankdroid.banking.banks.seb.model.SEBRequest;
import com.liato.bankdroid.banking.banks.seb.model.ServiceInput;
import com.liato.bankdroid.banking.banks.seb.model.UserCredentials;
import com.liato.bankdroid.banking.banks.seb.model.VODB;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class SEB extends Bank {
    private static final String TAG = "SEB";
    private static final String NAME = "SEB";
    private static final String NAME_SHORT = "seb";
    private static final String URL = "https://m.seb.se/cgi-bin/pts3/mpo/mpo0001.aspx";
    private static final int BANKTYPE_ID = IBankTypes.SEB;
    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;
    private static final String INPUT_HINT_USERNAME = "ÅÅMMDDXXXX";

    private Pattern reAccounts = Pattern.compile("/cgi-bin/pts3/mps/1100/mps1102\\.aspx\\?M1=show&amp;P1=([^&]+)&amp;P2=1&amp;P4=1\">([^<]+)</a></td>\\s*</tr>\\s*<tr[^>]+>\\s*<td>[^<]+</td>\\s*<td[^>]+>[^<]+</td>\\s*<td[^>]+>([^<]+)</td>\\s*", Pattern.CASE_INSENSITIVE);
    private Pattern reTransactions = Pattern.compile("(\\d{6})\\s*<br\\s?/>\\s*<span\\s*id=\"MPSMaster_MainPlaceHolder_repAccountTransactions[^\"]+\"\\s*class=\"name\">([^/]+)(?:/(\\d{2}-\\d{2}-\\d{2}))?</span>\\s*<span\\s*id=\"MPSMaster_MainPlaceHolder_repAccountTransactions[^\"]+\"\\s*class=\"value\">([^<]+)</span>", Pattern.CASE_INSENSITIVE);

    private String response = null;
    private ObjectMapper mObjectMapper;

    public SEB(Context context) {
        super(context);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
        super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
    }

    public SEB(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
        urlopen = new Urllib(context, CertificateReader.getCertificates(context, R.raw.cert_seb_web));
        urlopen.setContentCharset(HTTP.ISO_8859_1);
        urlopen.addHeader("Referer", "https://m.seb.se/");
        urlopen.setKeepAliveTimeout(5);
        //response = urlopen.open("https://m.seb.se/cgi-bin/pts3/mpo/9000/mpo9001.aspx?P1=logon.htm");
        List <NameValuePair> postData = new ArrayList <NameValuePair>();
        postData.add(new BasicNameValuePair("A1", username));
        postData.add(new BasicNameValuePair("A2", password));
        postData.add(new BasicNameValuePair("A3", "4"));
        return new LoginPackage(urlopen, postData, response, "https://m.seb.se/cgi-bin/pts3/mps/1000/mps1001bm.aspx");
    }

    @Override
    public Urllib login() throws LoginException, BankException {
        try {
            urlopen = new Urllib(context, CertificateReader.getClientCertificate(context, R.raw.cert_client_seb, "openbankdata"), CertificateReader.getCertificates(context, R.raw.cert_seb));
            urlopen.setFollowRedirects(false);
            List <NameValuePair> postData = new ArrayList <NameValuePair>();
            postData.add(new BasicNameValuePair("A1", username));
            postData.add(new BasicNameValuePair("A2", password));
            HttpResponse hr = urlopen.openAsHttpResponse("https://mP.seb.se/nauth2/Authentication/Auth?SEB_Referer=/priv/ServiceFactory-pw", postData, true);
            if (hr.getStatusLine().getStatusCode() == 200) {
                throw new LoginException(res.getString(R.string.invalid_username_password));
            } else if (hr.getStatusLine().getStatusCode() != 302) {
                throw new BankException(res.getString(R.string.unable_to_login));
            }
            urlopen.setFollowRedirects(true);
        } catch (ClientProtocolException e) {
            throw new BankException(e.getMessage(), e);
        } catch (IOException e) {
            throw new BankException(e.getMessage(), e);
        }
        return urlopen;
    }

    @Override
    public void update() throws BankException, LoginException, BankChoiceException {
        super.update();
        if (username == null || password == null || username.length() == 0 || password.length() == 0) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }

        urlopen = login();
        urlopen.addHeader("Content-Type", "application/json;charset=UTF-8");
        urlopen.addHeader("Accept", "application/json");

        SEBRequest sessionRequest = new SEBRequest();
        Request r = new Request();
        r.setUserCredentials(new UserCredentials(username, password));
        ServiceInput serviceInput = new ServiceInput();
        serviceInput.setCondition("EQ");
        serviceInput.setVariableName("CUSTOMERTYPE");
        serviceInput.setVariableValue("P");
        r.getServiceInput().add(serviceInput);
        VODB vodb = new VODB();
        vodb.setHWINFO01(HWINFO.createDefault());
        vodb.setDEVID01(DEVID.createDefault());
        r.setVODB(vodb);
        sessionRequest.setRequest(r);

        try {
            HttpEntity e = new StringEntity(getObjectmapper().writeValueAsString(sessionRequest));
            HttpResponse hr = urlopen.openAsHttpResponse("https://mP.seb.se/1000/ServiceFactory/PC_BANK/PC_BankAktivera01Session01.asmx/Execute", e, true);
            hr.getEntity().getContent();

        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        } catch (JsonProcessingException e1) {
            e1.printStackTrace();
        } catch (ClientProtocolException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }


//        {
//            "request": {
//            "ResultInfo": null,
//                    "VODB": {
//                "USRINF01": null,
//                        "DBZV160": [],
//                "DEVID01": {
//                    "APPLICATION_VERSION": "6.0.0",
//                            "OS_NAME": "Android",
//                            "MODEL": "4S",
//                            "MANUFACTURER": "Apple",
//                            "OS_VERSION": "5",
//                            "APPLICATION_NAME": "MASP"
//                },
//                "HWINFO01": {
//                    "LONGITUDE_DECIMAL": "0",
//                            "LATITUDE_DECIMAL": "0",
//                            "COUNTRY_PREFIX": 0
//                },
//                "CBEW501": [],
//                "DBZV170": [],
//                "CBEW502": []
//            },
//            "ServiceInput": [{
//                "Condition": "EQ",
//                        "VariableNamePossibleValues": [],
//                "VariableName": "CUSTOMERTYPE",
//                        "VariableValue": "P"
//            }],
//            "UserCredentials": {
//                "ApplicationName": "RFO",
//                        "WorkstationID": "",
//                        "LoggedOnUser": "Mobile",
//                        "AuthMethod": "0",
//                        "UserId": "8001019999",
//                        "Password": "123password"
//            },
//            "ServiceInfo": null
//        }
//        }

        Log.d("SEB", "Logged in");
//        Matcher matcher;
//        try {
//            response = urlopen.open("https://m.seb.se/cgi-bin/pts3/mps/1100/mps1101.aspx?X1=digipassAppl1");
//            matcher = reAccounts.matcher(response);
//            while (matcher.find()) {
//                /*
//                 * Capture groups:
//                 * GROUP                    EXAMPLE DATA
//                 * 1: ID                    GJmQRqlrOPmM++1zf50d6Q==
//                 * 2: Name                  Personkonto
//                 * 3: Amount                2.208,03
//                 *
//                 */
//                accounts.add(new Account(Html.fromHtml(matcher.group(2)).toString().trim(), Helpers.parseBalance(matcher.group(3)), matcher.group(1).trim()));
//                balance = balance.add(Helpers.parseBalance(matcher.group(3)));
//            }
//
//            if (accounts.isEmpty()) {
//                throw new BankException(res.getText(R.string.no_accounts_found).toString());
//            }
//        }
//        catch (ClientProtocolException e) {
//            throw new BankException(e.getMessage());
//        }
//        catch (IOException e) {
//            throw new BankException(e.getMessage());
//        }
//        finally {
//            super.updateComplete();
//        }
    }

    @Override
    public void updateTransactions(Account account, Urllib urlopen) throws LoginException, BankException {
        super.updateTransactions(account, urlopen);

        //No transaction history for loans, funds and credit cards.
        int accType = account.getType();
        if (accType == Account.LOANS || accType == Account.FUNDS || accType == Account.CCARD) return;

//        Matcher matcher;
//        try {
//            response = urlopen.open("https://m.seb.se/cgi-bin/pts3/mps/1100/mps1102.aspx?M1=show&P2=1&P4=1&P1=" + account.getId());
//            matcher = reTransactions.matcher(response);
//            ArrayList<Transaction> transactions = new ArrayList<Transaction>();
//            while (matcher.find()) {
//                /*
//                 * Capture groups:
//                 * GROUP                    EXAMPLE DATA
//                 * 1: Book. date            101214
//                 * 2: Transaction           St1
//                 * 3: Trans. date           10-12-11
//                 * 4: Amount                -200,07
//                 *
//                 */
//                String date;
//                if (matcher.group(3) == null || matcher.group(3).length() == 0) {
//                    date = Html.fromHtml(matcher.group(1)).toString().trim();
//                    date = String.format("%s-%s-%s", date.substring(0,2), date.substring(2,4), date.substring(4,6));
//                }
//                else {
//                    date = Html.fromHtml(matcher.group(3)).toString().trim();
//                }
//                transactions.add(new Transaction("20"+date, Html.fromHtml(matcher.group(2)).toString().trim(), Helpers.parseBalance(matcher.group(4))));
//            }
//            Collections.sort(transactions, Collections.reverseOrder());
//            account.setTransactions(transactions);
//        } catch (ClientProtocolException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
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
            throw new BankException(e.getMessage(), e);
        }
    }
}