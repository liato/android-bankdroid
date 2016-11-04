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
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.legacy.R;
import com.liato.bankdroid.provider.IBankTypes;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.text.InputType;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;
import timber.log.Timber;

public class Jojo extends Bank {

    private static final String NAME = "Jojo Reskassa";

    private static final String URL = "https://www.skanetrafiken.se";

    private static final int BANKTYPE_ID = IBankTypes.JOJO;

    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_TEXT
            | +InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;

    private static final String NAME_NOT_SET = "KortnamnSaknas";

    private String response = null;

    public Jojo(Context context) {
        super(context, R.drawable.logo_jojo);

        super.url = URL;
        super.inputTitletextUsername = R.string.email;
        super.inputTypeUsername = INPUT_TYPE_USERNAME;
    }

    @Override
    public int getBanktypeId() {
        return BANKTYPE_ID;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public Jojo(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException,
            IOException {
        urlopen = new Urllib(context, CertificateReader.getCertificates(context, R.raw.cert_jojo));

        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("loginInputModel.Email", getUsername()));
        postData.add(new BasicNameValuePair("loginInputModel.Password", getPassword()));
        postData.add(new BasicNameValuePair("loginInputModel.Role", "Private"));
        return new LoginPackage(urlopen, postData, response, URL + "/inloggning/LoginPost/");
    }

    public Urllib login() throws LoginException, BankException, IOException {
        LoginPackage lp = preLogin();
        response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
        if (!response.contains("window.location")) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
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

        response = urlopen.open(URL + "/mitt-konto/se-saldo-och-ladda-kort/");

        Document document = Jsoup.parse(response);
        Elements elements = document.select(".card-content");
        for (Element element : elements) {
            accounts.add(toAccount(element));
        }

        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
        super.updateComplete();
    }

    private Account toAccount(Element card) {
        String cardNumber = card.select(".card-number").text().trim();
        BigDecimal balance = getBalance(cardNumber);
        String name = card.select(".title").text().trim();
        String displayName = NAME_NOT_SET.equals(name) ? cardNumber : name;
        return new Account(displayName, balance, cardNumber);
    }

    private BigDecimal getBalance(String cardNumber) {
        try {
            String balanceData = urlopen.open(URL +
                    "/mitt-konto/se-saldo-och-ladda-kort/GetCardBalance/?cardId=" + cardNumber);
            Document balanceDocument = Jsoup.parse(balanceData);
            Elements balance = balanceDocument.select(".balance");

            if (!balance.isEmpty()) {
                return Helpers.parseBalance(balance.first().text().trim());
            }
        } catch (IOException e) {
            // Ignore and defaults to zero
            Timber.w(e, "Getting Jojo card balance failed");
        }
        return BigDecimal.ZERO;
    }
}
