package com.liato.bankdroid;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class TransactionsActivity extends LockableActivity {
	final static String TAG = "TransactionActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.transactions);
		Bundle extras = getIntent().getExtras();
		Bank bank = BankFactory.bankFromDb(extras.getLong("bank"), this, false);
		Account account = BankFactory.accountFromDb(this, extras.getLong("bank")+"_"+extras.getString("account"), true);
		TextView viewBankName = (TextView)findViewById(R.id.txtListitemAccountsGroupAccountname);
		TextView viewAccountName = (TextView)findViewById(R.id.txtListitemAccountsGroupBankname);
		TextView viewAccountBalance = (TextView)findViewById(R.id.txtListitemAccountsGroupTotal);
		ListView viewTransactionsList = (ListView)findViewById(R.id.lstTransactionsList);
		ImageView icon = (ImageView)findViewById(R.id.imgListitemAccountsGroup);
		viewBankName.setText(bank.getUsername());
		viewAccountName.setText(account.getName());
		viewAccountBalance.setText(Helpers.formatBalance(account.getBalance()));
		icon.setImageResource(bank.getImageResource());
		ArrayList<Transaction> transactions = account.getTransactions();
		Log.d(TAG, "Transactions: "+transactions.size());
		if (transactions.size() > 0) {
			findViewById(R.id.txtTranDesc).setVisibility(View.GONE);
			TransactionsAdapter adapter = new TransactionsAdapter(transactions);
			viewTransactionsList.setAdapter(adapter);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private class TransactionsAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		private ArrayList<Object> items = new ArrayList<Object>();

		public TransactionsAdapter(ArrayList<Transaction> transactions) {
			inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (!transactions.isEmpty()) {
				String date = transactions.get(0).getDate();
				items.add(date);
				for (Transaction transaction : transactions) {
					if (!date.equals(transaction.getDate())) {
						date = transaction.getDate();
						items.add(date);
					}
					items.add(transaction);
				}
				
			}
		}

		public View newTransactionView(Transaction transaction, ViewGroup parent, View convertView) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.transaction_item, parent, false);
			}
			((TextView)convertView.findViewById(R.id.txtTransaction)).setText(transaction.getTransaction());
			((TextView)convertView.findViewById(R.id.txtAmount)).setText(Helpers.formatBalance(transaction.getAmount()));
			if (transaction.getAmount().signum() == 1) {
				((ImageView)convertView.findViewById(R.id.imgColor)).setBackgroundResource(R.drawable.transaction_positive);
			}
			else {
				((ImageView)convertView.findViewById(R.id.imgColor)).setBackgroundResource(R.drawable.transaction_negative);
			}
			return convertView;
		}

		public View newDateView(String date, ViewGroup parent, View convertView) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.transaction_date, parent, false);
			}
			((TextView)convertView.findViewById(R.id.txtDate)).setText(date);
			return convertView;
		}		

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Object getItem(int position) {
			return items.get(position);
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
			if (item instanceof Transaction) {
				return newTransactionView((Transaction)item, parent, convertView);
			}
			else if (item instanceof String) {
				return newDateView((String)item, parent, convertView);
			}
			return null;
		}

		@Override
		public boolean areAllItemsEnabled() {
			return true;
		}

		@Override
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
			if (item instanceof Transaction) {
				return 0;
			}
			return 1;
		}

	}
	
}
