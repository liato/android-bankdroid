package se.smartrefill.ad.bank.domain;

import java.io.Serializable;

public class AdMoneyTransferOrder implements Serializable {
	public static final int STATE_COMPLETE = 2;
	public static final int STATE_FAILED = 3;
	public static final int STATE_PENDING = 1;
	public static final int STATE_UNKNOWN = 0;
	private static final long serialVersionUID = 1L;
	private String amount;
	private String created;
	private int id;
	private AdMoneyTransferCard moneyTransferCard;
	private int state = 0;

	public String getAmount() {
		return amount;
	}

	public String getCreated() {
		return created;
	}

	public int getId() {
		return id;
	}

	public AdMoneyTransferCard getMoneyTransferCard() {
		return moneyTransferCard;
	}

	public int getState() {
		return state;
	}
}
