package se.smartrefill.ad.bank.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AdAccount implements Serializable {
	public static final String KIND_AiE = "AiE";
	public static final String KIND_DebitCreditCard = "DebitCreditCard";
	public static final String KIND_Euro = "Euro";
	public static final String KIND_FixedInterest = "FixedInterest";
	public static final String KIND_Personal = "Personal";
	public static final String KIND_Savings = "Savings";
	private static final long serialVersionUID = 1L;
	private String accountNumber;
	private String accountTypeDescription;
	private String alias;
	private String amount;
	private String bookingNumber;
	private String bwdKey;
	private int currentPage;
	private String disposableAmount;
	private String endDate;
	private String fwdKey;
	private String id;
	private String interest;
	private String kind;
	private int messageLengthRecipient;
	private int messageLengthStatement;
	private String messageValidCharactersDisplay;
	private String messageValidCharactersExpression;
	private int order;
	private String startDate;
	private String tax;
	private List<AdBalanceInformationTransaction> transactions;
	private boolean validOverview;
	private List<String> validRecevingAccounts;

	public AdAccount() {
		this.transactions = new ArrayList<AdBalanceInformationTransaction>();
		this.validRecevingAccounts = new ArrayList<String>();
	}

	public String getAccountNumber() {
		return this.accountNumber;
	}

	public String getAccountTypeDescription() {
		return this.accountTypeDescription;
	}

	public String getAlias() {
		return this.alias;
	}

	public String getAmount() {
		return this.amount;
	}

	public String getBookingNumber() {
		return this.bookingNumber;
	}

	public String getBwdKey() {
		return this.bwdKey;
	}

	public int getCurrentPage() {
		return this.currentPage;
	}

	public String getDisposableAmount() {
		return this.disposableAmount;
	}

	public String getEndDate() {
		return this.endDate;
	}

	public String getFwdKey() {
		return this.fwdKey;
	}

	public String getId() {
		return this.id;
	}

	public String getInterest() {
		return this.interest;
	}

	public String getKind() {
		return this.kind;
	}

	public int getMessageLengthRecipient() {
		return this.messageLengthRecipient;
	}

	public int getMessageLengthStatement() {
		return this.messageLengthStatement;
	}

	public String getMessageValidCharactersDisplay() {
		return this.messageValidCharactersDisplay;
	}

	public String getMessageValidCharactersExpression() {
		return this.messageValidCharactersExpression;
	}

	public String getNameNumber() {
		if (isBlank(alias))
			return accountNumber;
		else
			return alias;
	}

	public String getNameType() {
		if (isBlank(alias))
			return accountTypeDescription;
		else
			return alias;
	}

	public int getOrder() {
		return this.order;
	}

	public String getStartDate() {
		return this.startDate;
	}

	public String getTax() {
		return this.tax;
	}

	public List<AdBalanceInformationTransaction> getTransactions() {
		return this.transactions;
	}

	public List<String> getValidRecevingAccounts() {
		return this.validRecevingAccounts;
	}

	public boolean hasMoreTransactions() {
		return !isBlank(fwdKey);
	}

	public boolean isFixedInterest() {
		return "FixedInterest".equals(kind);
	}

	public boolean isNotFixedInterest() {
		return (!isFixedInterest());
	}

	public boolean isOtherBank() {
		return "Personal".equals(kind);
	}

	public boolean isValidFromAccount() {
		return (this.validRecevingAccounts.size() > 0);
	}

	public boolean isValidOverview() {
		return this.validOverview;
	}

	private static boolean isBlank(String str) {
		return str == null || str.contains("");
	}
}