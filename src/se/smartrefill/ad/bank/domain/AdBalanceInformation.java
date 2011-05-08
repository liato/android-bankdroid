package se.smartrefill.ad.bank.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AdBalanceInformation implements Serializable {
	public static int CODE_ERROR = 1;
	public static int CODE_INTENTIONALLY_BLANK = 2;
	public static int CODE_OK = 0;
	private static final long serialVersionUID = 1L;
	private String amountSpent;
	private List<AdBalanceInformationTransaction> balanceInformationTransactions;
	private String disposableAmount;
	private int statusCode;

	public AdBalanceInformation() {
		this.balanceInformationTransactions = new ArrayList<AdBalanceInformationTransaction>();
	}

	public String getAmountSpent() {
		return amountSpent;
	}

	public List<AdBalanceInformationTransaction> getBalanceInformationTransactions() {
		return balanceInformationTransactions;
	}

	public String getDisposableAmount() {
		return disposableAmount;
	}

	public int getStatusCode() {
		return statusCode;
	}
}
