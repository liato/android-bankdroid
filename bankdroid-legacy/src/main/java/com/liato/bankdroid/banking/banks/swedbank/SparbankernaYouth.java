package com.liato.bankdroid.banking.banks.swedbank;

import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.legacy.R;
import com.liato.bankdroid.provider.IBankTypes;

import android.content.Context;

import java.io.IOException;

public class SparbankernaYouth extends AbstractSwedbank {

    private static final String TAG = "Sparbankerna Ung";

    private static final String NAME = "Sparbankerna Ung";

    private static final String NAME_SHORT = "sparbankerna-youth";

    private static final int BANKTYPE_ID = IBankTypes.SPARBANKERNA_YOUTH;

    private static final String APP_ID = "QtDHyRnJvEuET2vU";

    public SparbankernaYouth(Context context) {
        super(context, R.drawable.logo_sparbankerna);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;

    }

    public SparbankernaYouth(String username, String password, Context context)
            throws BankException, LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected String getAppId() {
        return APP_ID;
    }
}
