package com.liato.bankdroid;

import java.math.BigDecimal;
import java.util.ArrayList;

import net.sf.andhsli.hotspotlogin.SimpleCrypto;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

import com.liato.bankdroid.banks.Avanza;
import com.liato.bankdroid.banks.AvanzaMini;
import com.liato.bankdroid.banks.Coop;
import com.liato.bankdroid.banks.Eurocard;
import com.liato.bankdroid.banks.FirstCard;
import com.liato.bankdroid.banks.Handelsbanken;
import com.liato.bankdroid.banks.ICA;
import com.liato.bankdroid.banks.ICABanken;
import com.liato.bankdroid.banks.Lansforsakringar;
import com.liato.bankdroid.banks.Nordea;
import com.liato.bankdroid.banks.OKQ8;
import com.liato.bankdroid.banks.PayPal;
import com.liato.bankdroid.banks.Statoil;
import com.liato.bankdroid.banks.Swedbank;
import com.liato.bankdroid.banks.TestBank;
import com.liato.bankdroid.banks.Villabanken;

public class BankFactory {

	public static Bank fromBanktypeId(int id, Context context) throws BankException {
		switch (id) {
        case Bank.TESTBANK:
            return new TestBank(context);
        case Bank.SWEDBANK:
            return new Swedbank(context);
		case Bank.NORDEA:
			return new Nordea(context);
		case Bank.LANSFORSAKRINGAR:
			return new Lansforsakringar(context);
		case Bank.ICABANKEN:
			return new ICABanken(context);
		case Bank.HANDELSBANKEN:
			return new Handelsbanken(context);
		case Bank.COOP:
			return new Coop(context);
		case Bank.ICA:
			return new ICA(context);
		case Bank.STATOIL:
			return new Statoil(context);
		case Bank.AVANZA:
			return new Avanza(context);
		case Bank.VILLABANKEN:
			return new Villabanken(context);
        case Bank.AVANZAMINI:
            return new AvanzaMini(context);
        case Bank.OKQ8:
            return new OKQ8(context);
        case Bank.EUROCARD:
            return new Eurocard(context);
        case Bank.FIRSTCARD:
            return new FirstCard(context);
        case Bank.PAYPAL:
            return new PayPal(context);
		default:
			throw new BankException("BankType id not found.");
		}
	}
	

	public static ArrayList<Bank> listBanks(Context context) {
		ArrayList<Bank> banks = new ArrayList<Bank>();
		banks.add(new Swedbank(context));
		banks.add(new Nordea(context));
		banks.add(new ICABanken(context));
		banks.add(new Lansforsakringar(context));
		banks.add(new Handelsbanken(context));
		banks.add(new Coop(context));
		banks.add(new ICA(context));
		banks.add(new Statoil(context));
		banks.add(new Avanza(context));
		banks.add(new Villabanken(context));
		banks.add(new AvanzaMini(context));
        banks.add(new OKQ8(context));
		banks.add(new Eurocard(context));
        banks.add(new FirstCard(context));
        banks.add(new PayPal(context));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getBoolean("debug_mode", false)) { 
            banks.add(new TestBank(context));
        }
		return banks;
	}

	public static Bank bankFromDb(long id, Context context, boolean loadAccounts) {
		Bank bank = null;
		DBAdapter db = new DBAdapter(context);
		db.open();
		Cursor c = db.getBank(id);

		if (c != null) {
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
							 c.getString(c.getColumnIndex("custname")));
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
		db.close();
		return bank;
	}

	public static ArrayList<Bank> banksFromDb(Context context, boolean loadAccounts) {
		ArrayList<Bank> banks = new ArrayList<Bank>();
		DBAdapter db = new DBAdapter(context);
		db.open();
		Cursor c = db.fetchBanks();
		if (c == null) {
			db.close();
			return banks;
		}
		while (!c.isLast() && !c.isAfterLast()) {
			c.moveToNext();
			//Log.d("AA", "Refreshing "+c.getString(clmBanktype)+" ("+c.getString(clmUsername)+").");
			try {
			    if (c.getInt(c.getColumnIndex("banktype")) == Bank.AVANZA) {
			        continue;
			    }
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
				             c.getString(c.getColumnIndex("custname")));
				if (loadAccounts) {
					bank.setAccounts(accountsFromDb(context, bank.getDbId()));
				}
				banks.add(bank);
			} catch (BankException e) {
				//e.printStackTrace();
			}
		}
		c.close();
		db.close();
		return banks;
	}
	
	public static Account accountFromDb(Context context, String accountId, boolean loadTransactions) {
		DBAdapter db = new DBAdapter(context);
		db.open();
		Cursor c = db.getAccount(accountId);
		if (c == null || c.isClosed() || (c.isBeforeFirst() && c.isAfterLast())) {
			db.close();
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
		c.close();
		if (loadTransactions) {
			ArrayList<Transaction> transactions = new ArrayList<Transaction>();
			//"transdate", "btransaction", "amount"}			
			c = db.fetchTransactions(accountId);
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
		
		db.close();
		return account;
	}
	
	public static ArrayList<Account> accountsFromDb(Context context, long bankId) {
		ArrayList<Account> accounts = new ArrayList<Account>();
		DBAdapter db = new DBAdapter(context);
		db.open();
		Cursor c = db.fetchAccounts(bankId);
		if (c == null) {
			db.close();
			return accounts;
		}
		while (!c.isLast() && !c.isAfterLast()) {
			c.moveToNext();
			Account account = new Account(c.getString(c.getColumnIndex("name")),
                                          new BigDecimal(c.getString(c.getColumnIndex("balance"))),
                                          c.getString(c.getColumnIndex("id")).split("_", 2)[1],
                                          c.getLong(c.getColumnIndex("bankid")),
                                          c.getInt(c.getColumnIndex("acctype")));
	        account.setHidden(c.getInt(c.getColumnIndex("hidden")) == 1 ? true : false);
	        account.setNotify(c.getInt(c.getColumnIndex("notify")) == 1 ? true : false);			
	        account.setCurrency(c.getString(c.getColumnIndex("currency")));
			accounts.add(account);
		}
		c.close();
		db.close();
		return accounts;
	}
	
}
