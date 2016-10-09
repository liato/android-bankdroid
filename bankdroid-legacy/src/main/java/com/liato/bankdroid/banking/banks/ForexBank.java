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

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

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

public class ForexBank extends Bank {

    // Logon url: https://nettbank.edb.com/mobilepayment/index.jsp?n_bank=0087&nativeapp=android
    private static final String TAG = "ForexBank";

    private static final String NAME = "Forex Bank";

    private static final String NAME_SHORT = "forex";

    private static final String URL = "https://www.forex.se/";

    private static final int BANKTYPE_ID = IBankTypes.FOREX;

    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;

    private static final int INPUT_TYPE_PASSWORD = InputType.TYPE_CLASS_PHONE;

    private static final String INPUT_HINT_USERNAME = "ÅÅMMDDXXXX";

    private static final boolean STATIC_BALANCE = false;

    private static final String BASE_URL = "https://nettbank.edb.com";

    private Pattern reFormAction = Pattern.compile("form action=\"(.*)\"\\s", Pattern.MULTILINE);

    private Pattern reTranId = Pattern.compile("p_tranid\" value=\"(\\d+)\"", Pattern.MULTILINE);

    private Pattern reFallbackQuery = Pattern.compile("fallbackQuery\"\\svalue=\"(.*)\"",
            Pattern.MULTILINE);

    private Pattern reAccountIds = Pattern.compile("ransactions\\?cvokey=(.\\d+)",
            Pattern.MULTILINE);

    private Pattern reAccountNumbers = Pattern.compile("account_number.*\\>(\\d+)",
            Pattern.MULTILINE);

    private Pattern reAccountName = Pattern.compile("account_name.*\\>(.+)<", Pattern.MULTILINE);

    private Pattern reAccountBalance = Pattern.compile("balance.*\\>(\\d+,\\d\\d)",
            Pattern.MULTILINE);

    private Pattern reDisposable = Pattern.compile("disposable.*\\>(\\d+,\\d\\d)",
            Pattern.MULTILINE);

    private Pattern reTransactions = Pattern.compile(
            "item\\stransaction.+?(\\d{4}-\\d{2}-\\d{2}?).+?(-?\\d+,\\d{2}).*?left\">\\s+(.+?)\\s+</div>",
            Pattern.MULTILINE | Pattern.DOTALL);

    public ForexBank(Context context) {
        super(context, R.drawable.logo_forex);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
        super.INPUT_TYPE_PASSWORD = INPUT_TYPE_PASSWORD;
        super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
        super.STATIC_BALANCE = STATIC_BALANCE;
    }

    public ForexBank(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        urlopen = new Urllib(context,
                CertificateReader.getCertificates(context, R.raw.cert_forexbank));
        String baseUrl = "https://nettbank.edb.com";
        String res = urlopen.open(
                baseUrl + "/mobilepayment/index.jsp?n_bank=0087&nativeapp=android");
        String formAction = "";
        String tranId = "";
        String fallbackQuery = "";

        // Get post action
        Matcher mFormAction = reFormAction.matcher(res);
        if (mFormAction.find()) {
            formAction = mFormAction.group(1);
            //System.err.println(mFormAction.group(1));
        }

        // Get transaction id hidden param
        Matcher mTranId = reTranId.matcher(res);
        if (mTranId.find()) {
            tranId = mTranId.group(1);
            //System.err.println(mTranId.group(1));
        }

        // Weird param, lets include it!
        Matcher mFallbackQuery = reFallbackQuery.matcher(res);
        if (mFallbackQuery.find()) {
            fallbackQuery = mFallbackQuery.group(1);
            //System.err.println(mTranId.group(1));
        }

        // Assemble param table
        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("p_tranid", tranId));
        postData.add(new BasicNameValuePair("user_id", getUsername()));
        postData.add(new BasicNameValuePair("password", getPassword()));
        postData.add(new BasicNameValuePair("submitButton", "Logga in"));
        postData.add(new BasicNameValuePair("forcelayout", "touch"));
        postData.add(new BasicNameValuePair("fallbackQuery", fallbackQuery));
        postData.add(new BasicNameValuePair("p_errorScreen", "LOGON_REPOST_ERROR"));

        return new LoginPackage(urlopen, postData, null, formAction);
    }

    public Urllib login() throws LoginException, BankException, IOException {
        LoginPackage lp = preLogin();

        // Post
        HttpResponse httpResponse = urlopen.openAsHttpResponse(BASE_URL + lp.getLoginTarget(),
                lp.getPostData(), false);

        String result = EntityUtils.toString(httpResponse.getEntity());

        if (!result.contains("/mobilepayment/transigo/logon/logout")) {
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
        /*
        "account_number.*\\>(\\d+)"; // Kontonummer
        "account_name.*\\>(.+)<" // Kontonamn
        "balance.*\>(\d+,\d\d)" // Saldo
        "disposable.*\>(\d+,\d\d)" // Disponibelt

        System.err.println("Kontonummer " + mAccountNumbers.group(i+1));
        System.err.println("Kontonamn " + mAccountNames.group(i+1));
        System.err.println("Saldo " + mAccountBalances.group(i+1));
        System.err.println("Disponibelt " + mDisposables.group(i+1));
         */
        urlopen = login();

        // Go to main menu
        String result = urlopen.open("https://nettbank.edb.com/mobilepayment/transigo/menu/menu1");
        result = result.replace("&nbsp;", ""); // Remove non-breaking spaces, they fuck up balances

        Matcher mAccountIds = reAccountIds.matcher(result);
        Matcher mAccountNumbers = reAccountNumbers.matcher(result);
        Matcher mAccountNames = reAccountName.matcher(result);
        Matcher mAccountBalances = reAccountBalance.matcher(result);
        Matcher mDisposables = reDisposable.matcher(result);

        while (mAccountIds.find() && mAccountNumbers.find() && mAccountNames.find()
                && mAccountBalances.find()) {
            if (!mAccountIds.group(1).startsWith("-") && mDisposables.find()) {
                accounts.add(new Account(Html.fromHtml(mAccountNames.group(1)).toString().trim(),
                        Helpers.parseBalance(mDisposables.group(1).trim()),
                        mAccountIds.group(1))); //Disponibelt
            } else {
                accounts.add(new Account(Html.fromHtml(mAccountNames.group(1)).toString().trim(),
                        Helpers.parseBalance(mAccountBalances.group(1).trim()),
                        mAccountIds.group(1)));
            }

            balance = balance.add(Helpers.parseBalance(mAccountBalances.group(1)));
        }
        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }
        super.updateComplete();
    }

    @Override
    public void updateTransactions(Account account, Urllib urlopen) throws LoginException,
            BankException, IOException {
        super.updateTransactions(account, urlopen);
        String accountId = account.getId();
        Matcher matcher;
        String response = urlopen.open(BASE_URL
                + "/mobilepayment/transigo/account/overview/accountTransactions?cvokey="
                + accountId);
        response = response.replace("&nbsp;", "");
        matcher = reTransactions.matcher(response);
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        while (matcher.find()) {
            transactions.add(new Transaction(matcher.group(1).trim(), matcher.group(3).trim(),
                    Helpers.parseBalance(matcher.group(2))));
        }
        account.setTransactions(transactions);
    }
}
