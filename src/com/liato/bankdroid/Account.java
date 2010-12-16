/*
 * Copyright (C) 2010 Nullbyte <http://nullbyte.eu>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liato.bankdroid;

import java.math.BigDecimal;
import java.util.ArrayList;

public class Account {
	public final static int REGULAR = 1;
	public final static int FUNDS = 2;
	public final static int LOANS = 3;
	public final static int CCARD = 4;
	public final static int OTHER = 5;
	
	private String name;
	private BigDecimal balance;
	private String id;
	private Bank bank = null;
	private long bankId = -1;
	private int type = Account.REGULAR;
	private boolean hidden = false;
	private boolean notify = true;
	private String currency;
	private ArrayList<Transaction> transactions;

	public ArrayList<Transaction> getTransactions() {
		return transactions;
	}

	public void setTransactions(ArrayList<Transaction> transactions) {
		this.transactions = transactions;
	}

	public Account(String name, BigDecimal balance, String id, long bankId,
	               int type, String currency) {
		this.name = name;
		this.balance = balance;
		this.id = id;
		this.bankId = bankId;
		this.type = type;
		this.currency = currency;
	}

    public Account(String name, BigDecimal balance, String id, long bankId) {
        this(name, balance, id, bankId, REGULAR, "SEK");
    }
    
	public Account(String name, BigDecimal balance, String id, long bankId, int type) {
        this(name, balance, id, bankId, type, "SEK");
    }
    
    public Account(String name, BigDecimal balance, String id) {
        this(name, balance, id, -1L);
    }
    
    public Account(String name, BigDecimal balance, String id, int type) {
        this(name, balance, id, -1L, type);
    }	

    public Account(String name, BigDecimal balance, String id, int type, String currency) {
        this(name, balance, id, -1L, type, currency);
    }

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
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

	public void setType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

    public boolean isHidden() {
        return this.hidden;
    }
    
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
}
