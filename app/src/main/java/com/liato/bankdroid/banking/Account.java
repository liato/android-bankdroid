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

package com.liato.bankdroid.banking;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.liato.bankdroid.provider.IAccountTypes;

public class Account implements IAccountTypes {
	private String name;
	private BigDecimal balance;
	private String id;
	private Bank bank = null;
	private long bankId = -1;
	private int type = Account.REGULAR;
	private boolean hidden = false;
	private boolean notify = true;
	private String currency;
	private List<Transaction> transactions;
	private String aliasfor;

    /**
     * Temporary account id. Used for banks that have new account ids for each session.
     * This value is not persited.
     */
    private String sessionId;


	public List<Transaction> getTransactions() {
		return transactions;
	}

	public void setTransactions(final List<Transaction> transactions) {
		this.transactions = transactions;
	}

	public Account(final String name, final BigDecimal balance,
			final String id, final long bankId, final int type,
			final String currency) {
		this.name = name;
		this.balance = balance;
		this.id = id;
		this.bankId = bankId;
		this.type = type;
		this.currency = currency;
	}

	public Account(final String name, final BigDecimal balance,
			final String id, final long bankId) {
		this(name, balance, id, bankId, REGULAR, "SEK");
	}

	public Account(final String name, final BigDecimal balance,
			final String id, final long bankId, final int type) {
		this(name, balance, id, bankId, type, "SEK");
	}

	public Account(final String name, final BigDecimal balance, final String id) {
		this(name, balance, id, -1L);
	}

	public Account(final String name, final BigDecimal balance,
			final String id, final int type) {
		this(name, balance, id, -1L, type);
	}

	public Account(final String name, final BigDecimal balance,
			final String id, final int type, final String currency) {
		this(name, balance, id, -1L, type, currency);
	}

	public boolean isNotify() {
		return notify;
	}

	public void setNotify(final boolean notify) {
		this.notify = notify;
	}

	public void setBalance(final BigDecimal balance) {
		this.balance = balance;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
	    this.id = id;
	}

	public Bank getBank() {
		return bank;
	}

	public void setBank(final Bank bank) {
		this.bank = bank;
	}

	public long getBankDbId() {
		return bankId;
	}

	public void setType(final int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public boolean isHidden() {
		return this.hidden;
	}

	public void setHidden(final boolean hidden) {
		this.hidden = hidden;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(final String currency) {
		this.currency = currency;
	}
	
    public String getAliasfor() {
        return aliasfor;
    }

    public void setAliasfor(final String aliasfor) {
        this.aliasfor = aliasfor;
    }	

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
