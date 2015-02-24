package com.liato.bankdroid.banking.banks;

import android.content.Context;
import android.text.InputType;
import android.util.Log;

import com.liato.bankdroid.legacy.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Transaction;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import eu.nullbyte.android.urllib.CertificateReader;
import eu.nullbyte.android.urllib.Urllib;

public class SveaDirekt extends Bank {
    private static final String TAG = "SveaDirekt";
    private static final String NAME = "Svea Direkt";
    private static final String NAME_SHORT = "sveadirekt";
    private static final String URL = "https://http://www.sveadirekt.com/sv/swe//";
    private static final int BANKTYPE_ID = IBankTypes.SVEADIREKT;
    private static final int INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_PHONE;
    private static final int INPUT_TYPE_PASSWORD = InputType.TYPE_CLASS_TEXT;
    private static final String INPUT_HINT_USERNAME = "YYMMDDXXXX";


    private static final String BASE_URL = "https://services.sveadirekt.se/mypages/sv/";
    private static final String LOGIN_URL = "https://services.sveadirekt.se/mypages/sv/j_security_check";
    private static final String ACCOUNTS_URL = "https://services.sveadirekt.se/faces/WEB-INF/britney_jsp_s/home.jsp";
    private static final String TRANSACTIONS_URL = "https://services.sveadirekt.se/faces/WEB-INF/britney_jsp_s/balance.jsp";

    private String response;

    public SveaDirekt(Context context) {
        super(context);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.URL = URL;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
        super.INPUT_TYPE_PASSWORD = INPUT_TYPE_PASSWORD;
        super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
    }

    public SveaDirekt(String username, String password, Context context) throws BankException, LoginException, BankChoiceException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException,
            ClientProtocolException, IOException {
        if (urlopen == null) {
            urlopen = new Urllib(context, CertificateReader.getCertificates(context, R.raw.cert_sveadirekt));
            urlopen.getHttpclient().getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
        }
        response = urlopen
                .open(BASE_URL);

        Document doc = Jsoup.parse(response);
        if (!"Logga in".equals(doc.title())) {
            throw new BankException(res.getText(R.string.unable_to_find)
                    .toString() + " login url.");
        }
        String strLoginUrl = LOGIN_URL;

        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("j_username", username));
        postData.add(new BasicNameValuePair("j_password", password));
        return new LoginPackage(urlopen, postData, response, strLoginUrl);
    }

    @Override
    public Urllib login() throws LoginException, BankException {
        try {
            LoginPackage lp = preLogin();
            response = urlopen.open(LOGIN_URL, lp.getPostData());
            if (response.contains("error-failed-to-login")) {
                throw new LoginException(res.getText(
                        R.string.invalid_username_password).toString());
            }

        } catch (ClientProtocolException e) {
            Log.e(TAG, "ClientProtocolException: " + e.getMessage());
            throw new BankException(e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
            throw new BankException(e.getMessage());
        }
        return urlopen;
    }

    @Override
    public void update() throws BankException, LoginException, BankChoiceException {
        super.update();
        if (username == null || password == null || username.length() == 0
                || password.length() == 0) {
            throw new LoginException(res.getText(
                    R.string.invalid_username_password).toString());
        }

        urlopen = login();
        try {
            List<NameValuePair> postData = new ArrayList<NameValuePair>();
            postData.add(new BasicNameValuePair("homeForm:balance","Saldo"));
            postData.add(new BasicNameValuePair("homeForm","homeForm"));
            response = urlopen.open(ACCOUNTS_URL,postData);
            Document doc = Jsoup.parse(response);
            ArrayList<Account> accounts = parseAccounts(doc);

            if (!accounts.isEmpty()) {
                Account firstAccount = accounts.get(0);
                // Get account details for first account
                addAccountDetails(firstAccount, doc);
                firstAccount.setTransactions(parseTransactions(response));

            }

            // Fetch additional accounts transaction pages to get their balance.
            for (int i = 1; i < accounts.size(); i++) {
                Account account = accounts.get(i);
                response = urlopen.open(TRANSACTIONS_URL, createTransactionParams(account));
                addAccountDetails(account, Jsoup.parse(response));
                account.setTransactions(parseTransactions(response));
            }
            this.setAccounts(accounts);
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

    private ArrayList<Account> parseAccounts(Document pDocument) {
        ArrayList<Account> accountList = new ArrayList<Account>();
        Element element = pDocument.getElementById("balanceForm:accountsList");
        Elements accounts = element.select("td a[href=#]");
        for (int i = 0; i<accounts.size(); i++) {
            Account account = new Account("",BigDecimal.ZERO,Integer.toString(i));
            accountList.add(account);
        }
        return accountList;
    }

    private Account addAccountDetails(Account pAccount, Document pDocument) {
        Elements vAccountDetails = pDocument
                .select("strong:contains(Saldo och transaktioner) ~ table")
                .first().select("tr td:last-child");
        String vAccountType = vAccountDetails.first().text();
        String vBalance = vAccountDetails.last().text();
        pAccount.setName(vAccountType);
        pAccount.setCurrency("SEK");
        BigDecimal balance = new BigDecimal(vBalance.replaceAll("[^\\d]", ""));
        pAccount.setBalance(balance);
        return pAccount;
    }

    private List<Transaction> parseTransactions(String pResponse) {
        List<Transaction> vTransactions = new ArrayList<Transaction>();
        Document doc = Jsoup.parse(pResponse);
        Elements vTransactionElements =
                doc.getElementById("balanceForm:transactionPostList").select("tbody tr");

        for (Element element : vTransactionElements) {

            Elements vTransactionElement = element.select("td");

           BigDecimal amount = new BigDecimal(vTransactionElement.get(1).text()
                    .replaceAll("[^\\d-]", ""));
            String description = vTransactionElement.get(2).text();
            if (description == null || description.isEmpty()) {
               description = amount.compareTo(BigDecimal.ZERO) > 0 ? "Insättning"
                                : "Uttag";
            }
            String date = vTransactionElement.first().text();
            vTransactions.add(new Transaction(date,description,amount));
        }
        return vTransactions;
    }

    List<NameValuePair>  createTransactionParams(Account pAccount) {
        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("balanceForm", "balanceForm"));
        postData.add(new BasicNameValuePair("balanceForm:_idcl","balanceForm:accountsList:"+pAccount.getId()+":_id15"));
        return postData;
    }
}
