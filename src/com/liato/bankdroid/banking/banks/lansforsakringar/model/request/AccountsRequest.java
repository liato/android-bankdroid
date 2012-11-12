package com.liato.bankdroid.banking.banks.lansforsakringar.model.request;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class AccountsRequest {
	public enum Type {
		CHECKING("CHECKING"),
		SAVING("SAVING");
		
		private String name;
		private Type(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return this.name;
		}
	}
    private String mAccountType;


    public AccountsRequest (Type accountType) {
        mAccountType = accountType.toString();
    }

    @JsonSetter("accountType")
    public void setAccountType(String a) { mAccountType = a; }
    @JsonProperty("accountType")
    public String getAccountType() { return mAccountType; }

}