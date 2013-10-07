package com.liato.bankdroid.banking.banks;

import android.content.Context;
import android.text.Html;
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

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Meniga extends Bank{
    private static final String TAG = "Meniga";
    private static final String NAME = "Meniga";
    private static final String NAME_SHORT = "meniga";
    private static final String URL = "https://www.meniga.is/";
    private static final int BANKTYPE_ID = IBankTypes.MENIGA;
    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
    private static final String INPUT_HINT_USERNAME = "name@company.com";

    private Pattern reAccounts = Pattern.compile("\\?account=([^']+)'[^>]*>\\s*<div\\s*class=\"account-info\">[^<]*<span\\s*class=\"bold\">([^<]+)</span>\\s*(?:</div>\\s*<div\\s*class=\"account-status\">)\\s*<span\\s*class=\"(minus|plus)\">([^<]+)</span>");
    private Pattern reTransactions = Pattern.compile("\"Id\":([^,]*),.*?\"Text\":\"([^\"]*)\".*?\"OriginalDate\":\".?.?Date\\(([^\\)]*)\\).*?\"Amount\":([^,]*),");

    String response;

    public Meniga(Context context) {
        super(context);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.URL = URL;
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
        super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
        super.setCurrency("ISK");
    }

    public Meniga(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        urlopen = new Urllib(CertificateReader.getCertificates(context, R.raw.cert_meniga));
        urlopen.setContentCharset(HTTP.ISO_8859_1);
        response = urlopen.open("https://www.meniga.is/Mobile");
        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("email", username));
        postData.add(new BasicNameValuePair("password", password));
        return new LoginPackage(urlopen, postData, response, "https://www.meniga.is/Mobile");
    }

    @Override
    public Urllib login() throws LoginException, BankException {

        try {
            LoginPackage lp = preLogin();
            response = urlopen.open(lp.getLoginTarget(), lp.getPostData());

            if (response.contains("<div class=\"login\">")) {
                throw new LoginException(res.getText(R.string.invalid_username_password).toString());
            }
        }
        catch (ClientProtocolException e) {
            throw new BankException(e.getMessage());
        }
        catch (IOException e) {
            throw new BankException(e.getMessage());
        }

        try{
            response = urlopen.open("https://www.meniga.is/mobile/language/?lang=is-IS");
        }
        catch (ClientProtocolException e){
            //Do nothing
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
        urlopen = login();

        Matcher matcher;
        try {
            response = urlopen.open("https://www.meniga.is/Mobile/Accounts");
            matcher = reAccounts.matcher(response);
            while (matcher.find()) {
                /*
                 * Capture groups:
                 * GROUP                EXAMPLE DATA
                 * 1: Type              id
                 * 2: Name              accont
                 * 3: ----              plus or minus
                 * 4: Balance            5 678
                 *
                 */
                String balanceString ;
                balanceString = matcher.group(4) + ".00";
                Account account = new Account(Html.fromHtml(matcher.group(2)).toString(), Helpers.parseBalance(balanceString), matcher.group(1).trim());
                account.setCurrency("ISK");
                balance = balance.add(Helpers.parseBalance(matcher.group(4)));
                accounts.add(account);
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
        finally {
            super.updateComplete();
        }
    }

    @Override
    public void updateTransactions(Account account, Urllib urlopen) throws LoginException, BankException {
        super.updateTransactions(account, urlopen);
        if (account.getType() == Account.OTHER) return;

        String response;
        Matcher matcher;
        try {
            ArrayList<Transaction> transactions = new ArrayList<Transaction>();
            response = urlopen.open("https://www.meniga.is/Transactions?account="+account.getId());
            matcher = reTransactions.matcher(response);
            while (matcher.find()) {
                /*
                 * Capture groups:
                 * GROUP                    EXAMPLE DATA
                 * 1: Id                    1231213
                 * 2: Specification         Pressbyran
                 * 3: Date in millisec      2142411351235
                 * 4: Amount                -20
                 *
                 *
                 */
             Long date = Long.valueOf(matcher.group(3));
             SimpleDateFormat ft = new SimpleDateFormat ("yy-MM-dd");
             Transaction transaction = new Transaction(ft.format(date), Html.fromHtml(matcher.group(2)).toString().trim(), Helpers.parseBalance(matcher.group(4)));
             transaction.setCurrency("ISK");
             transactions.add(transaction);
            }
            account.setTransactions(transactions);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
