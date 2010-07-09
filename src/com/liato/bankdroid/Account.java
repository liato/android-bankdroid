package com.liato.bankdroid;

import java.math.BigDecimal;
import java.util.ArrayList;

public class Account {
	private String name;
	private BigDecimal balance;
	private String id;
	private Bank bank = null;
	private long bankId = -1;
	private ArrayList<Transaction> transactions;

	public ArrayList<Transaction> getTransactions() {
		return transactions;
	}

	public void setTransactions(ArrayList<Transaction> transactions) {
		this.transactions = transactions;
	}

	public Account(String name, BigDecimal balance, String id, long bankId) {
		this.name = name;
		this.balance = balance;
		this.id = id;
		this.bankId = bankId;
	}

	public Account(String name, BigDecimal balance, String id) {
		this(name, balance, id, -1);
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
	
	public Bank getBank() {
		return bank;
	}
	
	public void setBank(Bank bank) {
		this.bank = bank;
	}
	
	public long getBankDbId() {
		return bankId;
	}
}
