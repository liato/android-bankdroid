package com.liato.bankdroid.banking.banks;

import android.content.Context;

import com.liato.bankdroid.legacy.R;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.provider.IBankTypes;

import java.io.IOException;

public class SupremeCard extends ResursBank {

    public SupremeCard(Context context) {
        super(context);
        super.TAG = "SupremeCard";
        super.NAME = "Supreme Card";
        super.NAME_SHORT = "supremecard";
        super.INPUT_TITLETEXT_USERNAME = R.string.account_number;
        super.INPUT_TITLETEXT_PASSWORD = R.string.control_code;
        super.BANKTYPE_ID = IBankTypes.SUPREMECARD;
    }

    public SupremeCard(String username, String password, Context context) throws BankException,
            LoginException, BankChoiceException, IOException {
        super(username, password, context);
    }
}
