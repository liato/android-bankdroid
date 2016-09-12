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

package com.liato.bankdroid;

import com.liato.bankdroid.adapters.AccountsAdapter;
import com.liato.bankdroid.appwidget.AutoRefreshService;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.BankFactory;
import com.liato.bankdroid.db.DBAdapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends LockableActivity {

    @InjectView(R.id.txtAccountsDesc)
    TextView mAccountsDescription;

    protected static boolean showHidden = false;

    private static Bank selected_bank = null;

    private static Account selected_account = null;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            refreshView();
        }
    };

    protected AccountsAdapter adapter = null;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PairApplicationsActivity.initialSetupApiKey(this);

        setContentView(R.layout.main);
        ButterKnife.inject(this);

        adapter = new AccountsAdapter(this, showHidden);
        final ArrayList<Bank> banks = new ArrayList<>();
        adapter.setGroups(banks);
        final ListView lv = (ListView) findViewById(R.id.lstAccountsList);
        lv.setAdapter(adapter);
        lv.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, final View view,
                    final int position, final long id) {
                if (adapter.getItem(position) instanceof Account) {
                    selected_account = (Account) adapter.getItem(position);
                    final PopupMenuAccount pmenu = new PopupMenuAccount(parent, view, MainActivity.this);
                    pmenu.showLikeQuickAction(0, 12);
                    return true;
                } else if (adapter.getItem(position) instanceof Bank) {
                    selected_bank = (Bank) adapter.getItem(position);
                    selected_bank.toggleHideAccounts();
                    DBAdapter.save(selected_bank, MainActivity.this);
                    refreshView();
                    return true;
                }
                return false;
            }
        });
        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view,
                    final int position, final long id) {
                if (adapter.getItem(position) instanceof Bank) {
                    selected_bank = (Bank) adapter.getItem(position);
                    final PopupMenuBank pmenu = new PopupMenuBank(parent, view, MainActivity.this);
                    pmenu.showLikeQuickAction(0, 12);
                } else {
                    final Intent intent = new Intent(MainActivity.this, TransactionsActivity.class);
                    final Account account = (Account) adapter.getItem(position);
                    intent.putExtra("account", account.getId());
                    intent.putExtra("bank", account.getBankDbId());
                    MainActivity.this.startActivity(intent);
                }
            }
        });

        final Bundle extras = getIntent().getExtras();
        // Clicking on widgets opens the transactions history through MainActivity so that
        // the user can back out to the main window.
        if (AutoRefreshService.ACTION_MAIN_SHOW_TRANSACTIONS.equals(getIntent().getAction())) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (prefs.getBoolean("widget_opens_transactions", true)) {
                skipLockOnce();
                final Intent intent = new Intent(this, TransactionsActivity.class);
                intent.putExtra("account", extras.getString("account"));
                intent.putExtra("bank", extras.getLong("bank"));
                startActivity(intent);
            }
        }
        ((BankdroidApplication) getApplication()).showAndDeleteApplicationMessage();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Receive refresh Intent from AutoRefreshService and refresh the main view if changes
        // have been detected.
        registerReceiver(receiver, new IntentFilter(AutoRefreshService.BROADCAST_MAIN_REFRESH));
        refreshView();
    }

    public void refreshView() {
        final ArrayList<Bank> banks = BankFactory.banksFromDb(this, true);
        mAccountsDescription.setVisibility(banks.isEmpty() ? View.VISIBLE : View.GONE);

        adapter.setShowHidden(showHidden);
        adapter.setGroups(banks);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        final MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.main, menu);
        return true;
    }


    @Override
    protected Dialog onCreateDialog(final int id) {
        super.onCreateDialog(id);
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.about);
        dialog.setTitle(getString(R.string.about));
        String version = BuildConfig.VERSION_NAME;
        ((TextView) dialog.findViewById(R.id.txtVersion))
                .setText(getText(R.string.version).toString().replace("$version", version));
        return dialog;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_toggle_hidden:
                showHidden = !showHidden;
                item.setTitle(showHidden ? R.string.menu_hide_hidden : R.string.menu_show_hidden);
                refreshView();
                return true;
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                this.startActivity(intent);
                //Helpers.setActivityAnimation(this, R.anim.zoom_enter, R.anim.zoom_exit);
                return true;
            case R.id.action_about:
                intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_refresh:
                new DataRetrieverTask(MainActivity.this).execute();
                return true;
            case R.id.action_add:
                final Intent intentAccount = new Intent(MainActivity.this, BankEditActivity.class);
                startActivity(intentAccount);
                return true;

        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    /**
     * Extends {@link BetterPopupWindow}
     * <p>
     * Overrides onCreate to create the view and register the button listeners
     *
     * @author qbert
     */
    private static class PopupMenuBank extends BetterPopupWindow implements OnClickListener {

        MainActivity parent = null;

        public PopupMenuBank(final ViewGroup parentView, final View anchor, final MainActivity parent) {
            super(parentView, anchor);
            this.parent = parent;
        }

        @Override
        protected void onCreate() {
            // inflate layout
            final LayoutInflater inflater =
                    (LayoutInflater) this.anchor.getContext()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            final ViewGroup root = (ViewGroup) inflater.inflate(R.layout.popup_bank, this.parentView);
            final Button btnHide = (Button) root.findViewById(R.id.btnHide);
            final Button btnUnhide = (Button) root.findViewById(R.id.btnUnhide);
            final Button btnWWW = (Button) root.findViewById(R.id.btnWWW);
            if (selected_bank.getHideAccounts()) {
                btnHide.setVisibility(View.GONE);
                btnUnhide.setVisibility(View.VISIBLE);
                btnUnhide.setOnClickListener(this);
            } else {
                btnHide.setVisibility(View.VISIBLE);
                btnUnhide.setVisibility(View.GONE);
                btnHide.setOnClickListener(this);
            }
            if (selected_bank.isWebViewEnabled()) {
                btnWWW.setOnClickListener(this);
            } else {
                btnWWW.setVisibility(View.GONE);
            }
            root.findViewById(R.id.btnRefresh).setOnClickListener(this);
            root.findViewById(R.id.btnEdit).setOnClickListener(this);
            root.findViewById(R.id.btnRemove).setOnClickListener(this);

            this.setContentView(root);
        }

        @Override
        public void onClick(final View v) {
            final Context context = this.anchor.getContext();
            final int id = v.getId();
            switch (id) {
                case R.id.btnHide:
                case R.id.btnUnhide:
                    this.dismiss();
                    selected_bank.toggleHideAccounts();
                    DBAdapter.save(selected_bank, context);
                    parent.refreshView();
                    return;
                case R.id.btnWWW:
                    if (selected_bank != null && selected_bank.isWebViewEnabled()) {
                        //Uri uri = Uri.parse(selected_bank.getURL());
                        //Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        final Intent intent = new Intent(context, WebViewActivity.class);
                        intent.putExtra("bankid", selected_bank.getDbId());
                        context.startActivity(intent);
                    }
                    this.dismiss();
                    return;
                case R.id.btnEdit:
                    final Intent intent = new Intent(context, BankEditActivity.class);
                    intent.putExtra("id", selected_bank.getDbId());
                    context.startActivity(intent);
                    this.dismiss();
                    return;
                case R.id.btnRefresh:
                    this.dismiss();
                    new DataRetrieverTask(parent, selected_bank.getDbId()).execute();
                    return;
                case R.id.btnRemove:
                    this.dismiss();
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    //builder.setMessage(getText(R.string.passwords_mismatch)).setTitle(getText(R.string.passwords_mismatch_title))
                    builder.setMessage(context.getText(R.string.remove_bank_msg))
                            .setTitle(context.getText(R.string.remove_bank_title))
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(context.getText(R.string.yes),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(final DialogInterface dialog,
                                                final int id) {
                                            final DBAdapter db = DBAdapter.create(context);
                                            db.deleteBank(selected_bank.getDbId());
                                            dialog.cancel();
                                            parent.refreshView();
                                        }
                                    })
                            .setNegativeButton(context.getText(R.string.no),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(final DialogInterface dialog,
                                                final int id) {
                                            dialog.cancel();
                                        }
                                    });
                    final AlertDialog alert = builder.create();
                    alert.show();
                    return;
            }

        }
    }


    /**
     * Extends {@link BetterPopupWindow}
     * <p>
     * Overrides onCreate to create the view and register the button listeners
     *
     * @author qbert
     */
    private static class PopupMenuAccount extends BetterPopupWindow implements OnClickListener {

        MainActivity parent = null;

        public PopupMenuAccount(final ViewGroup parentView, final View anchor, final MainActivity parent) {
            super(parentView, anchor);
            this.parent = parent;
        }

        @Override
        protected void onCreate() {
            final LayoutInflater inflater =
                    (LayoutInflater) this.anchor.getContext()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            final ViewGroup root = (ViewGroup) inflater.inflate(R.layout.popup_account, this.parentView);
            final Button btnHide = (Button) root.findViewById(R.id.btnHide);
            final Button btnUnhide = (Button) root.findViewById(R.id.btnUnhide);
            final Button btnDisableNotifications = (Button) root
                    .findViewById(R.id.btnDisableNotifications);
            final Button btnEnableNotifications = (Button) root
                    .findViewById(R.id.btnEnableNotifications);
            if (selected_account.isHidden()) {
                btnHide.setVisibility(View.GONE);
                btnUnhide.setVisibility(View.VISIBLE);
                btnUnhide.setOnClickListener(this);
            } else {
                btnHide.setVisibility(View.VISIBLE);
                btnUnhide.setVisibility(View.GONE);
                btnHide.setOnClickListener(this);
            }
            if (selected_account.isNotify()) {
                btnDisableNotifications.setVisibility(View.VISIBLE);
                btnDisableNotifications.setOnClickListener(this);
                btnEnableNotifications.setVisibility(View.GONE);
            } else {
                btnDisableNotifications.setVisibility(View.GONE);
                btnEnableNotifications.setOnClickListener(this);
                btnEnableNotifications.setVisibility(View.VISIBLE);
            }
            this.setContentView(root);
        }

        @Override
        public void onClick(final View v) {
            final int id = v.getId();
            switch (id) {
                case R.id.btnHide:
                    this.dismiss();
                    selected_account.setHidden(true);
                    DBAdapter.save(selected_account.getBank(), parent);
                    parent.refreshView();
                    return;
                case R.id.btnUnhide:
                    this.dismiss();
                    selected_account.setHidden(false);
                    DBAdapter.save(selected_account.getBank(), parent);
                    parent.refreshView();
                    return;
                case R.id.btnEnableNotifications:
                    this.dismiss();
                    selected_account.setNotify(true);
                    DBAdapter.save(selected_account.getBank(), parent);
                    parent.refreshView();
                    return;
                case R.id.btnDisableNotifications:
                    this.dismiss();
                    selected_account.setNotify(false);
                    DBAdapter.save(selected_account.getBank(), parent);
                    parent.refreshView();
                    return;

            }

        }
    }

}
