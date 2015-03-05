package com.liato.bankdroid.banking.banks.swedbank;

import android.content.Context;

import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;

import java.io.IOException;

public class Sparbankerna extends AbstractSwedbank {

    private static final String TAG = "Sparbankerna";
    private static final String NAME = "Sparbankerna";
    private static final String NAME_SHORT = "sparbankerna";
    private static final int BANKTYPE_ID = IBankTypes.SPARBANKERNA;

    private static final String APP_ID = "qdorTi1mqZ09Zcyc";

    public Sparbankerna(Context context) {
        super(context);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;

    }

    public Sparbankerna(String username, String password, Context context)
            throws BankException, LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected String getAppId() {
        return APP_ID;
    }
}
