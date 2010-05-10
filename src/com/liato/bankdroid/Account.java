package com.liato.bankdroid;

import java.math.BigDecimal;

public class Account {
	private String name;
	private BigDecimal balance;
	private String id;

	public Account(String name, BigDecimal balance, String id) {
		this.name = name;
		this.balance = balance;
		this.id = id;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public String getName() {
		return name;
	}
	
	public String getId() {
		return id;
	}
}
