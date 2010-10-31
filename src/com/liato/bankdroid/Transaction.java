package com.liato.bankdroid;

import java.math.BigDecimal;

public class Transaction implements Comparable<Transaction> {
	private String date;
	private String transaction;
	private BigDecimal amount;
	private String currency;
	
	public Transaction(String date, String transaction, BigDecimal amount, String currency) {
		this.date = date;
		this.transaction = transaction;
		this.amount = amount;
		this.currency = currency;
	}

    public Transaction(String date, String transaction, BigDecimal amount) {
        this(date, transaction, amount, "SEK");
    }	

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTransaction() {
		return transaction;
	}

	public void setTransaction(String transaction) {
		this.transaction = transaction;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
	public int compareTo(Transaction another) {
		Integer thisdate = Integer.parseInt(date.replaceAll("-", ""));
		Integer thatdate = Integer.parseInt((another).getDate().replaceAll("-", ""));
		return thisdate - thatdate;
	}
}
