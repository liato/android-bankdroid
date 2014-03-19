package com.liato.bankdroid.banking.banks.icabanken.model.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.liato.bankdroid.banking.banks.icabanken.model.IcaBankenAccount;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {

	private List<IcaBankenAccount> mAccounts;

	@JsonProperty("Accounts")
	public List<IcaBankenAccount> getAccounts() {
		return mAccounts;
	}

	public void setAccounts(List<IcaBankenAccount> pAccounts) {
		this.mAccounts = pAccounts;
	}
}
