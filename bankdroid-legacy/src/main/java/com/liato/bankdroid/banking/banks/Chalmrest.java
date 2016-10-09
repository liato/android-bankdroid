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

import android.content.Context;
import android.text.Html;
import android.text.InputType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.nullbyte.android.urllib.Urllib;

public class Chalmrest extends Bank {

    private static final String TAG = "Chalmrest";

    private static final String NAME = "Chalmrest";

    private static final String NAME_SHORT = "chalmrest";

    private static final int BANKTYPE_ID = IBankTypes.CHALMREST;

    private Pattern reViewState = Pattern.compile("__VIEWSTATE\"\\s+value=\"([^\"]+)\"");
    private Pattern reEventValidation = Pattern.compile("__EVENTVALIDATION\"\\s+value=\"([^\"]+)\"");

    private Pattern reAccount = Pattern
            .compile("<span id=\"txtPTMCardName\">(.*?)</span>",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    private Pattern reBalance = Pattern.compile(
            "<span id=\"txtPTMCardValue\">(.*?)</span>",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    private String response = null;

    public Chalmrest(Context context) {
        super(context, R.drawable.logo_chalmrest);

        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;
        super.INPUT_TITLETEXT_USERNAME = R.string.card_number;
        super.INPUT_HINT_USERNAME = "XXXXXXXXXXXXXXXX";
        super.INPUT_TYPE_USERNAME = InputType.TYPE_CLASS_NUMBER;
        super.INPUT_HIDDEN_PASSWORD = true;
    }

    public Chalmrest(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected LoginPackage preLogin() throws BankException, IOException {
        urlopen = new Urllib(context);
        response = urlopen.open("http://kortladdning3.chalmerskonferens.se/Default.aspx");

        Matcher matcherView = reViewState.matcher(response);
        if (!matcherView.find()) {
            throw new BankException(
                    res.getText(R.string.unable_to_find).toString() + " ViewState.");
        }
        String strViewState = matcherView.group(1);

        Matcher matcherEvent = reEventValidation.matcher(response);
        if (!matcherEvent.find()) {
            throw new BankException(
                    res.getText(R.string.unable_to_find).toString() + " EventValidation.");
        }
        String strEvent = matcherEvent.group(1);

        List<NameValuePair> postData = new ArrayList<NameValuePair>();
        postData.add(new BasicNameValuePair("__VIEWSTATE", strViewState));
        postData.add(new BasicNameValuePair("__EVENTVALIDATION", strEvent));
        postData.add(new BasicNameValuePair("txtCardNumber", getUsername()));
        postData.add(new BasicNameValuePair("btnNext", "Nästa"));
        postData.add(new BasicNameValuePair("hiddenIsMobile", "desktop"));

        return new LoginPackage(urlopen, postData, response,
                "http://kortladdning3.chalmerskonferens.se/Default.aspx");
    }

    @Override
    public Urllib login() throws LoginException, BankException, IOException {
        LoginPackage lp = preLogin();
        response = urlopen.open(lp.getLoginTarget(), lp.getPostData());
        if (!response.contains("Logga ut")) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }
        return urlopen;
    }

    @Override
    public void update() throws BankException, LoginException, BankChoiceException, IOException {
        super.update();
        if (getUsername().isEmpty()) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }
        urlopen = login();
        response = urlopen.open("http://kortladdning3.chalmerskonferens.se/CardLoad_Order.aspx");
        Matcher matcher;
        Matcher matcher_b;

        matcher = reAccount.matcher(response);
        if (matcher.find()) {
            /*
             * Capture groups:
             * GROUP                EXAMPLE DATA
             * 1: Name              Kalle Karlsson
             */

            matcher_b = reBalance.matcher(response);
            if (matcher_b.find()) {
                /*
                 * Capture groups:
                 * GROUP                EXAMPLE DATA
                 * 1: Balance              118 kr
                 */

                String balanceString = matcher_b.group(1).replaceAll("\\<a[^>]*>", "")
                        .replaceAll("\\<[^>]*>", "").trim();

                accounts.add(new Account(Html.fromHtml(matcher.group(1)).toString().trim(),
                        Helpers.parseBalance(balanceString), matcher.group(1)));
                balance = balance.add(Helpers.parseBalance(balanceString));
            }
        }

        if (accounts.isEmpty()) {
            throw new BankException(res.getText(R.string.no_accounts_found).toString());
        }

        super.updateComplete();
    }
}
