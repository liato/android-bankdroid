package se.smartrefill.ad.bank.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AdMoneyTransferCard implements Serializable {
	private static final long serialVersionUID = 1L;
	private String alias;
	private int creditCardId;
	private int id;
	private String mobileNumber;
	private List<AdMoneyTransferOrder> moneyTransferOrders;
	private AdMoneyTransferService moneyTransferService;
	private boolean pending;

	public AdMoneyTransferCard() {
		this.moneyTransferOrders = new ArrayList<AdMoneyTransferOrder>();
	}

	public String getAlias() {
		return alias;
	}

	public int getCreditCardId() {
		return creditCardId;
	}

	public int getId() {
		return id;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public List<AdMoneyTransferOrder> getMoneyTransferOrders() {
		return moneyTransferOrders;
	}

	public AdMoneyTransferService getMoneyTransferService() {
		return moneyTransferService;
	}

	public boolean isPending() {
		return pending;
	}
}
