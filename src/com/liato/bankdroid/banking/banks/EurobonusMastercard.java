package com.liato.bankdroid.banking.banks;

import android.content.Context;

import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;


public class EurobonusMastercard extends SEBKortBase {
	private static final String TAG = "EurobonusMastercard";
	private static final String NAME = "Eurobonus Mastercard";
	private static final String NAME_SHORT = "ebmaster";
	private static final int BANKTYPE_ID = Bank.EUROBONUSMASTERCARD;

	public EurobonusMastercard(Context context) {
		super(context, "sase", "0102");
		super.TAG = TAG;
		super.NAME = NAME;
		super.NAME_SHORT = NAME_SHORT;
		super.BANKTYPE_ID = BANKTYPE_ID;
	}
	
	public EurobonusMastercard(String username, String password, Context context) throws BankException, LoginException {
		super(username, password, context, "sase", "0102");
	}

}
