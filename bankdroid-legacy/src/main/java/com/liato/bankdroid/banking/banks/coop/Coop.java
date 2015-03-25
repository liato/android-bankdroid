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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.Transaction;
import com.liato.bankdroid.banking.banks.coop.model.web.Result;
import com.liato.bankdroid.banking.banks.coop.model.web.WebTransactionHistoryResponse;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.legacy.R;
import com.liato.bankdroid.provider.IBankTypes;

import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.text.TextUtils;

import java.io.IOException;
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

    private static final Map<String, String> MONTHS = new HashMap<>();

    static {
        String[] ms = new String[]{"januari", "februari", "mars", "april", "maj", "juni", "juli",
                "augusti", "september", "oktober", "november", "december"};
        for (int i = 0; i < ms.length; i++) {
            MONTHS.put(ms[i], String.format("%02d", i + 1));
        }
    }

    private final Pattern rePageGuid = Pattern
            .compile("pageGuid\"\\s*:\\s*\"([^\"]+)", Pattern.CASE_INSENSITIVE);

    private final Map<AccountType, TransactionParams> mTransactionParams = new HashMap<>();

    private ObjectMapper mObjectMapper;

    private String response;

    public Coop(Context context) {
        super(context);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
        super.STATIC_BALANCE = true;
    }

    public Coop(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException,
            IOException {
        urlopen = new Urllib(context, CertificateReader.getCertificates(context, R.raw.cert_coop));
        urlopen.addHeader("X-Requested-With", "XMLHttpRequest");
        HttpResponse httpResponse = urlopen
                .openAsHttpResponse("https://www.coop.se/Personliga-Baren/Logga-in/?method=Login",
                        new StringEntity("{\"isBar\":\"true\",\"username\":\"" + username
                                + "\",\"password\":\"" + password + "\"}"),
                        true);
        urlopen.removeHeader("X-Requested-With");
        LoginPackage lp = new LoginPackage(urlopen, null, response,
                "https://www.coop.se/Mina-sidor/Oversikt/");
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            lp.setIsLoggedIn(true);
        }
        return lp;
    }

    @Override
    public Urllib login() throws LoginException, BankException, IOException {
        LoginPackage lp = preLogin();
        if (!lp.isLoggedIn()) {
            throw new LoginException(res.getString(R.string.invalid_username_password));
        }
        return urlopen;
    }

    @Override
    public void update() throws BankException, LoginException, BankChoiceException, IOException {
        super.update();
        if (username == null || password == null || username.length() == 0
                || password.length() == 0) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }

        login();

        response = urlopen.open("https://www.coop.se/Mina-sidor/Oversikt/Mina-poang/");
        Document dResponse = Jsoup.parse(response);
        Account poang = new Account("\u2014  Poäng",
                Helpers.parseBalance(dResponse.select(".Grid-cell--1 p").text()),
                "poang", Account.OTHER, "");
        List<Transaction> transactions = new ArrayList<>();
        poang.setTransactions(transactions);
        for (Element e : dResponse.select(".Timeline-item")) {
            try {
                if (e.parent().hasClass("Timeline-group--emphasized")) {
                    transactions.add(new Transaction(
                            formatDate(e.ownText()),
                            e.select(".Timeline-label").text(),
                            Helpers.parseBalance(e.select(".Timeline-title").first().text()), ""));

                } else {
                    transactions.add(new Transaction(
                            formatDate(e.select(".Timeline-header .u-nbfcAlt span").text()),
                            e.select(".u-block").text(),
                            Helpers.parseBalance(
                                    e.select(".Timeline-header .Timeline-title").first().ownText()),
                            ""));
                }
            } finally {
                continue;
            }
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
                List<String> names = new ArrayList<>();
                List<String> values = new ArrayList<>();
                for (Element e : es.first().select("dt")) {
                    names.add(e.text().replaceAll(":", "").trim());
                }
                for (Element e : es.first().select("dd")) {
                    values.add(e.text().trim());
                }
                for (int i = 0; i < Math.min(names.size(), values.size()); i++) {
                    Account a = new Account(names.get(i), Helpers.parseBalance(values.get(i)),
                            String.format("%s%d", at.getPrefix(), i));
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

        response = urlopen
                .open("https://www.coop.se/Mina-sidor/Oversikt/Information-om-aterbaringen/");
        dResponse = Jsoup.parse(response);
        Elements refound = dResponse.select(".Heading--coopNew");
        if (refound.hasText()) {
            BigDecimal balance = Helpers.parseBalance(refound.text());
            if (balance.compareTo(BigDecimal.ZERO) >= 0) {
                Account a = new Account("Återbäring", balance, "refound", Account.OTHER, "SEK");
                accounts.add(a);
            }
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

    @Override
    public void updateTransactions(Account account, Urllib urlopen)
            throws LoginException, BankException, IOException {
        AccountType at = getAccuntType(account.getId());
        TransactionParams tp = mTransactionParams.get(at);
        if (at == null || tp == null || !tp.isValid() || !isFirstAccountForType(account.getId())) {
            return;
        }

        String data = URLEncoder.encode(String
                .format("{\"page\":1,\"pageSize\":15,\"from\":\"%s\",\"to\":\"%s\"}",
                        tp.getMinDate(), tp.getMaxDate()), "utf-8");
        String url = String
                .format("https://www.coop.se/Services/PlainService.svc/JsonExecuteGet?pageGuid=%s&method=GetTransactions&data=%s&_=%s",
                        tp.getPageGuid(), data, System.currentTimeMillis());
        WebTransactionHistoryResponse transactionsResponse = getObjectmapper()
                .readValue(urlopen.openStream(url), WebTransactionHistoryResponse.class);
        if (transactionsResponse != null && transactionsResponse.getModel() != null) {
            List<Transaction> transactions = new ArrayList<>();
            account.setTransactions(transactions);
            for (Result r : transactionsResponse.getModel().getResults()) {
                StringBuilder title = new StringBuilder(
                        !TextUtils.isEmpty(r.getLocation()) ? r.getLocation() : r.getTitle());
                if (!TextUtils.isEmpty(r.getCardholder())) {
                    title.append(" (").append(r.getCardholder()).append(")");
                }
                if (r.getDate() != null) {
                    transactions.add(new Transaction(formatDate(r.getDate()), title.toString(),
                            BigDecimal.valueOf(r.getSum())));
                }
            }
        }
    }

    private String formatDate(String date) {
        String[] parts = date.split(" ");
        if (parts.length < 3) {
            return "";
        }
        return String.format("%s-%s-%02d", parts[2],
                MONTHS.containsKey(parts[1].toLowerCase()) ? MONTHS.get(parts[1].toLowerCase())
                        : "01", Integer.parseInt(parts[0]));
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

    enum AccountType {
        MEDMERA_KONTO("konto_", "https://www.coop.se/Mina-sidor/Oversikt/MedMera-Konto/"),
        MEDMERA_EFTER("efter_",
                "https://www.coop.se/Mina-sidor/Oversikt/Kontoutdrag-MedMera-Efter/"),
        MEDMERA_EFTER1("efter1_",
                "https://www.coop.se/Mina-sidor/Oversikt/Kontoutdrag-MedMera-Efter1/"),
        MEDMERA_FORE("fore_", "https://www.coop.se/Mina-sidor/Oversikt/Kontoutdrag-MedMera-Fore/"),
        MEDMERA_MER("mer_", "https://www.coop.se/Mina-sidor/Oversikt/Kontoutdrag-MedMera-Mer/"),
        MEDMERA_VISA("visa_", "https://www.coop.se/Mina-sidor/Oversikt/Kontoutdrag-MedMera-Visa/");

        final String prefix;

        final String url;

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
}
