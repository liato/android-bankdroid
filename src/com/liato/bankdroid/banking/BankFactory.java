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

import com.liato.bankdroid.banking.banks.AkeliusInvest;
import com.liato.bankdroid.banking.banks.AkeliusSpar;
import com.liato.bankdroid.banking.banks.AmericanExpress;
import com.liato.bankdroid.banking.banks.AppeakPoker;
import com.liato.bankdroid.banking.banks.Audi;
import com.liato.bankdroid.banking.banks.AvanzaMini;
import com.liato.bankdroid.banking.banks.BetterGlobe;
import com.liato.bankdroid.banking.banks.Bioklubben;
import com.liato.bankdroid.banking.banks.BrummerKF;
import com.liato.bankdroid.banking.banks.CSN;
import com.liato.bankdroid.banking.banks.Chalmrest;
import com.liato.bankdroid.banking.banks.Chevrolet;
import com.liato.bankdroid.banking.banks.Coop;
import com.liato.bankdroid.banking.banks.DanskeBank;
import com.liato.bankdroid.banking.banks.DinersClub;
import com.liato.bankdroid.banking.banks.Djurgarden;
import com.liato.bankdroid.banking.banks.EasyCard;
import com.liato.bankdroid.banking.banks.EurobonusMastercard;
import com.liato.bankdroid.banking.banks.EurobonusMastercardDk;
import com.liato.bankdroid.banking.banks.EurobonusMastercardNo;
import com.liato.bankdroid.banking.banks.Eurocard;
import com.liato.bankdroid.banking.banks.Everydaycard;
import com.liato.bankdroid.banking.banks.FirstCard;
import com.liato.bankdroid.banking.banks.ForexBank;
import com.liato.bankdroid.banking.banks.Handelsbanken;
import com.liato.bankdroid.banking.banks.Hemkop;
import com.liato.bankdroid.banking.banks.ICA;
import com.liato.bankdroid.banking.banks.IKEA;
import com.liato.bankdroid.banking.banks.IkanoBank;
import com.liato.bankdroid.banking.banks.Jojo;
import com.liato.bankdroid.banking.banks.Marginalen;
import com.liato.bankdroid.banking.banks.McDonalds;
import com.liato.bankdroid.banking.banks.Meniga;
import com.liato.bankdroid.banking.banks.NordeaDK;
import com.liato.bankdroid.banking.banks.Nordnet;
import com.liato.bankdroid.banking.banks.Nordnetdirekt;
import com.liato.bankdroid.banking.banks.OKQ8;
import com.liato.bankdroid.banking.banks.Opel;
import com.liato.bankdroid.banking.banks.Osuuspankki;
import com.liato.bankdroid.banking.banks.PayPal;
import com.liato.bankdroid.banking.banks.Payson;
import com.liato.bankdroid.banking.banks.PlusGirot;
import com.liato.bankdroid.banking.banks.Preem;
import com.liato.bankdroid.banking.banks.Quintessentially;
import com.liato.bankdroid.banking.banks.ResursBank;
import com.liato.bankdroid.banking.banks.Rikslunchen;
import com.liato.bankdroid.banking.banks.SEB;
import com.liato.bankdroid.banking.banks.SJPrio;
import com.liato.bankdroid.banking.banks.Saab;
import com.liato.bankdroid.banking.banks.Seat;
import com.liato.bankdroid.banking.banks.SevenDay;
import com.liato.bankdroid.banking.banks.Shell;
import com.liato.bankdroid.banking.banks.Skandiabanken;
import com.liato.bankdroid.banking.banks.Skoda;
import com.liato.bankdroid.banking.banks.SparbankenOresund;
import com.liato.bankdroid.banking.banks.SparbankenSyd;
import com.liato.bankdroid.banking.banks.Statoil;
import com.liato.bankdroid.banking.banks.SvenskaSpel;
import com.liato.bankdroid.banking.banks.Swedbank;
import com.liato.bankdroid.banking.banks.TestBank;
import com.liato.bankdroid.banking.banks.TicketRikskortet;
import com.liato.bankdroid.banking.banks.TrustBuddy;
import com.liato.bankdroid.banking.banks.Vasttrafik;
import com.liato.bankdroid.banking.banks.Villabanken;
import com.liato.bankdroid.banking.banks.Volkswagen;
import com.liato.bankdroid.banking.banks.Volvofinans;
import com.liato.bankdroid.banking.banks.Wallet;
import com.liato.bankdroid.banking.banks.Zidisha;
import com.liato.bankdroid.banking.banks.Nordea.Nordea;
import com.liato.bankdroid.banking.banks.avanza.Avanza;
import com.liato.bankdroid.banking.banks.bitcoin.Bitcoin;
import com.liato.bankdroid.banking.banks.icabanken.ICABanken;
import com.liato.bankdroid.banking.banks.lansforsakringar.Lansforsakringar;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.db.Crypto;
import com.liato.bankdroid.db.DBAdapter;
import com.liato.bankdroid.provider.IBankTypes;

public class BankFactory {

