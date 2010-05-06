package com.liato.bankdroid;

import java.math.BigDecimal;

public class Account {
	private String name;
	private BigDecimal balance;

	public Account(String name, BigDecimal balance) {
		this.name = name;
		this.balance = balance;
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
}
