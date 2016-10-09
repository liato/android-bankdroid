package com.liato.bankdroid.banking.banks.swedbank;

import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.legacy.R;
import com.liato.bankdroid.provider.IBankTypes;

import android.content.Context;

import java.io.IOException;

public class SwedbankYouth extends AbstractSwedbank {

    private static final String TAG = "Swedbank Ung";

    private static final String NAME = "Swedbank Ung";

    private static final String NAME_SHORT = "swedbank-youth";

    private static final int BANKTYPE_ID = IBankTypes.SWEDBANK_YOUTH;

    private static final String APP_ID = "19AaEzp0jQJDt3vO";

    public SwedbankYouth(Context context) {
        super(context, R.drawable.logo_swedbank);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;

    }

    public SwedbankYouth(String username, String password, Context context)
            throws BankException, LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected String getAppId() {
        return APP_ID;
    }
}
