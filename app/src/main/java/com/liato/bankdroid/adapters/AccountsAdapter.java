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

package com.liato.bankdroid.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;

public class AccountsAdapter extends BaseAdapter {
	public final static int VIEWTYPE_BANK = 0;
    public final static int VIEWTYPE_ACCOUNT = 1;
    public final static int VIEWTYPE_EMPTY = 2;
	private ArrayList<Bank> banks;
	private Context context;
	private LayoutInflater inflater;
	private boolean showHidden;
	SharedPreferences prefs;

    public AccountsAdapter(Context context, boolean showHidden) {
		this.context = context;
		this.banks = new ArrayList<Bank>();
		inflater = LayoutInflater.from(this.context);
		this.showHidden = showHidden;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public void addGroup(Bank bank) {
		banks.add(bank);
	}
	
	public void setGroups(ArrayList<Bank> banks) {
		this.banks = banks;
		/*for (Bank b : this.banks) {
		    ArrayList<Account> as = b.getAccounts(); 
		    for (Account a : as) {
		        if (a.isHidden() && !showHidden) {
		            as.remove(a);
		        }
		            
		    }
		}*/
	}

    public boolean isShowHidden() {
        return showHidden;
    }

    public void setShowHidden(boolean showHidden) {
        this.showHidden = showHidden;
    }
    
	public View newBankView(Bank bank, ViewGroup parent, View convertView) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.listitem_accounts_group, parent, false);
		}

		ImageView icon = (ImageView)convertView.findViewById(R.id.imgListitemAccountsGroup);
		((TextView)convertView.findViewById(R.id.txtListitemAccountsGroupAccountname)).setText(bank.getDisplayName());
		((TextView)convertView.findViewById(R.id.txtListitemAccountsGroupBankname)).setText(bank.getName());
        ((TextView) convertView
                .findViewById(R.id.txtListitemAccountsGroupTotal))
                .setText(Helpers.formatBalance(bank.getBalance(),
                        bank.getCurrency(),
                        prefs.getBoolean("round_balance", false) || !bank.getDisplayDecimals(), bank.getDecimalFormatter()));
		icon.setImageResource(bank.getImageResource());
		ImageView warning = (ImageView)convertView.findViewById(R.id.imgWarning);
		if (bank.isDisabled()) {
			warning.setVisibility(View.VISIBLE);
		}
		else {
			warning.setVisibility(View.INVISIBLE);
		}
		return convertView;
	}

	public View newAccountView(Account account, ViewGroup parent, View convertView) {
        if (account.isHidden() && !showHidden) {
            return convertView == null ? inflater.inflate(R.layout.empty, parent, false) : convertView;
        }
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.listitem_accounts_item, parent, false);
		}
		convertView.findViewById(R.id.divider).setBackgroundColor(Color.argb(30, 255, 255, 255));
		TextView txtAccountName = ((TextView)convertView.findViewById(R.id.txtListitemAccountsItemAccountname));
        TextView txtBalance = ((TextView)convertView.findViewById(R.id.txtListitemAccountsItemBalance));
		txtAccountName.setText(account.getName());
		txtBalance.setText(Helpers.formatBalance(account.getBalance(), account.getCurrency()));
        txtBalance
                .setText(Helpers.formatBalance(account.getBalance(),
                        account.getCurrency(),
                        prefs.getBoolean("round_balance", false) || !account.getBank().getDisplayDecimals(),
                        account.getBank().getDecimalFormatter()));
		if (account.isHidden()) {
            txtAccountName.setTextColor(Color.argb(255, 191, 191, 191));
            txtBalance.setTextColor(Color.argb(255, 191, 191, 191));		    
		}
		else {
            txtAccountName.setTextColor(Color.WHITE);
            txtBalance.setTextColor(Color.WHITE);            
		}
		return convertView;
	}

	@Override
	public int getCount() {
		int c = 0;
		for(Bank g : banks) {
			c += g.getAccounts().size()+1;
		}
		return c;
	}

	@Override
	public Object getItem(int position) {
		if (banks.size() == 0) {
			return null;
		}
		if (position == 0) {
			return banks.get(0);
		}

		int i = 0;
		for (Bank g : banks) {
			if (position == i) {
				return g;
			}
			else if (position <= (g.getAccounts().size()+i)) {
				return g.getAccounts().get(position-i-1);
			}
			i += g.getAccounts().size()+1;
		}

		return(null);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Object item = getItem(position);
		if (item == null) {
			return null;
		}
		if (item instanceof Bank) {
			return newBankView((Bank)item, parent, convertView);
		}
		else if (item instanceof Account) {
			return newAccountView((Account)item, parent, convertView);
		}
		return null;
	}

	public boolean isEnabled(int position) {
	    if (getItemViewType(position) == VIEWTYPE_EMPTY) return false;
	    return true;
	}
        

	@Override
	public int getViewTypeCount () {
		return 3;
	}

	@Override
	public int getItemViewType(int position) {
		Object item = getItem(position);
		if (item instanceof Bank) {
			return VIEWTYPE_BANK;
		}
		else {
		    if (((Account)item).isHidden() && !showHidden) {
		        return VIEWTYPE_EMPTY;
		    }
		}
		return VIEWTYPE_ACCOUNT;
	}	
}


