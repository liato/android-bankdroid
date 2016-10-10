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

import com.liato.bankdroid.appwidget.AutoRefreshService;
import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.banking.BankFactory;
import com.liato.bankdroid.banking.exceptions.BankChoiceException;
import com.liato.bankdroid.banking.exceptions.BankException;
import com.liato.bankdroid.banking.exceptions.LoginException;
import com.liato.bankdroid.db.DBAdapter;
import com.liato.bankdroid.utils.NetworkUtils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class DataRetrieverTask extends AsyncTask<String, String, Void> {

    private final static String TAG = "DataRetrieverTask";

    private final ProgressDialog dialog;

    private final MainActivity parent;

    private final Resources res;

    private ArrayList<String> errors;

    private long bankId = -1;

    public DataRetrieverTask(final MainActivity parent) {
        this.parent = parent;
        this.res = parent.getResources();
        this.dialog = new ProgressDialog(parent);
    }

    public DataRetrieverTask(final MainActivity parent, final long bankId) {
        this(parent);
        this.bankId = bankId;
    }

    @Override
    protected void onPreExecute() {
        getDialog().setMessage(res.getText(R.string.updating_account_balance)
                + "\n ");
        getDialog().setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        getDialog().setCancelable(false);
        getDialog().show();
    }

    @NonNull
    protected ProgressDialog getDialog() {
        return this.dialog;
    }

    protected Bank getBankFromDb(long bankId, Context parent) {
        return BankFactory.bankFromDb(bankId, parent, true);
    }

    protected List<Bank> getBanksFromDb(Context parent) {
        return BankFactory.banksFromDb(parent, true);
    }

    protected void saveBank(Bank bank, Context context) {
        DBAdapter.save(bank, parent);
    }

    protected void publishProgress(int zeroBasedBankNumber, @Nullable Bank bank) {
        String text = "";
        if (bank != null) {
            text = bank.getName() + " (" + bank.getUsername() + ")";
        }
        publishProgress(Integer.toString(zeroBasedBankNumber), text);
    }

    protected boolean isContentProviderEnabled() {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(parent);
        return prefs.getBoolean("content_provider_enabled", false);
    }

    @Override
    protected Void doInBackground(final String... args) {
        errors = new ArrayList<>();
        List<Bank> banks;
        if (bankId != -1) {
            banks = new ArrayList<>();
            banks.add(getBankFromDb(bankId, parent));
        } else {
            banks = getBanksFromDb(parent);
        }
        getDialog().setMax(banks.size());
        int i = 0;
        for (final Bank bank : banks) {
            publishProgress(i, bank);

            if (isListingAllBanks() && bank.isDisabled()) {
                continue;
            }

            try {
                bank.update();
                bank.updateAllTransactions();
                bank.closeConnection();
                saveBank(bank, parent);
                i++;
            } catch (final BankException e) {
                this.errors.add(bank.getName() + " (" + bank.getUsername()
                        + ")");

                Timber.e(e, "Could not update bank.");
            } catch (final LoginException e) {
                this.errors.add(bank.getName() + " (" + bank.getUsername()
                        + ")");
                DBAdapter.disable(bank, parent);
            } catch (BankChoiceException e) {
                this.errors.add(bank.getName() + " (" + bank.getUsername()
                        + ")");
            } catch (IOException e) {
                this.errors.add(bank.getName() + " (" + bank.getUsername()
                        + ")");
                if (NetworkUtils.isInternetAvailable()) {
                    Timber.e(e);
                }
            }

            if (isContentProviderEnabled()) {
                final ArrayList<Account> accounts = bank.getAccounts();
                for (final Account account : accounts) {
                    AutoRefreshService.broadcastTransactionUpdate(parent,
                            bank.getDbId(), account.getId());
                }
            }
        }
        publishProgress(i, null);
        return null;
    }

    private boolean isListingAllBanks() {
        return bankId == -1;
    }

    @Override
    protected void onProgressUpdate(final String... args) {
        getDialog().setProgress(Integer.parseInt(args[0]));
        getDialog().setMessage(res.getText(R.string.updating_account_balance)
                + "\n" + args[1]);
    }

    @Override
    protected void onPostExecute(final Void unused) {
        parent.refreshView();
        AutoRefreshService.sendWidgetRefresh(parent);
        ActivityHelper.dismissDialog(getDialog());

        if ((this.errors != null) && !this.errors.isEmpty()) {
            final StringBuilder errormsg = new StringBuilder();
            errormsg.append(res.getText(R.string.accounts_were_not_updated))
                    .append(":\n");
            for (final String err : errors) {
                errormsg.append(err);
                errormsg.append("\n");
            }
            final AlertDialog.Builder builder = new AlertDialog.Builder(parent);
            builder.setMessage(errormsg.toString())
                    .setTitle(res.getText(R.string.errors_when_updating))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setNeutralButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        final DialogInterface dialog,
                                        final int id) {
                                    dialog.cancel();
                                }
                            });
            final AlertDialog alert = builder.create();
            alert.show();
        }
    }
}
