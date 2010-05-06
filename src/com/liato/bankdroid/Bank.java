package com.liato.bankdroid;

import java.math.BigDecimal;
import java.util.ArrayList;

public interface Bank {
	ArrayList<Account> getAccounts();
	String getUsername();
	String getPassword();
	Banks getType();
	BigDecimal getBalance();
	void update() throws BankException;
	void update(String username, String password) throws BankException;
}
