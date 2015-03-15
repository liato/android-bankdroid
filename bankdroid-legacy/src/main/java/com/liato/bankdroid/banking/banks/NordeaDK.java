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

import android.content.Context;
import android.text.Html;
import android.text.InputType;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class NordeaDK extends Bank {

    private static final String TAG = "NordeaDK";

    private static final String NAME = "Nordea DK";

    private static final String NAME_SHORT = "nordea_dk";

    private static final String URL = "https://m.nordea.dk/";

    private static final int BANKTYPE_ID = IBankTypes.NORDEA_DK;

    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;

    private static final int INPUT_TYPE_PASSWORD = InputType.TYPE_CLASS_PHONE;

    private static final String INPUT_HINT_USERNAME = "DDMMÅÅ-XXXX";

    private final String currency = "DKK";

    private String prefix;

    private String referer;

    private Pattern reLoginUrl = Pattern.compile(
            "<input.*?name=\"prefix\".*?value=\"([^\"]+)\"",
            Pattern.CASE_INSENSITIVE);

    private Pattern reAccounts = Pattern
            .compile(
                    "<a.*?href=\"(.*?AccountTransactions.*?productidx=([0-9]+).*?)[^>]+>\\s*<span[^>]+>(.*?)</span>\\s*<span[^>]+>\\s*(.*?)</span>",
                    Pattern.CASE_INSENSITIVE);

    private Pattern reTransactions = Pattern
            .compile(
                    "<tr [^>]+>\\s*<td.*?[^>]+>\\s*(.*?)[.]{1}\\s*</td>\\s*<td[^>]*>\\s*(.*?)\\s*</td>\\s*<td[^>]+>\\s*(.*?)\\s*</td>\\s*</tr>",
                    Pattern.CASE_INSENSITIVE);

    private Pattern rePrefix = Pattern.compile(
            "<a id=\"logout\" href=.*?prefix=([0-9-]+)",
            Pattern.CASE_INSENSITIVE);

    private Pattern reOldTransactions = Pattern
            .compile("<a.*?class=\".*?prev_btn.*?\".*?href=\".*?AccountTransactions",
                    Pattern.CASE_INSENSITIVE);

    private Pattern reTransactionYear = Pattern
            .compile("<h3[^>]+>.*?([0-9]{4})</h3>", Pattern.CASE_INSENSITIVE);

    private String response;

    public NordeaDK(Context context) {
        super(context);

        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
        super.INPUT_TYPE_PASSWORD = INPUT_TYPE_PASSWORD;
        super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
        super.currency = "DKK";
        this.response = null;
        this.prefix = "";
    }

    public NordeaDK(String username, String password, Context context)
            throws BankException, LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        if (urlopen == null) {
            urlopen = new Urllib(context,
                    CertificateReader.getCertificates(context, R.raw.cert_nordea_dk));
        }
        response = urlopen
                .open("https://www.netbank.nordea.dk/mnetbank/index.jsp");
        response = urlopen.open("https://www.netbank.nordea.dk/mnetbank/logon.jsp");
        Matcher matcher = reLoginUrl.matcher(response);
        if (!matcher.find()) {
            throw new BankException(res.getText(R.string.unable_to_find)
                    .toString() + " login url.");
        }
        String strLoginUrl = "https://www.netbank.nordea.dk/mnetbank/servlet/Logon";
        this.referer = strLoginUrl;
        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("user_id", username));
        postData.add(new BasicNameValuePair("logon_code", password));
        postData.add(new BasicNameValuePair("command", "1"));
        this.prefix = matcher.group(1);
        postData.add(new BasicNameValuePair("prefix", matcher.group(1)));
        return new LoginPackage(urlopen, postData, response, strLoginUrl);
    }

    @Override
    public Urllib login() throws LoginException, BankException, IOException {
        LoginPackage lp = preLogin();
        response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
        if (response.contains("class=\"icon error_icon\"")) {
            throw new LoginException(res.getText(
                    R.string.invalid_username_password).toString());
        }
        this.updatePrefix();
        return urlopen;
    }

    @Override
    public void update() throws BankException, LoginException,
            BankChoiceException, IOException {
        super.update();
        if (username == null || password == null || username.length() == 0
                || password.length() == 0) {
            throw new LoginException(res.getText(
                    R.string.invalid_username_password).toString());
        }

        urlopen = login();
        Matcher matcher = reAccounts.matcher(response);

        if (!matcher.find()) {
            throw new BankException(res.getText(R.string.unable_to_find)
                    .toString() + " accounts url.");
        }
        matcher.reset();
        while (matcher.find()) {
                        /*
			 * Capture groups:
			 * GROUP 		EXAMPLE DATA
			 * 1: Link		https://www.netbank.nordea.dk/mnetbank/servlet/AccountTransactions
			 * 2: ID 		0
			 * 3: Name 		Check-in-konto
			 * 4: Amount	1.234,56
			 */
            String name = Html.fromHtml(matcher.group(3)).toString().trim();
            String id = matcher.group(2).toString().trim();
            BigDecimal amount = Helpers.parseBalance(matcher.group(4));

            accounts.add(new Account(name, amount, id, Account.REGULAR,
                    this.currency));
            balance = balance.add(amount);

        }
        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found)
                    .toString());
        }
        this.updatePrefix();
        super.updateComplete();
    }

    @Override
    public void updateTransactions(Account account, Urllib urlopen)
            throws LoginException, BankException, IOException {
        super.updateTransactions(account, urlopen);

        ArrayList<Transaction> transactions = new ArrayList<Transaction>();

        transactions.addAll(updateMonthTransactions(account, urlopen, false));

        //Get last months' transactions
        Matcher oldTransactionMatcher = reOldTransactions.matcher(response);
        if (oldTransactionMatcher.find()) {
            transactions.addAll(updateMonthTransactions(account, urlopen, true));
        }

        account.setTransactions(transactions);

    }

    private ArrayList<Transaction> updateMonthTransactions(Account account,
            Urllib urlopen, boolean oldTransactions) throws BankException, IOException {
        String command = "command=";
        command += (oldTransactions) ? "1" : "0";
        String url =
                "https://www.netbank.nordea.dk/mnetbank/servlet/AccountTransactions?productidx="
                        + account.getId() + "&prefix=" + this.prefix + "&" + command;

        ArrayList<Transaction> transactions = new ArrayList<Transaction>();

        Matcher matcher;
        urlopen.addHeader("referer", this.referer);
        this.response = urlopen.open(url);
        this.updatePrefix();
        this.referer = url;

        Matcher transYear = reTransactionYear.matcher(response);
        String year = "";
        if (transYear.find()) {
            year = Html.fromHtml(transYear.group(1)).toString().trim();
        }

        matcher = reTransactions.matcher(response);
		/*
		 * Capture groups:
		 * GROUP 	EXAMPLE 	DATA
		 * 1: 		Date 		29.07
		 * 2: 		Transaction	Bgs Check-in-konto
		 * 3: 		Amount 		906.56
		 */

        while (matcher.find()) {
            String monthDate = Html.fromHtml(matcher.group(1)).toString().trim();
            String text = Html.fromHtml(matcher.group(2)).toString().trim();
            BigDecimal amount = Helpers.parseBalance(matcher.group(3));
            String date = year + "-" + monthDate.substring(3, 5) + "-" + monthDate.substring(0, 2);

            Transaction transaction = new Transaction(date, text, amount,
                    super.currency);
            transactions.add(transaction);
        }
        return transactions;
    }

    private void updatePrefix() throws BankException {
        Matcher matcher = rePrefix.matcher(this.response);
        if (!matcher.find()) {
            throw new BankException(res.getText(R.string.unable_to_find)
                    .toString());
        }
        this.prefix = Html.fromHtml(matcher.group(1)).toString().trim();
    }
}
