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

import net.sf.andhsli.hotspotlogin.SimpleCrypto;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

import com.liato.bankdroid.banking.banks.AmericanExpress;
import com.liato.bankdroid.banking.banks.Avanza;
import com.liato.bankdroid.banking.banks.AvanzaMini;
import com.liato.bankdroid.banking.banks.CSN;
import com.liato.bankdroid.banking.banks.Chevrolet;
import com.liato.bankdroid.banking.banks.Coop;
import com.liato.bankdroid.banking.banks.DinersClub;
import com.liato.bankdroid.banking.banks.EurobonusMastercard;
import com.liato.bankdroid.banking.banks.Eurocard;
import com.liato.bankdroid.banking.banks.FirstCard;
import com.liato.bankdroid.banking.banks.Handelsbanken;
import com.liato.bankdroid.banking.banks.Hemkop;
import com.liato.bankdroid.banking.banks.ICA;
import com.liato.bankdroid.banking.banks.ICABanken;
import com.liato.bankdroid.banking.banks.IkanoBank;
import com.liato.bankdroid.banking.banks.Jojo;
import com.liato.bankdroid.banking.banks.Lansforsakringar;
import com.liato.bankdroid.banking.banks.McDonalds;
import com.liato.bankdroid.banking.banks.Nordea;
import com.liato.bankdroid.banking.banks.Nordnet;
import com.liato.bankdroid.banking.banks.OKQ8;
import com.liato.bankdroid.banking.banks.Osuuspankki;
import com.liato.bankdroid.banking.banks.PayPal;
import com.liato.bankdroid.banking.banks.Payson;
import com.liato.bankdroid.banking.banks.ResursBank;
import com.liato.bankdroid.banking.banks.Rikslunchen;
import com.liato.bankdroid.banking.banks.SEB;
import com.liato.bankdroid.banking.banks.SJPrio;
import com.liato.bankdroid.banking.banks.Saab;
import com.liato.bankdroid.banking.banks.SevenDay;
import com.liato.bankdroid.banking.banks.Statoil;
import com.liato.bankdroid.banking.banks.Steam;
import com.liato.bankdroid.banking.banks.Swedbank;
import com.liato.bankdroid.banking.banks.TestBank;
import com.liato.bankdroid.banking.banks.Villabanken;
import com.liato.bankdroid.banking.banks.Volvofinans;
import com.liato.bankdroid.banking.banks.Wallet;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.db.Crypto;
import com.liato.bankdroid.db.DBAdapter;
import com.liato.bankdroid.provider.IBankTypes;

public class BankFactory {

	public static Bank fromBanktypeId(int id, Context context) throws BankException {
		switch (id) {
        case IBankTypes.TESTBANK:
            return new TestBank(context);
        case IBankTypes.SWEDBANK:
            return new Swedbank(context);
		case IBankTypes.NORDEA:
			return new Nordea(context);
		case IBankTypes.LANSFORSAKRINGAR:
			return new Lansforsakringar(context);
		case IBankTypes.ICABANKEN:
			return new ICABanken(context);
		case IBankTypes.HANDELSBANKEN:
			return new Handelsbanken(context);
		case IBankTypes.COOP:
			return new Coop(context);
		case IBankTypes.ICA:
			return new ICA(context);
		case IBankTypes.STATOIL:
			return new Statoil(context);
		case IBankTypes.AVANZA:
			return new Avanza(context);
		case IBankTypes.VILLABANKEN:
			return new Villabanken(context);
        case IBankTypes.AVANZAMINI:
            return new AvanzaMini(context);
        case IBankTypes.OKQ8:
            return new OKQ8(context);
        case IBankTypes.EUROCARD:
            return new Eurocard(context);
        case IBankTypes.FIRSTCARD:
            return new FirstCard(context);
        case IBankTypes.PAYPAL:
            return new PayPal(context);
        case IBankTypes.PAYSON:
            return new Payson(context);
        case IBankTypes.JOJO:
            return new Jojo(context);
        case IBankTypes.STEAM:
            return new Steam(context);
        case IBankTypes.DINERSCLUB:
            return new DinersClub(context);
        case IBankTypes.IKANOBANK:
            return new IkanoBank(context);
        case IBankTypes.EUROBONUSMASTERCARD:
        	return new EurobonusMastercard(context);
        case IBankTypes.RIKSLUNCHEN:
            return new Rikslunchen(context);            
        case IBankTypes.HEMKOP:
            return new Hemkop(context);            
        case IBankTypes.SEB:
            return new SEB(context);            
        case IBankTypes.NORDNET:
            return new Nordnet(context);            
        case IBankTypes.SEVENDAY:
            return new SevenDay(context);
        case IBankTypes.OSUUSPANKKI:
            return new Osuuspankki(context);
        case IBankTypes.VOLVOFINANS:
            return new Volvofinans(context);
        case IBankTypes.CSN:
            return new CSN(context);
        case IBankTypes.RESURSBANK:
            return new ResursBank(context);
        case IBankTypes.AMERICANEXPRESS:
            return new AmericanExpress(context);
        case IBankTypes.MCDONALDS:
            return new McDonalds(context);
        case IBankTypes.SAAB:
            return new Saab(context);
        case IBankTypes.WALLET:
            return new Wallet(context);
        case IBankTypes.CHEVROLET:
            return new Chevrolet(context);
        case IBankTypes.SJPRIO:
            return new SJPrio(context);
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
        banks.add(new Payson(context));
        banks.add(new Jojo(context));
        banks.add(new Steam(context));
        banks.add(new DinersClub(context));
        banks.add(new IkanoBank(context));
        banks.add(new EurobonusMastercard(context));
        banks.add(new Rikslunchen(context));
        banks.add(new Hemkop(context));
        banks.add(new SEB(context));
        banks.add(new Nordnet(context));
        banks.add(new SevenDay(context));
        banks.add(new Osuuspankki(context));
        banks.add(new Volvofinans(context));
        banks.add(new CSN(context));
        banks.add(new ResursBank(context));
        //American Express doesn't work yet
        //banks.add(new AmericanExpress(context));
        banks.add(new McDonalds(context));
        banks.add(new Saab(context));
        banks.add(new Wallet(context));
        banks.add(new Chevrolet(context));
        banks.add(new SJPrio(context));
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
			try {
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
			catch (ArrayIndexOutOfBoundsException e) {
			    // Attempted to load an account without and ID, probably an old Avanza account.
			}
		}
		c.close();
		db.close();
		return accounts;
	}
	
}
