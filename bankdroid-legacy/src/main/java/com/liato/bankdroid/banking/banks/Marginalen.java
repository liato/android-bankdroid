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

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.text.Html;
import android.text.InputType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class Marginalen extends Bank {

    private static final String TAG = "Marginalen";

    private static final String NAME = "Marginalen Bank";

    private static final String NAME_SHORT = "marginalen";

    private static final String BASE_URL = "https://secure1.marginalen.se/marginalen/";

    private static final int BANKTYPE_ID = IBankTypes.MARGINALEN;

    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;

    private static final String INPUT_HINT_USERNAME = "ÅÅMMDD-XXXX";

    String response;

    private Pattern reLoginLink = Pattern
            .compile("href=\"(engine\\?usecase=pin&[a-zA-Z0-9;=&._]+)");

    private Pattern reHash = Pattern.compile("name=\"hash\" value=\"([a-zA-Z0-9]+)\"");

    private Pattern reGuid = Pattern.compile("name=\"guid\" value=\"([a-zA-Z0-9]+)\"");

    private Pattern reAccountLink = Pattern
            .compile("href=\"(engine\\?[a-zA-Z0-9;=&._]+menuid=15[a-zA-Z0-9;=&._]+)\"");

    private Pattern reAccounts = Pattern.compile(
            "<td>\\s*([a-zA-ZåäöÅÄÖ0-9]+)\\s*</td>\\s*<td>\\s*<a href=\"(engine\\?usecase=account[a-zA-Z0-9;=&._]+)\">([0-9]+)</a>\\s*</td>\\s*<td class=\"aright\">\\s*([0-9.,]+)\\s*[a-zA-Z&;]+\\s*</td>");

    private Pattern reTransactions = Pattern.compile(
            "href=\"engine\\?usecase=transactiondetails.*tabindex=\"4\">([0-9\\-]+)</a>\\s*</td>\\s*<td>\\s*(.*?)\\s*</td>\\s*<td class=\"aright\">\\s*([\\-0-9\\.,]+)&nbsp;");

    private String accountUrl = "";

    public Marginalen(Context context) {
        super(context);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
        super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
    }

    public Marginalen(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        urlopen = new Urllib(context, CertificateReader
                .getCertificates(context, R.raw.cert_marginalen, R.raw.cert_marginalen2));
        urlopen.setContentCharset(HTTP.ISO_8859_1);
        Matcher matcher;
        response = urlopen.open(BASE_URL + "engine");
        matcher = reLoginLink.matcher(response);

        if (!matcher.find()) {
            throw new BankException(
                    res.getText(R.string.unable_to_find).toString() + " login link.");
        }
        String url = BASE_URL + matcher.group(1);
        url = url.replaceAll("&amp;", "&");
        response = urlopen.open(url);

        matcher = reHash.matcher(response);
        if (!matcher.find()) {
            throw new BankException(
                    res.getText(R.string.unable_to_find).toString() + " hash value.");
        }

        String hash = matcher.group(1);

        matcher = reGuid.matcher(response);
        if (!matcher.find()) {
            throw new BankException(
                    res.getText(R.string.unable_to_find).toString() + " GUID value.");
        }

        String guid = matcher.group(1);

        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("usecase", "base"));
        postData.add(new BasicNameValuePair("command", "formcommand"));
        postData.add(new BasicNameValuePair("commandorigin", "0.pin_logon_step1_view_handler"));
        postData.add(new BasicNameValuePair("guid", guid));
        postData.add(new BasicNameValuePair("hash", hash));
        postData.add(new BasicNameValuePair("userId", getUsername()));
        postData.add(new BasicNameValuePair("pin", getPassword()));

        return new LoginPackage(urlopen, postData, response, BASE_URL + "engine");
    }

    @Override
    public Urllib login() throws LoginException, BankException, IOException {
        LoginPackage lp = preLogin();
        response = urlopen.open(lp.getLoginTarget(), lp.getPostData());

        if (response.contains("Felmeddelande")) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }

        Matcher matcher;
        matcher = reAccountLink.matcher(response);
        if (!matcher.find()) {
            throw new BankException(
                    res.getText(R.string.unable_to_find).toString() + " accounts link.");
        }
        accountUrl = BASE_URL + matcher.group(1).replaceAll("&amp;", "&");

        return urlopen;
    }

    @Override
    public void update() throws BankException, LoginException, BankChoiceException, IOException {
        super.update();
        if (getUsername().isEmpty() || getPassword().isEmpty()) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }
        urlopen = login();
        response = urlopen.open(accountUrl);
        Matcher matcher = reAccounts.matcher(response);
        while (matcher.find()) {
            /*
             * Capture groups:
             * GROUP                EXAMPLE DATA
             * 1: Name              Högräntekonto
             * 2: URL               engine?usecase=account&amp;command=transactions&amp;guid=mCmupGvnAAJuC76MGuuRnwCC&amp;commandorigin=0.account_private_viewhandler&amp;account_table=0&amp;hash=Be2HxFargpk2m0BI2tShAACC
             * 3: ID                92351124972
             * 4: Amount            100.000,00
             *
             */
            Account account = new Account(Html.fromHtml(matcher.group(1)).toString(),
                    Helpers.parseBalance(matcher.group(4)), matcher.group(2),
                    Long.parseLong(matcher.group(3)));
            balance = balance.add(Helpers.parseBalance(matcher.group(4)));
            accounts.add(account);
        }
        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
        super.updateComplete();
    }

    public void updateTransactions(Account account, Urllib urlopen) throws LoginException,
            BankException, IOException {
        super.updateTransactions(account, urlopen);
        Matcher matcher;
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        response = urlopen.open(BASE_URL + account.getId().replaceAll("&amp;", "&"));

        matcher = reTransactions.matcher(response);
        while (matcher.find()) {
                /*
             * Capture groups:
             * GROUP                    EXAMPLE DATA
             * 1: Date                  2011-04-06
             * 2: Specification         Pressbyran
             * 3: Amount                -20
             *
             */
            transactions.add(new Transaction(matcher.group(1).trim(),
                    Html.fromHtml(matcher.group(2)).toString().trim(),
                    Helpers.parseBalance(matcher.group(3))));
        }
        account.setTransactions(transactions);
    }
}
