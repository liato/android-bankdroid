package com.liato.bankdroid;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AccountsAdapter extends BaseAdapter {
	private ArrayList<Group> groups;
	private Context context;

	public AccountsAdapter(Context context) {
		this.context = context;
		this.groups = new ArrayList<Group>();
	}

	public void addGroup(Group group) {
		groups.add(group);
	}

	public View newGroupView(Group group, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.listitem_accounts_group, parent, false);
		ImageView icon = (ImageView)v.findViewById(R.id.imgListitemAccountsGroup);
		((TextView)v.findViewById(R.id.txtListitemAccountsGroupAccountname)).setText(group.getName());
		((TextView)v.findViewById(R.id.txtListitemAccountsGroupBankname)).setText(group.getType());
		((TextView)v.findViewById(R.id.txtListitemAccountsGroupTotal)).setText(Helpers.formatBalance(group.getTotal()));
		icon.setImageResource(context.getResources().getIdentifier("drawable/"+Helpers.toAscii(group.getType().toLowerCase()), null, context.getPackageName()));
		ImageView warning = (ImageView)v.findViewById(R.id.imgWarning);
		Log.d("AccountsAdapter", ""+group.getDisabled());
		if (group.getDisabled()) {
			warning.setVisibility(View.VISIBLE);
		}
		else {
			warning.setVisibility(View.INVISIBLE);
		}
		return v;
	}

	public View newItemView(Item item, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.listitem_accounts_item, parent, false);
		((TextView)v.findViewById(R.id.txtListitemAccountsItemAccountname)).setText(item.getName());
		((TextView)v.findViewById(R.id.txtListitemAccountsItemBalance)).setText(Helpers.formatBalance(item.getBalance()));
		//v.setOnClickListener(this);
		return v;
	}

	@Override
	public int getCount() {
		int c = 0;
		for(Group g : groups) {
			c += g.getItems().size()+1;
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
		for (Group g : groups) {
			if (position == i) {
				return g;
			}
			else if (position <= (g.getItems().size()+i)) {
				return g.getItems().get(position-i-1);
			}
			i += g.getItems().size()+1;
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
		if (item instanceof Group) {
			return newGroupView((Group)item, parent);
		}
		else if (item instanceof Item) {
			return newItemView((Item)item, parent);
		}
		return null;
	}

	public boolean isEnabled(int position) {
		if (getItem(position) instanceof Item) {
			return true;
		}
		return false;
	}

	public final static class Group {
		private String name;
		private String type;
		private BigDecimal total;
		private List<Item> items;
		private Boolean disabled;
		public Group(String name, String type, Double total, List<Item> items, Boolean disabled) {
			this.name = name;
			this.type = type;
			this.total = new BigDecimal(total);
			for(Item item : items) {
				item.setGroup(this);
			}
			this.items = items;
			this.disabled = disabled;
		}
		public Group(String name, String type, Double total, Item item, Boolean disabled) {
			ArrayList<Item> items = new ArrayList<Item>();
			items.add(item);
			this.name = name;
			this.type = type;
			this.total = new BigDecimal(total);
			this.items = items;
			this.disabled = disabled;
		}
		public String getName() {
			return name;
		}
		public String getType() {
			return type;
		}
		public BigDecimal getTotal() {
			return total;
		}
		public List<Item> getItems() {
			return items;
		}
		public Boolean getDisabled() {
			return disabled;
		}

	}

	public final static class Item {
		private String name;
		private BigDecimal balance;
		private String id;
		private Group group;
		public Item (String name, Double balance, String id) {
			this.name = name;
			this.balance = new BigDecimal(balance);
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public BigDecimal getBalance() {
			return balance;
		}
		public String getId() {
			return id;
		}
		public Group getGroup() {
			return group;
		}
		public void setGroup(Group group) {
			this.group = group;
		}		
	}	

}


