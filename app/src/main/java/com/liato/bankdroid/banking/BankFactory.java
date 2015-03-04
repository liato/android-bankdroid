/*
 * Copyright (C) 2014 Nullbyte <http://nullbyte.eu>
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

import android.content.Context;
import android.database.Cursor;

import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.db.Crypto;
import com.liato.bankdroid.db.DBAdapter;

import net.sf.andhsli.hotspotlogin.SimpleCrypto;

import java.math.BigDecimal;
import java.util.ArrayList;

public class BankFactory {

	public static Bank fromBanktypeId(int id, Context context) throws BankException {
		return LegacyBankFactory.fromBanktypeId(id, context);
	}


	public static ArrayList<Bank> listBanks(Context context) {
        return LegacyBankFactory.listBanks(context);
	}

	public static Bank bankFromDb(long id, Context context, boolean loadAccounts) {
		Bank bank = null;
		DBAdapter db = new DBAdapter(context);
		Cursor c = db.getBank(id);

		if (c != null && c.getCount() > 0) {
			try {
				bank = fromBanktypeId(c.getInt(c.getColumnIndex("banktype")), context);
				String password = "";
				try {
					password = SimpleCrypto.decrypt(Crypto.getKey(), c.getString(c.getColumnIndex("password")));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				bank.setData(c.getString(c.getColumnIndex("username")),
							 password,
							 new BigDecimal(c.getString(c.getColumnIndex("balance"))),
							 (c.getInt(c.getColumnIndex("disabled")) == 0 ? false : true),
							 c.getLong(c.getColumnIndex("_id")),
							 c.getString(c.getColumnIndex("currency")),
							 c.getString(c.getColumnIndex("custname")),
							 c.getString(c.getColumnIndex("extras")),
                             c.getInt(c.getColumnIndex("hideAccounts")));
				if (loadAccounts) {
					bank.setAccounts(accountsFromDb(context, bank.getDbId()));
				}
			} catch (BankException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally {
				c.close();
			}
		}
		return bank;
	}

	public static ArrayList<Bank> banksFromDb(Context context, boolean loadAccounts) {
		ArrayList<Bank> banks = new ArrayList<Bank>();
		DBAdapter db = new DBAdapter(context);
		Cursor c = db.fetchBanks();
		if (c == null || c.getCount() == 0) {
			return banks;
		}
		while (!c.isLast() && !c.isAfterLast()) {
			c.moveToNext();
			try {
				Bank bank = fromBanktypeId(c.getInt(c.getColumnIndex("banktype")), context);

	            String password = "";
                try {
                    password = SimpleCrypto.decrypt(Crypto.getKey(), c.getString(c.getColumnIndex("password")));
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                bank.setData(c.getString(c.getColumnIndex("username")),
				             password,
				             new BigDecimal(c.getString(c.getColumnIndex("balance"))),
				             (c.getInt(c.getColumnIndex("disabled")) == 0 ? false : true),
				             c.getLong(c.getColumnIndex("_id")),
				             c.getString(c.getColumnIndex("currency")),
				             c.getString(c.getColumnIndex("custname")),
                             c.getString(c.getColumnIndex("extras")),
                             c.getInt(c.getColumnIndex("hideAccounts")));
				if (loadAccounts) {
					bank.setAccounts(accountsFromDb(context, bank.getDbId()));
				}
				banks.add(bank);
			} catch (BankException e) {
				//e.printStackTrace();
			}
		}
		c.close();
		return banks;
	}

	public static Account accountFromDb(Context context, String accountId, boolean loadTransactions) {
		DBAdapter db = new DBAdapter(context);
		Cursor c = db.getAccount(accountId);

		if (c == null || c.isClosed() || (c.isBeforeFirst() && c.isAfterLast())) {
			return null;
		}

		Account account = new Account(c.getString(c.getColumnIndex("name")),
                                      new BigDecimal(c.getString(c.getColumnIndex("balance"))),
                                      c.getString(c.getColumnIndex("id")).split("_", 2)[1],
                                      c.getLong(c.getColumnIndex("bankid")),
                                      c.getInt(c.getColumnIndex("acctype")));
        account.setHidden(c.getInt(c.getColumnIndex("hidden")) == 1 ? true : false);
        account.setNotify(c.getInt(c.getColumnIndex("notify")) == 1 ? true : false);
        account.setCurrency(c.getString(c.getColumnIndex("currency")));
        account.setAliasfor(c.getString(c.getColumnIndex("aliasfor")));
		c.close();
		if (loadTransactions) {
			ArrayList<Transaction> transactions = new ArrayList<Transaction>();
			String fromAccount = accountId;
			if (account.getAliasfor() != null && account.getAliasfor().length() > 0) {
			    fromAccount = Long.toString(account.getBankDbId()) + "_" + account.getAliasfor();
			}
			c = db.fetchTransactions(fromAccount);
			if (!(c == null || c.isClosed() || (c.isBeforeFirst() && c.isAfterLast()))) {
				while (!c.isLast() && !c.isAfterLast()) {
					c.moveToNext();
					transactions.add(new Transaction(c.getString(c.getColumnIndex("transdate")),
                                     c.getString(c.getColumnIndex("btransaction")),
                                     new BigDecimal(c.getString(c.getColumnIndex("amount"))),
                                     c.getString(c.getColumnIndex("currency"))));
				}
				c.close();
			}
			account.setTransactions(transactions);
		}
		return account;
	}

	public static ArrayList<Account> accountsFromDb(Context context, long bankId) {
		ArrayList<Account> accounts = new ArrayList<Account>();
		DBAdapter db = new DBAdapter(context);
		Cursor c = db.fetchAccounts(bankId);
		if (c == null || c.getCount() == 0) {
			return accounts;
		}
		while (!c.isLast() && !c.isAfterLast()) {
			c.moveToNext();
			try {
    			Account account = new Account(c.getString(c.getColumnIndex("name")),
                                              new BigDecimal(c.getString(c.getColumnIndex("balance"))),
                                              c.getString(c.getColumnIndex("id")).split("_", 2)[1],
                                              c.getLong(c.getColumnIndex("bankid")),
                                              c.getInt(c.getColumnIndex("acctype")));
    	        account.setHidden(c.getInt(c.getColumnIndex("hidden")) == 1 ? true : false);
    	        account.setNotify(c.getInt(c.getColumnIndex("notify")) == 1 ? true : false);
                account.setCurrency(c.getString(c.getColumnIndex("currency")));
                account.setAliasfor(c.getString(c.getColumnIndex("aliasfor")));
    			accounts.add(account);
			}
			catch (ArrayIndexOutOfBoundsException e) {
			    // Attempted to load an account without and ID, probably an old Avanza account.
			}
		}
		c.close();
		return accounts;
	}

}
