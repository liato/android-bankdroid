package com.liato.bankdroid;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AccountsAdapter extends BaseAdapter {
	public final static int VIEWTYPE_BANK = 0;
	public final static int VIEWTYPE_ACCOUNT = 1;
	private ArrayList<Bank> groups;
	private Context context;
	private LayoutInflater inflater;

	public AccountsAdapter(Context context) {
		this.context = context;
		this.groups = new ArrayList<Bank>();
		inflater = LayoutInflater.from(context);
		
	}

	public void addGroup(Bank group) {
		groups.add(group);
	}
	
	public void setGroups(ArrayList<Bank> banks) {
		groups = banks;
	}

	public View newGroupView(Bank group, ViewGroup parent, View convertView) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.listitem_accounts_group, parent, false);
		}
		//Log.d("Convertview", ""+convertView);
		//Log.d("Missing view?", ""+convertView.findViewById(R.id.txtListitemAccountsGroupAccountname));
		ImageView icon = (ImageView)convertView.findViewById(R.id.imgListitemAccountsGroup);
		((TextView)convertView.findViewById(R.id.txtListitemAccountsGroupAccountname)).setText(group.getUsername());
		((TextView)convertView.findViewById(R.id.txtListitemAccountsGroupBankname)).setText(group.getName());
		((TextView)convertView.findViewById(R.id.txtListitemAccountsGroupTotal)).setText(Helpers.formatBalance(group.getBalance()));
		icon.setImageResource(context.getResources().getIdentifier("drawable/"+group.getShortName(), null, context.getPackageName()));
		ImageView warning = (ImageView)convertView.findViewById(R.id.imgWarning);
		Log.d("AccountsAdapter", ""+group.isDisabled());
		if (group.isDisabled()) {
			warning.setVisibility(View.VISIBLE);
		}
		else {
			warning.setVisibility(View.INVISIBLE);
		}
		return convertView;
	}

	public View newItemView(Account item, ViewGroup parent, View convertView) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.listitem_accounts_item, parent, false);
		}
		//Log.d("Convertview", ""+convertView);
		//Log.d("Missing view?", ""+convertView.findViewById(R.id.txtListitemAccountsItemAccountname));
		
		((TextView)convertView.findViewById(R.id.txtListitemAccountsItemAccountname)).setText(item.getName());
		((TextView)convertView.findViewById(R.id.txtListitemAccountsItemBalance)).setText(Helpers.formatBalance(item.getBalance()));
		return convertView;
	}

	@Override
	public int getCount() {
		int c = 0;
		for(Bank g : groups) {
			c += g.getAccounts().size()+1;
		}
		return c;
	}

	@Override
	public Object getItem(int position) {
		if (groups.size() == 0) {
			return null;
		}
		if (position == 0) {
			return groups.get(0);
		}

		int i = 0;
		for (Bank g : groups) {
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
			return newGroupView((Bank)item, parent, convertView);
		}
		else if (item instanceof Account) {
			return newItemView((Account)item, parent, convertView);
		}
		return null;
	}

	public boolean isEnabled(int position) {
		return true;
	}

	@Override
	public int getViewTypeCount () {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		Object item = getItem(position);
		if (item instanceof Bank) {
			return VIEWTYPE_BANK;
		}
		return VIEWTYPE_ACCOUNT;
	}	
}


