package com.liato.bankdroid.banking.banks;

import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.Transaction;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.legacy.R;
import com.liato.bankdroid.provider.IBankTypes;

import org.apache.http.NameValuePair;
import org.apache.http.client.params.ClientPNames;
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

public class SveaDirekt extends Bank {

    private static final String NAME = "Svea Direkt";

    private static final String NAME_SHORT = "sveadirekt";

    private static final String URL = "https://http://www.sveadirekt.com/sv/swe//";

    private static final int BANKTYPE_ID = IBankTypes.SVEADIREKT;

    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;

    private static final int INPUT_TYPE_PASSWORD = InputType.TYPE_CLASS_TEXT;

    private static final String INPUT_HINT_USERNAME = "YYMMDDXXXX";


    private static final String BASE_URL = "https://services.sveadirekt.se/mypages/sv/";

    private static final String LOGIN_URL
            = "https://services.sveadirekt.se/mypages/sv/j_security_check";

    private static final String ACCOUNTS_URL
            = "https://services.sveadirekt.se/customerweb/mypages/save/index.page";

    private static final String TRANSACTIONS_URL
            = "https://services.sveadirekt.se/customerweb/mypages/save/account-details.page";

    private String response;

    public SveaDirekt(Context context) {
        super(context, R.drawable.logo_sveadirekt);

        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.URL = URL;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
        super.INPUT_TYPE_PASSWORD = INPUT_TYPE_PASSWORD;
        super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
    }

    public SveaDirekt(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        if (urlopen == null) {
            urlopen = new Urllib(context,
                    CertificateReader.getCertificates(context, R.raw.cert_sveadirekt));
            urlopen.getHttpclient().getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS,
                    true);
        }
        response = urlopen
                .open(BASE_URL);

        Document doc = Jsoup.parse(response);
        if (!"Logga in".equals(doc.title())) {
            throw new BankException(res.getText(R.string.unable_to_find)
                    .toString() + " login url.");
        }
        String strLoginUrl = LOGIN_URL;

        List<NameValuePair> postData = new ArrayList<>();
        postData.add(new BasicNameValuePair("j_username", getUsername()));
        postData.add(new BasicNameValuePair("j_password", getPassword()));
        return new LoginPackage(urlopen, postData, response, strLoginUrl);
    }

    @Override
    public Urllib login() throws LoginException, BankException, IOException {
        LoginPackage lp = preLogin();
        response = urlopen.open(LOGIN_URL, lp.getPostData());
        if (response.contains("error-failed-to-login")) {
            throw new LoginException(res.getText(
                    R.string.invalid_username_password).toString());
        }
        return urlopen;
    }

    @Override
    public void update() throws BankException, LoginException, BankChoiceException, IOException {
        super.update();
        if (getUsername().isEmpty() || getPassword().isEmpty()) {
            throw new LoginException(res.getText(
                    R.string.invalid_username_password).toString());
        }

        urlopen = login();

        response = urlopen.open(ACCOUNTS_URL);
        Document doc = Jsoup.parse(response);
        ArrayList<Account> accounts = parseAccounts(doc);
        for (Account account : accounts) {
            response = urlopen.open(TRANSACTIONS_URL + "?account=" + account.getId());
            account.setTransactions(parseTransactions(response));
        }
        this.setAccounts(accounts);
        super.updateComplete();
    }

    private ArrayList<Account> parseAccounts(Document pDocument) {
        ArrayList<Account> accountList = new ArrayList<>();
        Elements accounts = pDocument.select("table > tbody > tr");
        for (Element accountElement : accounts) {
            Account account = new Account(
                    accountElement.child(1).text(),
                    amountOf(accountElement.child(3).text()),
                    accountElement.child(0).text());
            accountList.add(account);
        }
        return accountList;
    }

    private List<Transaction> parseTransactions(String pResponse) {
        List<Transaction> vTransactions = new ArrayList<>();
        Document doc = Jsoup.parse(pResponse);
        Elements transactionElements =
                doc.select("table > tbody").get(1).children();

        for (Element element : transactionElements) {
            BigDecimal amount = amountOf(element.child(2).text());
            String description = element.child(1).text();
            if (description == null || description.isEmpty()) {
                description = amount.compareTo(BigDecimal.ZERO) > 0 ? "Insättning"
                        : "Uttag";
            }
            String date = element.child(0).text();
            vTransactions.add(new Transaction(date, description, amount));
        }
        return vTransactions;
    }

    private BigDecimal amountOf(String amount) {
        return new BigDecimal(amount
                .replaceAll("\\u2011", "-")
                .replaceAll("[^\\d-]", ""));
    }
}
