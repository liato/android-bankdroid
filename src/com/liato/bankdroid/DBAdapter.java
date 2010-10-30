package com.liato.bankdroid;

import java.util.ArrayList;

import net.sf.andhsli.hotspotlogin.SimpleCrypto;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DBAdapter {

    private static final String TAG = "DBAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    private static final String DATABASE_NAME = "data";
    private static final int DATABASE_VERSION = 8;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table banks (_id integer primary key autoincrement, "
            		+ "balance text not null, "
                    + "banktype integer not null, username text not null, "
                    + "password text not null, disabled integer);");
            db.execSQL("create table accounts (bankid integer not null, id text not null, balance text not null, name text not null);");
            db.execSQL("create table transactions (_id integer primary key autoincrement, transdate text not null, btransaction text not null, amount text not null, account text not null);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS banks;");
            db.execSQL("DROP TABLE IF EXISTS accounts;");
            db.execSQL("DROP TABLE IF EXISTS transactions;");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public DBAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public DBAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    
    public void close() {
        mDbHelper.close();
        mDb.close();
    }


    public long createBank(Bank bank) {
    	return updateBank(bank);
    }

    
    /**
     * Delete the bank with the given bankId
     * 
     * @param bankId id of bank to delete
     */
    public int deleteBank(long bankId) {
        int c = mDb.delete("banks", "_id=" + bankId, null);
        c += this.deleteAccounts(bankId);
        return c;
    }
    
    /**
     * Delete the accounts for the given bankIdbank with the given rowId
     * 
     * @param bankId id of bank to delete
     */    
    public int deleteAccounts(long bankId) {
        int c = mDb.delete("accounts", "bankid=" + bankId, null);
        return c;
    }

    
    public int deleteTransactions(String account) {
        int c = mDb.delete("transactions", "account='" + account + "'", null);
        return c;
    }    
    		
    /**
     * Return a Cursor over the list of all banks in the database
     * 
     * @return Cursor over all banks
     */
    public Cursor fetchBanks() {
        return mDb.query("banks", new String[] {"_id", "balance", "banktype", "username", "password", "disabled"}, null, null, null, null, "_id asc");
    }

    
    /**
     * Return a Cursor over the list of all accounts belonging to a bank
     * 
     * @return Cursor over all accounts belonging to a bank
     */
    public Cursor fetchAccounts(long bankId) {
        return mDb.query("accounts", new String[] {"bankid", "balance", "name", "id"}, "bankid="+bankId, null, null, null, null);
    }
    
    public Cursor fetchTransactions(String account) {
        return mDb.query("transactions", new String[] {"transdate", "btransaction", "amount"}, "account='"+account+"'", null, null, null, null);
    }    

    public long updateBank(Bank bank) {
    	Log.d(TAG, "Updating bank");
    	ContentValues initialValues = new ContentValues();
        initialValues.put("banktype", bank.getBanktypeId());
        initialValues.put("username", bank.getUsername());
        try {
			initialValues.put("password", SimpleCrypto.encrypt(Crypto.getKey(), bank.getPassword()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        initialValues.put("disabled", 0);
        initialValues.put("balance", bank.getBalance().toPlainString());
        long bankId = bank.getDbId();
        Log.d(TAG, "Bankid: "+bankId);
        if (bankId == -1) {
            Log.d(TAG, "Inserting new bank");
        	bankId = mDb.insert("banks", null, initialValues);
        }
        else {
            Log.d(TAG, "Updating existing bank");
        	mDb.update("banks", initialValues, "_id="+bankId, null);
            deleteAccounts(bankId);
        }
        if (bankId != -1) {
	        ArrayList<Account> accounts = bank.getAccounts();
            Log.d(TAG, "Bank accounts: "+bank.getAccounts().size());
	        for(Account acc : accounts) {
	            ContentValues vals = new ContentValues();
	            vals.put("bankid", bankId);
	            vals.put("balance", acc.getBalance().toPlainString());
	            vals.put("name", acc.getName());
	            vals.put("id", new Long(bankId).toString()+"_"+acc.getId());
	            mDb.insert("accounts", null, vals);
	            ArrayList<Transaction> transactions = acc.getTransactions();
	            if (transactions != null && !transactions.isEmpty()) {
	                deleteTransactions(new Long(bankId).toString()+"_"+acc.getId());
		            for(Transaction transaction : transactions) {
			            ContentValues transvals = new ContentValues();
			            transvals.put("transdate", transaction.getDate());
			            transvals.put("btransaction", transaction.getTransaction());
			            transvals.put("amount", transaction.getAmount().toPlainString());
			            transvals.put("account", new Long(bankId).toString()+"_"+acc.getId());
			            mDb.insert("transactions", null, transvals);
		            }
	            }
	        }
        }
        Log.d(TAG, "Updated bank: "+bankId);
        return bankId;
    }
    
    public void disableBank(long bankId) {
    	if (bankId == -1) return;
        ContentValues initialValues = new ContentValues();
        initialValues.put("disabled", 1);
    	mDb.update("banks", initialValues, "_id="+bankId, null);
    }
    
    public Cursor getBank(String bankId) {
    	Cursor c = mDb.query("banks", new String[] {"_id", "balance", "banktype", "username", "password", "disabled"}, "_id="+bankId, null, null, null, null);
    	if (c != null) {
    		c.moveToFirst();
    	}
		return c;
    }
    
    public Cursor getBank(long bankId) {
    	return getBank(new Long(bankId).toString());
    }

    public Cursor getAccount(String id) {
    	Cursor c = mDb.query("accounts", new String[] {"id", "balance", "name", "bankid"}, "id='"+id+"'", null, null, null, null);
    	if (c != null) {
    		c.moveToFirst();
    	}
		return c;
    }
    

}
