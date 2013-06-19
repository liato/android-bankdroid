package com.liato.bankdroid.banking.banks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.text.Html;
import android.text.InputType;
import android.util.Log;

import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.Transaction;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;

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
    private Pattern reFallbackQuery = Pattern.compile("fallbackQuery\"\\svalue=\"(.*)\"", Pattern.MULTILINE);

    private Pattern reAccountIds = Pattern.compile("ransactions\\?cvokey=(\\d+)", Pattern.MULTILINE);
    private Pattern reAccountNumbers = Pattern.compile("account_number.*\\>(\\d+)", Pattern.MULTILINE);
    private Pattern reAccountName = Pattern.compile("account_name.*\\>(.+)<", Pattern.MULTILINE);
    private Pattern reAccountBalance = Pattern.compile("balance.*\\>(\\d+,\\d\\d)", Pattern.MULTILINE);
    private Pattern reDisposable = Pattern.compile("disposable.*\\>(\\d+,\\d\\d)", Pattern.MULTILINE);

    private Pattern reTransactions = Pattern.compile("item\\stransaction.+?(\\d{4}-\\d{2}-\\d{2}?).+?(-?\\d+,\\d{2}).*?left\">\\s+(.+?)\\s+</div>", Pattern.MULTILINE | Pattern.DOTALL);

    private HashMap<String, String> mIdMappings = new HashMap<String, String>();

    public ForexBank(Context context) {
        super(context);
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

    public ForexBank(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
        urlopen = new Urllib();
        String baseUrl = "https://nettbank.edb.com";
        String res = urlopen.open(baseUrl + "/mobilepayment/index.jsp?n_bank=0087&nativeapp=android");
        String formAction = "";
        String tranId = "";
        String fallbackQuery = "";

        // Get post action
        Matcher mFormAction = reFormAction.matcher(res);
        if(mFormAction.find())
        {
            formAction = mFormAction.group(1);
            //System.err.println(mFormAction.group(1));
        }

        // Get transaction id hidden param
        Matcher mTranId = reTranId.matcher(res);
        if(mTranId.find())
        {
            tranId = mTranId.group(1);
            //System.err.println(mTranId.group(1));
        }

        // Weird param, lets include it!
        Matcher mFallbackQuery = reFallbackQuery.matcher(res);
        if(mFallbackQuery.find())
        {
            fallbackQuery = mFallbackQuery.group(1);
            //System.err.println(mTranId.group(1));
        }

        // Assemble param table
        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("p_tranid", tranId));
        postData.add(new BasicNameValuePair("user_id", username));
        postData.add(new BasicNameValuePair("password", password));
        postData.add(new BasicNameValuePair("submitButton", "Logga in"));
        postData.add(new BasicNameValuePair("forcelayout", "touch"));
        postData.add(new BasicNameValuePair("fallbackQuery", fallbackQuery));
        postData.add(new BasicNameValuePair("p_errorScreen", "LOGON_REPOST_ERROR"));

        return new LoginPackage(urlopen, postData, null, formAction);
    }

    public Urllib login() throws LoginException, BankException {
        try {
            LoginPackage lp = preLogin();

            // Post
            HttpResponse httpResponse = urlopen.openAsHttpResponse(BASE_URL + lp.getLoginTarget(), lp.getPostData(), false);

            String result = EntityUtils.toString(httpResponse.getEntity());

            if(!result.contains("/mobilepayment/transigo/logon/logout")) {
                throw new LoginException(res.getText(R.string.invalid_username_password).toString());
            }
        }
        catch (ClientProtocolException e) {
            Log.e(TAG, "ClientProtocolException: " + e.getMessage());
            throw new BankException(e.getMessage());
        }
        catch (IOException e) {
            Log.e(TAG, "IOException: "+e.getMessage());
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

        try {
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
            result = result.replace("&nbsp;",""); // Remove non-breaking spaces, they fuck up balances

            Matcher mAccountIds = reAccountIds.matcher(result);
            Matcher mAccountNumbers = reAccountNumbers.matcher(result);
            Matcher mAccountNames = reAccountName.matcher(result);
            Matcher mAccountBalances = reAccountBalance.matcher(result);
            Matcher mDisposables = reDisposable.matcher(result);

            if(mAccountIds.find() && mAccountNumbers.find() && mAccountNames.find() && mAccountBalances.find() && mDisposables.find())  {
                for (int i = 0; i < mAccountNumbers.groupCount() ; i++) {
                    mIdMappings.put(Integer.toString(i+1), mAccountIds.group(i+1));
                    accounts.add(new Account(Html.fromHtml(mAccountNames.group(i+1)).toString().trim() + " (Disponibelt)", Helpers.parseBalance(mDisposables.group(i+1).trim()), Integer.toString(i+1)));
                    Account account = new Account(Html.fromHtml(mAccountNames.group(i+1)).toString().trim() + " (Saldo)", Helpers.parseBalance(mAccountBalances.group(i+1).trim()), "a:" + i+1);
                    account.setAliasfor(Integer.toString(i+1));
                    accounts.add(account);

                    balance = balance.add(Helpers.parseBalance(mAccountBalances.group(i+1)));
                }
            }
            if (accounts.isEmpty()) {
                throw new BankException(res.getText(R.string.no_accounts_found).toString());
            }
        }
        catch (ClientProtocolException e) {
            throw new BankException(e.getMessage());
        }
        catch (IOException e) {
            throw new BankException(e.getMessage());
        }
    }

    @Override
    public void updateTransactions(Account account, Urllib urlopen) throws LoginException, BankException {
        super.updateTransactions(account, urlopen);
        if (account.getId().startsWith("a:") || !mIdMappings.containsKey(account.getId())) return; // No transactions for "saldo"-accounts
        String accountId = mIdMappings.get(account.getId());
        String response = null;
        Matcher matcher;
        try {
            response = urlopen.open(BASE_URL + "/mobilepayment/transigo/account/overview/accountTransactions?cvokey=" + accountId);
            response = response.replace("&nbsp;", "");
            matcher = reTransactions.matcher(response);
            ArrayList<Transaction> transactions = new ArrayList<Transaction>();
            while (matcher.find()) {
                transactions.add(new Transaction(matcher.group(1).trim(), matcher.group(3).trim(), Helpers.parseBalance(matcher.group(2))));
            }
            account.setTransactions(transactions);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            super.updateComplete();
        }
    }
}
