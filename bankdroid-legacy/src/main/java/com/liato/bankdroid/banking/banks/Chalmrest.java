package com.liato.bankdroid.banking.banks;

import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.legacy.R;
import com.liato.bankdroid.provider.IBankTypes;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.text.InputType;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Chalmrest extends Bank {

    private static final String TAG = "Chalmrest";

    private static final String NAME = "Chalmrest";

    private static final String NAME_SHORT = "chalmrest";

    private static final int BANKTYPE_ID = IBankTypes.CHALMREST;

    public Chalmrest(Context context) {
        super(context);

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
    public void update() throws BankException, LoginException, BankChoiceException, IOException {
        super.update();
        if (username == null || username.length() == 0) {
            throw new LoginException(res.getText(R.string.invalid_username_password).toString());
        }

        String cardNr = username;

        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(
                "http://kortladdning.chalmerskonferens.se/bgw.aspx?type=getCardAndArticles&card="
                        + cardNr);
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            throw new BankException("Couldn't connect!");
        }

        String s1 = EntityUtils.toString(entity);
        Pattern pattern = Pattern.compile(
                "<ExtendedInfo Name=\"Kortvarde\" Type=\"System.Double\" >(.*?)</ExtendedInfo>");
        Matcher matcher = pattern.matcher(s1);

        if (!matcher.find()) {
            throw new BankException("Couldn't parse value!");
        }

        String value = matcher.group(1);

        StringBuilder sb = new StringBuilder();
        int last = 0;
        Matcher match = Pattern.compile("_x([0-9A-Fa-f]{4})_").matcher(value);
        while (match.find()) {
            sb.append(value.substring(last, match.start()));
            int i = Integer.parseInt(match.group(1), 16);
            sb.append((char) i);
            last = match.end();
        }
        sb.append(value.substring(last));
        value = sb.toString().replace(',', '.');

        matcher = Pattern.compile("<CardInfo id=\"" + cardNr + "\" Name=\"(.*?)\"").matcher(s1);
        if (!matcher.find()) {
            throw new BankException("Coldn't parse name!");
        }
        String name = matcher.group(1);

        accounts.add(new Account(name, BigDecimal.valueOf(Double.parseDouble(value)), "1"));

        super.updateComplete();
    }
}
