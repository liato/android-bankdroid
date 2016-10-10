package com.liato.bankdroid.banking.banks.swedbank;

import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.legacy.R;
import com.liato.bankdroid.provider.IBankTypes;

import android.content.Context;

import java.io.IOException;

public class Swedbank extends AbstractSwedbank {

    protected static final String NAME_SHORT = "swedbank";

    private static final String NAME = "Swedbank";

    private static final int BANKTYPE_ID = IBankTypes.SWEDBANK;

    private static final String APP_ID = "vgmYRMelBJ0Yzujs";

    public Swedbank(Context context) {
        super(context, R.drawable.logo_swedbank);

        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
        super.BANKTYPE_ID = BANKTYPE_ID;

    }

    public Swedbank(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        this(context);
        this.update(username, password);
    }

    @Override
    protected String getAppId() {
        return APP_ID;
    }
}
