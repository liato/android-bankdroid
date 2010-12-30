package com.liato.bankdroid.banking.banks;

import android.content.Context;

import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;


public class Statoil extends SEBKortBase {
	private static final String TAG = "Statoil";
	private static final String NAME = "Statoil";
	private static final String NAME_SHORT = "statoil";
	private static final int BANKTYPE_ID = Bank.STATOIL;

	public Statoil(Context context) {
		super(context, "stse", "0122");
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
	}
	
	public Statoil(String username, String password, Context context) throws BankException, LoginException {
		super(username, password, context, "stse", "0122");
	}

}
