package com.liato.bankdroid.banking.banks.lansforsakringar.model.response;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class AccountsResponse {

	private ArrayList<Account> mAccounts = new ArrayList<Account>();


	@JsonSetter("accounts")
	public void setAccounts(ArrayList<Account> a) { mAccounts = a; }
	@JsonProperty("accounts")
	public ArrayList<Account> getAccounts() { return mAccounts; }

}