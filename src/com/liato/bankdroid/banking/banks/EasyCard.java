package com.liato.bankdroid.banking.banks;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.text.InputType;

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

public class EasyCard extends Bank {
    private static final String TAG = "EasyCard";
    private static final String NAME = "EasyCard";
    private static final String NAME_SHORT = "easycard";
    private static final String URL = "https://kundportal.cerdo.se/collectorpub/card/default.aspx";
    private static final int BANKTYPE_ID = IBankTypes.EASYCARD;

    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_NUMBER;
    private static final String INPUT_HINT_USERNAME = "XXXXXXXXX";

    private static final int INPUT_TYPE_PASSWORD = InputType.TYPE_CLASS_NUMBER;

    private Pattern reAccounts = Pattern.compile("<h2>MasterCard,\\s([0-9]*)[^:]*:[^:]*:[^:]*:[^:]*[^>]*>([0-9\\s,]*)[^:]*:[^:]*:[^:]*:[^>]*>([0-9\\s,]*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private Pattern reTransactions = Pattern.compile("<td\\snowrap>([0-9-]*)<\\/td><td\\snowrap>[^,]*,\\s([^,]*)[^<]*<\\/td><td\\snowrap\\sclass='cp-wp-td-right'>[^<]*<\\/td><td\\snowrap\\sclass='cp-wp-td-right'>([0-9\\s,]*)", Pattern.CASE_INSENSITIVE);
    private Pattern rePostData = Pattern.compile("<input\\stype=\"hidden\"\\sname=\"([0-9A-z_]*)\"\\sid=\"[0-9A-z_]*\"\\svalue=\"([^\"]*)\"\\s\\/>", Pattern.CASE_INSENSITIVE);

    private String response = null;

    public EasyCard(Context context) {
        super(context);

        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
        super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
        super.INPUT_TYPE_PASSWORD = INPUT_TYPE_PASSWORD;
    }

    public EasyCard(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException, ClientProtocolException, IOException {

        urlopen = new Urllib(true);
        response = urlopen.open(EasyCard.URL);
        List<NameValuePair> postData = new ArrayList<NameValuePair>();

        // Find hidden required fields.
        Matcher matcher = rePostData.matcher(response);

        // Populate hidden fields
        while (matcher.find()) {
            // 1 == name, 2 == value
            postData.add(new BasicNameValuePair(matcher.group(1).toString(), matcher.group(2).toString()));
        }

        // Our data + button value
        postData.add(new BasicNameValuePair("ctl00$m$g_2e67c657_c88c_44e4_9e93_48efac2dab20$accountNumber", username));
        postData.add(new BasicNameValuePair("ctl00$m$g_2e67c657_c88c_44e4_9e93_48efac2dab20$password", password));
        postData.add(new BasicNameValuePair("ctl00$m$g_2e67c657_c88c_44e4_9e93_48efac2dab20$ctl00", "Logga in"));

        return new LoginPackage(urlopen, postData, response, EasyCard.URL);
    }

    @Override
    public Urllib login() throws LoginException, BankException {

        try {
            LoginPackage lp = preLogin();
            response = urlopen.open(lp.getLoginTarget(), lp.getPostData());

            // The string "Valuta" is always present on successful login.
            if (!response.contains("Valuta")) {
                throw new LoginException(res.getText(R.string.invalid_username_password).toString());
            }

        } catch (ClientProtocolException e) {
            throw new BankException(e.getMessage());
        } catch (IOException e) {
            throw new BankException(e.getMessage());
        }

        return urlopen;
    }

    @Override
    public void update() throws BankException, LoginException, BankChoiceException {
        super.update();

        if (username == null || password == null || username.length() != 9 || password.length() == 0) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }

        urlopen = login();

        // Find account info
        Matcher matcher = reAccounts.matcher(response);

        if (matcher.find()) {
            // Our data!
            String account_number = matcher.group(2).toString().trim(); // 123123123
            BigDecimal credit_left_amount = Helpers.parseBalance(matcher.group(2).toString().trim()); // 3 748,87
            BigDecimal credit_spent_amount = Helpers.parseBalance(matcher.group(3).toString().trim()); // 1 083,63

            // Construct accounts
            Account credit_left = new Account("Kredit", credit_left_amount, account_number + ":left", Account.CCARD);
            Account credit_spent = new Account("└ Utnyttjad kredit", credit_spent_amount, account_number + ":spent", Account.OTHER);

            accounts.add(credit_left);
            accounts.add(credit_spent);
        }

        // No accounts found no profit...
        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }

        // Find transactions
        Matcher tMatcher = reTransactions.matcher(response);

        ArrayList<Transaction> credit_left_transactions = new ArrayList<Transaction>();
        ArrayList<Transaction> credit_spent_transactions = new ArrayList<Transaction>();

        while (tMatcher.find()) {
            String date = tMatcher.group(1).toString().trim(); // 2013-01-15
            String transaction = tMatcher.group(2).toString().trim(); // EBG HOSPITALITY
            BigDecimal amount = Helpers.parseBalance(tMatcher.group(3).toString()); // 214,00

            credit_left_transactions.add(new Transaction(date, transaction, amount.negate()));
            credit_spent_transactions.add(new Transaction(date, transaction, amount));
        }

        // Add transactions to account.
        for (Account account : accounts) {
            if (account.getType() == Account.CCARD) {
                account.setTransactions(credit_left_transactions);
            } else {
                account.setTransactions(credit_spent_transactions);
            }
        }

        super.updateComplete();
    }
}