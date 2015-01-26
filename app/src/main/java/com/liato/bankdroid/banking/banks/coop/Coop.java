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
import android.text.TextUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.Transaction;
import com.liato.bankdroid.banking.banks.coop.model.AuthenticateRequest;
import com.liato.bankdroid.banking.banks.coop.model.AuthenticateResponse;
import com.liato.bankdroid.banking.banks.coop.model.RefundSummaryRequest;
import com.liato.bankdroid.banking.banks.coop.model.RefundSummaryResponse;
import com.liato.bankdroid.banking.banks.coop.model.web.Result;
import com.liato.bankdroid.banking.banks.coop.model.web.WebAuthenticateRequest;
import com.liato.bankdroid.banking.banks.coop.model.web.WebTransactionHistoryResponse;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.StringEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private static final Map<String, String> MONTHS = new HashMap<String, String>();
    static {
        String[] ms = new String[] {"januari", "februari", "mars", "april", "maj", "juni", "juli", "augusti", "september", "oktober", "november", "december"};
        for (int i = 0; i < ms.length; i++) {
            MONTHS.put(ms[i], String.format("%02d", i+1));
        }
    }

    enum AccountType {
        MEDMERA_KONTO("konto_", "https://www.coop.se/Mina-sidor/Oversikt/MedMera-Konto/"),
        MEDMERA_MER("mer_", "https://www.coop.se/Mina-sidor/Oversikt/Kontoutdrag-MedMera-Mer/"),
        MEDMERA_VISA("visa_", "https://www.coop.se/Mina-sidor/Oversikt/Kontoutdrag-MedMera-Visa/");

        String prefix;
        String url;
        private AccountType(String prefix, String url) {
            this.prefix = prefix;
            this.url = url;
        }

        public String getPrefix() {
            return prefix;
        }

        public String getUrl() {
            return url;
        }
    }

    class TransactionParams {
        String pageGuid;
        String minDate;
        String maxDate;

        public String getPageGuid() {
            return pageGuid;
        }

        public void setPageGuid(String pageGuid) {
            this.pageGuid = pageGuid;
        }

        public String getMinDate() {
            return minDate;
        }

        public void setMinDate(String minDate) {
            this.minDate = minDate;
        }

        public String getMaxDate() {
            return maxDate;
        }

        public void setMaxDate(String maxDate) {
            this.maxDate = maxDate;
        }

        public boolean isValid() {
            return pageGuid != null && minDate != null && maxDate != null;
        }
    }

    private Pattern reViewState = Pattern.compile("__VIEWSTATE\"\\s+value=\"([^\"]+)\"");
 //   private Pattern reEventValidation = Pattern.compile("__EVENTVALIDATION\"\\s+value=\"([^\"]+)\"");
    private Pattern reBalance = Pattern.compile("saldo\">([^<]+)<", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private Pattern reTransactions = Pattern.compile("<td>\\s*(\\d{4}-\\d{2}-\\d{2})\\s*</td>\\s*<td>([^<]+)</td>\\s*<td>([^<]*)</td>\\s*<td>([^<]*)</td>\\s*<td[^>]*>(?:\\s*<a[^>]+>)?([^<]+)(?:</a>\\s*)?</td>", Pattern.CASE_INSENSITIVE);
    private Pattern rePageGuid = Pattern.compile("pageGuid\"\\s*:\\s*\"([^\"]+)", Pattern.CASE_INSENSITIVE);
    private ObjectMapper mObjectMapper;
    private String response;
    private String mToken;
    private String mUserId;
    private Map<AccountType, TransactionParams> mTransactionParams = new HashMap<AccountType, TransactionParams>();

    public Coop(Context context) {
        super(context);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
        super.STATIC_BALANCE = true;
    }

    public Coop(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
        urlopen = new Urllib(context, CertificateReader.getCertificates(context, R.raw.cert_coop, R.raw.cert_coop2));
        urlopen.addHeader("Origin", "https://www.coop.se");
        urlopen.addHeader("Referer", "https://www.coop.se/Mina-sidor/Logga-in-puffsida/?li=True");
        response = urlopen.open("https://www.coop.se/");
        Document d = Jsoup.parse(response);
        String pageGuid = d.select("input[name=pageGuid]").first().val();
        WebAuthenticateRequest webAuthReq = new WebAuthenticateRequest(pageGuid, username, password);
        urlopen.addHeader("Content-Type", "application/json");
        HttpEntity e = new StringEntity(getObjectmapper().writeValueAsString(webAuthReq));

        HttpResponse httpResponse = urlopen.openAsHttpResponse("https://www.coop.se/Services/PlainService.svc/JsonExecute", e, true);
        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            throw new BankException(res.getString(R.string.invalid_username_password));
        }

        LoginPackage lp = new LoginPackage(urlopen, null, response, "https://www.coop.se/Mina-sidor/Oversikt/");
        lp.setIsLoggedIn(true);
        return lp;
    }


    @Override
    public Urllib login() throws LoginException, BankException {
        try {
            //Coop MedMera Kort/Visa information and transactions are not available from the json api
            //so we'll have to login once to the web site and once to the api.
            LoginPackage lp = preLogin();
            if (!lp.isLoggedIn()) {
                throw new BankException(res.getString(R.string.invalid_username_password));
            }

            AuthenticateRequest authReq = new AuthenticateRequest(username, password, APPLICATION_ID);
            HttpEntity e = new StringEntity(getObjectmapper().writeValueAsString(authReq));
//            urlopen = new Urllib(context, CertificateReader.getCertificates(context, R.raw.cert_coop));
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
            response = urlopen.open("https://www.coop.se/Mina-sidor/Oversikt/Mina-poang/");
            Document dResponse = Jsoup.parse(response);
            Account poang = new Account("Poäng",
                    Helpers.parseBalance(dResponse.select(".Grid-cell--1 p").text()),
                    "poang", Account.OTHER, "");
            List<Transaction> transactions = new ArrayList<Transaction>();
            poang.setTransactions(transactions);
            for (Element e : dResponse.select(".Timeline-item")) {
                transactions.add(new Transaction(
                    formatDate(e.select(".Timeline-header .u-nbfcAlt span").text()),
                    e.select(".u-block").text(),
                    Helpers.parseBalance(e.select(".Timeline-header .Timeline-title").first().ownText()), ""));
            }
            accounts.add(poang);
            for (AccountType at : AccountType.values()) {
                response = urlopen.open(at.getUrl());
                Document d = Jsoup.parse(response);
                Elements historik = d.select("#historik section");
                TransactionParams params = new TransactionParams();
                mTransactionParams.put(at, params);
                if (historik != null && !historik.isEmpty()) {
                    String data = historik.first().attr("data-controller");
                    Matcher m = rePageGuid.matcher(data);
                    if (m.find()) {
                        params.setPageGuid(m.group(1));
                    }
                }
                Element date = d.getElementById("dateFrom");
                if (date != null) {
                    params.setMinDate(date.hasAttr("min") ? date.attr("min") : null);
                    params.setMaxDate(date.hasAttr("max") ? date.attr("max") : null);
                }
                Elements es = d.select(".List:contains(Saldo)");
                if (es != null && !es.isEmpty()) {
                    List<String> names = new ArrayList<String>();
                    List<String> values = new ArrayList<String>();
                    for (Element e : es.first().select("dt")) {
                        names.add(e.text().replaceAll(":", "").trim());
                    }
                    for (Element e : es.first().select("dd")) {
                        values.add(e.text().trim());
                    }
                    for (int i = 0; i < Math.min(names.size(), values.size()); i++) {
                        Account a = new Account(names.get(i), Helpers.parseBalance(values.get(i)), String.format("%s%d", at.getPrefix(), i));
                        a.setCurrency(Helpers.parseCurrency(values.get(i), "SEK"));
                        if (a.getName().toLowerCase().contains("disponibelt")) {
                            a.setType(Account.REGULAR);
                            balance = a.getBalance();
                            setCurrency(a.getCurrency());
                        } else {
                            a.setType(Account.OTHER);
                        }

                        if (i > 0) {
                            a.setAliasfor(String.format("%s%d", at.getPrefix(), 0));
                        }
                        accounts.add(a);
                    }
                }
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            throw new BankException(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new BankException(e.getMessage());
        }


        try {
            RefundSummaryRequest refsumReq = new RefundSummaryRequest(mUserId, mToken, APPLICATION_ID);
            HttpEntity e = new StringEntity(getObjectmapper().writeValueAsString(refsumReq));
            InputStream is = urlopen.openStream("https://www.coop.se/ExternalServices/RefundService.svc/RefundSummary", e, true);
            RefundSummaryResponse refsumResp = readJsonValue(is, RefundSummaryResponse.class);
            if (refsumResp != null && refsumResp.getRefundSummaryResult() != null) {
                Account a = new Account("Återbäring på ditt kort", BigDecimal.valueOf(refsumResp.getRefundSummaryResult().getAccountBalance()), "refsummary");
                a.setCurrency("SEK");
                if (accounts.isEmpty()) {
                    balance = a.getBalance();
                    setCurrency(a.getCurrency());
                }
                accounts.add(a);
                a = new Account(String.format("Återbäring för %s", refsumResp.getRefundSummaryResult().getMonthName()), BigDecimal.valueOf(refsumResp.getRefundSummaryResult().getTotalRefund()), "refsummary_month");
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

    @Override
    public void updateTransactions(Account account, Urllib urlopen) throws LoginException, BankException {
        AccountType at = getAccuntType(account.getId());
        TransactionParams tp = mTransactionParams.get(at);
        if (at == null || tp == null || !tp.isValid() || !isFirstAccountForType(account.getId())) return;
        try {
            String data = URLEncoder.encode(String.format("{\"page\":1,\"pageSize\":15,\"from\":\"%s\",\"to\":\"%s\"}", tp.getMinDate(), tp.getMaxDate()), "utf-8");
            String url = String.format("https://www.coop.se/Services/PlainService.svc/JsonExecuteGet?pageGuid=%s&method=GetTransactions&data=%s&_=%s", tp.getPageGuid(), data, System.currentTimeMillis());
            WebTransactionHistoryResponse transactionsResponse = getObjectmapper().readValue(urlopen.openStream(url), WebTransactionHistoryResponse.class);
            if (transactionsResponse != null && transactionsResponse.getModel() != null) {
                List<Transaction> transactions = new ArrayList<Transaction>();
                account.setTransactions(transactions);
                for (Result r : transactionsResponse.getModel().getResults()) {
                    StringBuilder title = new StringBuilder(!TextUtils.isEmpty(r.getLocation()) ? r.getLocation() : r.getTitle());
                    if (!TextUtils.isEmpty(r.getCardholder())) {
                        title.append(" (").append(r.getCardholder()).append(")");
                    }
                    if (r.getDate() != null) {
                        transactions.add(new Transaction(formatDate(r.getDate()), title.toString(), BigDecimal.valueOf(r.getSum())));
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatDate(String date) {
        String[] parts = date.split(" ");
        return String.format("%s-%s-%02d", parts[2], MONTHS.containsKey(parts[1].toLowerCase()) ? MONTHS.get(parts[1].toLowerCase()) : "01", Integer.parseInt(parts[0]));
    }

    private boolean isFirstAccountForType(String accountId) {
        for (AccountType at : AccountType.values()) {
            if (accountId.equals(String.format("%s%d", at.getPrefix(), 0))) {
                return true;
            }
        }
        return false;
    }

    private AccountType getAccuntType(String accountId) {
        for (AccountType at : AccountType.values()) {
            if (accountId.startsWith(at.getPrefix())) {
                return at;
            }
        }
        return null;
    }

}