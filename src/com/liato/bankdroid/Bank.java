package com.liato.bankdroid;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.res.Resources;

import com.liato.urllib.Urllib;

public abstract class Bank implements Comparable<Bank> {
	public final static int SWEDBANK = 1;
	public final static int NORDEA = 2;
	public final static int ICABANKEN = 3;
	public final static int LANSFORSAKRINGAR = 4;
	public final static int HANDELSBANKEN = 5;
	public final static int COOP = 6;
	public final static int ICA = 7;
	public final static int STATOIL = 8;
	public final static int AVANZA = 9;
	public final static int VILLABANKEN = 10;
	public final static int AVANZAMINI = 11;
	public final static int OKQ8 = 12;
	public final static int EUROCARD = 13;
	public final static int FIRSTCARD = 14;
	

	protected String TAG = "Bank";
	protected String NAME = "Bank";
	protected String NAME_SHORT = "bank";
	protected int BANKTYPE_ID = 0;
	protected String URL;

	protected Context context;
	protected Resources res;

    protected boolean usernameNumeric = false;
    protected boolean passwordNumeric = false;
	
	protected String username;
	protected String password;
	protected ArrayList<Account> accounts = new ArrayList<Account>();
	protected HashMap<String, Account> oldAccounts;
	protected BigDecimal balance = new BigDecimal(0);
	protected boolean disabled = false;
	protected long dbid = -1;
	protected Urllib urlopen = null;
	protected String customName;
	protected String currency = "SEK";


	public Urllib getUrlopen() {
		return urlopen;
	}

	public void setUrlopen(Urllib urlopen) {
		this.urlopen = urlopen;
	}

	public void setDbid(long dbid) {
		this.dbid = dbid;
	}

	public Bank(Context context) {
		this.context = context;
		this.res = this.context.getResources();
	}

	public void update(String username, String password) throws BankException, LoginException {
		this.username = username;
		this.password = password;
		this.update();
	}

	public void update() throws BankException, LoginException {
		balance = new BigDecimal(0);
		oldAccounts = new HashMap<String, Account>();
		for(Account account: accounts) {
		    oldAccounts.put(account.getId(), account);
		}
		accounts = new ArrayList<Account>();
	}

	public void updateTransactions(Account account, Urllib urlopen) throws LoginException, BankException {
	}

	public void updateAllTransactions() throws LoginException, BankException {
		if (urlopen == null) {
			urlopen = login();
		}
		for (Account account: accounts) {
			updateTransactions(account, urlopen);
		}
		if (urlopen != null) {
			urlopen.close();
		}
		
	}
	
	public Urllib login() throws LoginException, BankException {
		return null;
	}

	public void closeConnection() {
		if (urlopen != null) {
			urlopen.close();
		}
	}
	public ArrayList<Account> getAccounts() {
		return this.accounts;
	}
	
	public void setAccounts(ArrayList<Account> accounts) {
		this.accounts = accounts;
		for (Account a : accounts) {
		    a.setBank(this);
		}
	}

	public String getPassword() {
		return password;
	}

	public String getUsername() {
		return username;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public int getBanktypeId() {
		return BANKTYPE_ID;
	}

	public String getName() {
		return NAME;
	}

	public String getDisplayName() {
		if (customName != null && customName.length() > 0) return customName;
		return username;
	}

	
	public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getShortName() {
		return NAME_SHORT;
	}

	public void setData(String username, String password, BigDecimal balance,
			boolean disabled, long dbid, String currency, String customName) {
		this.username = username;
		this.password = password;
		this.balance = balance;
		this.disabled = disabled;
		this.dbid = dbid;
		this.currency = currency;
		this.customName = customName;
	}
	
	public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public long getDbId() {
		return dbid;
	}

	public boolean isDisabled() {
		return disabled;
	}
	
	public void disable() {
		DBAdapter db = new DBAdapter(context);
		db.open();
		db.disableBank(dbid);
		db.close();
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}


	public void setPassword(String password) {
		this.password = password;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public void save() {
		DBAdapter db = new DBAdapter(context);
		db.open();
		db.updateBank(this);
		db.close();
	}
	
	public String getURL() {
		return URL;
	}
	
    public boolean isUsernameNumeric() {
        return usernameNumeric;
    }

    public boolean isPasswordNumeric() {
        return passwordNumeric;
    }	
	
	public int getImageResource() {
		return res.getIdentifier("logo_"+NAME_SHORT, "drawable", context.getPackageName());	
	}

	public int compareTo(Bank another) {
		return this.toString().compareToIgnoreCase(another.toString());
	}

    public void updateComplete() {
        for (Account a : this.accounts) {
            //Preserve hidden and notify settings from old accounts
            if (oldAccounts != null) {
                Account oa = oldAccounts.get(a.getId());
                if (oa != null) {
                    a.setHidden(oa.isHidden());
                    a.setNotify(oa.isNotify());
                }
            }
            a.setBank(this);
        }
    }	
	
}
