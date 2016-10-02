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

import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.db.Crypto;
import com.liato.bankdroid.db.DBAdapter;
import com.liato.bankdroid.db.Database;

import net.sf.andhsli.hotspotlogin.SimpleCrypto;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class BankFactory {

    public static Bank fromBanktypeId(int id, Context context) throws BankException {
        return LegacyBankFactory.fromBanktypeId(id, context);
    }


    public static List<Bank> listBanks(Context context) {
        return LegacyBankFactory.listBanks(context);
    }

    @Nullable
    public static Bank bankFromDb(long id, Context context, boolean loadAccounts) {
        Bank bank = null;
        DBAdapter db = DBAdapter.create(context);
        try {
            bank = db.getBank(id);
        } catch (BankException e) {
            Timber.w(e, "Failed getting bank from database");
        }
        return bank;
    }

    public static ArrayList<Bank> banksFromDb(Context context, boolean loadAccounts) {
        ArrayList<Bank> banks = new ArrayList<>();
        DBAdapter db = DBAdapter.create(context);
        try {
            banks.addAll(db.fetchBanks());
        } catch (BankException ex) {
            Timber.w(ex, "Failed getting bank from database");
        }

        return banks;
    }

    public static Account accountFromDb(Context context, String accountId,
            boolean loadTransactions) {
        DBAdapter db = DBAdapter.create(context);
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
}
