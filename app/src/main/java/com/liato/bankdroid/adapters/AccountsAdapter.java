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

import com.liato.bankdroid.Helpers;
import com.liato.bankdroid.R;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class AccountsAdapter extends BaseAdapter {

    public final static int VIEWTYPE_BANK = 0;

    public final static int VIEWTYPE_ACCOUNT = 1;

    public final static int VIEWTYPE_EMPTY = 2;

    private final SharedPreferences prefs;

    private ArrayList<Bank> banks;

    private final LayoutInflater inflater;

    private boolean showHidden;

    public AccountsAdapter(Context context, boolean showHidden) {
        this.banks = new ArrayList<>();
        inflater = LayoutInflater.from(context);
        this.showHidden = showHidden;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setGroups(ArrayList<Bank> banks) {
        this.banks = banks;
    }

    public void setShowHidden(boolean showHidden) {
        this.showHidden = showHidden;
    }

    private View newBankView(Bank bank, ViewGroup parent, View convertView) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listitem_accounts_group, parent, false);
        }

        ImageView icon = (ImageView) convertView.findViewById(R.id.imgListitemAccountsGroup);
        ((TextView) convertView.findViewById(R.id.txtListitemAccountsGroupAccountname))
                .setText(bank.getDisplayName());
        ((TextView) convertView.findViewById(R.id.txtListitemAccountsGroupBankname))
                .setText(bank.getName());
        ((TextView) convertView
                .findViewById(R.id.txtListitemAccountsGroupTotal))
                .setText(Helpers.formatBalance(bank.getBalance(),
                        bank.getCurrency(),
                        prefs.getBoolean("round_balance", false) || !bank.getDisplayDecimals(),
                        bank.getDecimalFormatter(),
                        false));
        icon.setImageResource(bank.getImageResource());
        View warning = convertView.findViewById(R.id.txtDisabledWarningX);
        if (bank.isDisabled()) {
            warning.setVisibility(View.VISIBLE);
        } else {
            warning.setVisibility(View.GONE);
        }
        return convertView;
    }

    private View newAccountView(Account account, ViewGroup parent, View convertView) {
        if ((account.isHidden() && !showHidden) || account.getBank().getHideAccounts()) {
            return convertView == null ? inflater.inflate(R.layout.empty, parent, false)
                    : convertView;
        }
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listitem_accounts_item, parent, false);
        }
        convertView.findViewById(R.id.divider).setBackgroundColor(Color.argb(30, 255, 255, 255));
        TextView txtAccountName = ((TextView) convertView
                .findViewById(R.id.txtListitemAccountsItemAccountname));
        TextView txtBalance = ((TextView) convertView
                .findViewById(R.id.txtListitemAccountsItemBalance));
        txtAccountName.setText(account.getName());
        txtBalance.setText(Helpers.formatBalance(account.getBalance(), account.getCurrency()));
        txtBalance
                .setText(Helpers.formatBalance(account.getBalance(),
                        account.getCurrency(),
                        prefs.getBoolean("round_balance", false) || !account.getBank()
                                .getDisplayDecimals(),
                        account.getBank().getDecimalFormatter(),
                        false));
        if (account.isHidden()) {
            txtAccountName.setTextColor(Color.argb(255, 191, 191, 191));
            txtBalance.setTextColor(Color.argb(255, 191, 191, 191));
        } else {
            txtAccountName.setTextColor(Color.WHITE);
            txtBalance.setTextColor(Color.WHITE);
        }
        return convertView;
    }

    @Override
    public int getCount() {
        int c = 0;
        for (Bank g : banks) {
            if (g.getHideAccounts()) {
                c++;
            } else {
                c += g.getAccounts().size() + 1;
            }
        }
        return c;
    }

    @Override
    @Nullable
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
            } else if (g.getHideAccounts()) {
                i++;
                continue;
            } else if (position <= (g.getAccounts().size() + i)) {
                return g.getAccounts().get(position - i - 1);
            }
            i += g.getAccounts().size() + 1;
        }

        return (null);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    @Nullable
    public View getView(int position, View convertView, ViewGroup parent) {
        Object item = getItem(position);
        if (item == null) {
            return null;
        }
        if (item instanceof Bank) {
            return newBankView((Bank)item, parent, convertView);
        } else if (item instanceof Account) {
            return newAccountView((Account)item, parent, convertView);
        }
        return null;
    }

    @Override
    public boolean isEnabled(int position) {
        if (getItemViewType(position) == VIEWTYPE_EMPTY) {
            return false;
        }
        return true;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        Object item = getItem(position);
        if (item instanceof Bank) {
            return VIEWTYPE_BANK;
        } else {
            final Account account = (Account)item;
            if ((account.isHidden() && !showHidden) ||
                    account.getBank().getHideAccounts()) {
                return VIEWTYPE_EMPTY;
            }
        }
        return VIEWTYPE_ACCOUNT;
    }
}


