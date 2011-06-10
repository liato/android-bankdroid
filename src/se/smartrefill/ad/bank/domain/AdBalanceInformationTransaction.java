package se.smartrefill.ad.bank.domain;

import java.io.Serializable;

public class AdBalanceInformationTransaction implements Serializable {
	private static final long serialVersionUID = 1L;
	private String amount;
	private String id;
	private String merchant;
	private int order;
	private int page;
	private String time;

	public AdBalanceInformationTransaction() {
	}

	public String getAmount() {
		return this.amount;
	}

	public String getId() {
		return this.id;
	}

	public String getMerchant() {
		return this.merchant;
	}

	public int getOrder() {
		return this.order;
	}

	public int getPage() {
		return this.page;
	}

	public String getTime() {
		return this.time;
	}
}