	public static Bank fromBanktypeId(int id, Context context) throws BankException {
		switch (id) {
        case IBankTypes.TESTBANK:
            return new TestBank(context);
        case IBankTypes.AKELIUSINVEST:
            return new AkeliusInvest(context);
        case IBankTypes.AKELIUSSPAR:
            return new AkeliusSpar(context);
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
        //case IBankTypes.STEAM:
        //    return new Steam(context);
        case IBankTypes.DINERSCLUB:
            return new DinersClub(context);
        case IBankTypes.IKANOBANK:
            return new IkanoBank(context);
        case IBankTypes.SASEUROBONUSMASTERCARD:
        	return new EurobonusMastercard(context);
        case IBankTypes.SASEUROBONUSMASTERCARD_NO:
        	return new EurobonusMastercardNo(context);
        case IBankTypes.SASEUROBONUSMASTERCARD_DK:
            return new EurobonusMastercardDk(context);
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
        case IBankTypes.OPEL:
            return new Opel(context);
        case IBankTypes.DJURGARDEN:
            return new Djurgarden(context);
        case IBankTypes.QUINTESSENTIALLY:
            return new Quintessentially(context);
        case IBankTypes.PLUSGIROT:
            return new PlusGirot(context);
        case IBankTypes.SHELL:
            return new Shell(context);
        case IBankTypes.VOLKSWAGEN:
            return new Volkswagen(context);
        case IBankTypes.AUDI:
            return new Audi(context);
        case IBankTypes.PREEM:
            return new Preem(context);
        case IBankTypes.SEAT:
            return new Seat(context);
        case IBankTypes.SKODA:
            return new Skoda(context);
        case IBankTypes.IKEA:
            return new IKEA(context);
        case IBankTypes.SPARBANKEN_SYD:
            return new SparbankenSyd(context);
        case IBankTypes.SPARBANKEN_ORESUND:
            return new SparbankenOresund(context);
        case IBankTypes.NORDNETDIREKT:
            return new Nordnetdirekt(context);
        case IBankTypes.SKANDIABANKEN:
            return new Skandiabanken(context);
        case IBankTypes.DANSKEBANK:
            return new DanskeBank(context);
        case IBankTypes.NORDEA_DK:
        	return new NordeaDK(context);
        case IBankTypes.VASTTRAFIK:
            return new Vasttrafik(context);
        case IBankTypes.EVERYDAYCARD:
            return new Everydaycard(context);
        case IBankTypes.MENIGA:
            return new Meniga(context);
        case IBankTypes.RIKSKORTET:
            return new TicketRikskortet(context);
        case IBankTypes.BIOKLUBBEN:
            return new Bioklubben(context);
        case IBankTypes.CHALMREST:
        	return new Chalmrest(context);
        case IBankTypes.MARGINALEN:
            return new Marginalen(context);
        case IBankTypes.SVENSKASPEL:
            return new SvenskaSpel(context);
        case IBankTypes.EASYCARD:
            return new EasyCard(context);
        case IBankTypes.APPEAKPOKER:
            return new AppeakPoker(context);
        case IBankTypes.TRUSTBUDDY:
            return new TrustBuddy(context);
        case IBankTypes.BRUMMER_KF:
       	    return new BrummerKF(context);
        case IBankTypes.ZIDISHA:
       	    return new Zidisha(context);
        case IBankTypes.BETTERGLOBE:
        	return new BetterGlobe(context);
        case IBankTypes.FOREX:
            return new ForexBank(context);
        case IBankTypes.BITCOIN:
            return new Bitcoin(context);
		default:
			throw new BankException("BankType id not found.");
		}
	}
	

	public static ArrayList<Bank> listBanks(Context context) {
		ArrayList<Bank> banks = new ArrayList<Bank>();
		banks.add(new AkeliusInvest(context));
		banks.add(new AkeliusSpar(context));
		banks.add(new TrustBuddy(context));
		banks.add(new BrummerKF(context));
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
        //banks.add(new Steam(context));
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
        banks.add(new McDonalds(context));
        banks.add(new SJPrio(context));
        banks.add(new SparbankenSyd(context));
//        banks.add(new SparbankenOresund(context));
        banks.add(new Opel(context));
        banks.add(new Skandiabanken(context));
        banks.add(new AmericanExpress(context));
        banks.add(new PlusGirot(context));
        banks.add(new Nordnetdirekt(context));
        banks.add(new Saab(context));
        banks.add(new Wallet(context));
        banks.add(new Chevrolet(context));
        banks.add(new Djurgarden(context));
        banks.add(new Quintessentially(context));
        banks.add(new Shell(context));
        banks.add(new Volkswagen(context));
        banks.add(new Audi(context));
        banks.add(new Preem(context));
        banks.add(new Seat(context));
        banks.add(new Skoda(context));
        banks.add(new IKEA(context));
//        banks.add(new DanskeBank(context));
        banks.add(new NordeaDK(context));
        banks.add(new Vasttrafik(context));
        banks.add(new Everydaycard(context));
        banks.add(new Meniga(context));
        banks.add(new TicketRikskortet(context));
        banks.add(new Bioklubben(context));
        banks.add(new Chalmrest(context));
        banks.add(new Marginalen(context));
        banks.add(new SvenskaSpel(context));
        banks.add(new EasyCard(context));
        banks.add(new AppeakPoker(context));
        banks.add(new Zidisha(context));
        banks.add(new BetterGlobe(context));
        banks.add(new ForexBank(context));
        banks.add(new EurobonusMastercardNo(context));
        banks.add(new Bitcoin(context));
        banks.add(new EurobonusMastercardDk(context));

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
							 c.getString(c.getColumnIndex("custname")),
							 c.getString(c.getColumnIndex("extras")));
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
                             c.getString(c.getColumnIndex("extras")));
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
                account.setAliasfor(c.getString(c.getColumnIndex("aliasfor")));
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
