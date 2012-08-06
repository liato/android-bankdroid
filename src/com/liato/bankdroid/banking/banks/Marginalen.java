package com.liato.bankdroid.banking.banks;

import android.content.Context;

import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;

public class Marginalen extends Bank {
	private static final String TAG = "Marginalen";
    private static final String NAME = "Marginalen Bank";
    private static final String NAME_SHORT = "marginalen";
    private static final String URL = "https://secure1.marginalen.se/marginalen/engine";
    
    public Marginalen(Context context) {
        super(context);
        super.TAG = TAG;
        super.NAME = NAME;
        super.NAME_SHORT = NAME_SHORT;
//        super.BANKTYPE_ID = BANKTYPE_ID;
//        super.URL = URL;
//        super.INPUT_TYPE_USERNAME = INPUT_TYPE_USERNAME;
//        super.INPUT_TYPE_PASSWORD = INPUT_TYPE_PASSWORD;
//        super.INPUT_HINT_USERNAME = INPUT_HINT_USERNAME;
    }
    
    @Override
    public void update() throws BankException, LoginException, BankChoiceException {
        super.update();
    }
}
