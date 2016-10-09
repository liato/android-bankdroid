package com.liato.bankdroid.appwidget;

import com.liato.bankdroid.banking.Account;
import com.liato.bankdroid.banking.Bank;
import com.liato.bankdroid.db.DBAdapter;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DataRetrieverTaskTest {
    private static class TestableBank extends Bank {
        private final int balanceBeforeUpdate;
        private final int balanceAfterUpdate;
        private boolean hasUpdated = false;

        public TestableBank(int balanceBeforeUpdate, int balanceAfterUpdate) {
            super(Mockito.mock(Context.class), 0);

            this.balanceBeforeUpdate = balanceBeforeUpdate;
            this.balanceAfterUpdate = balanceAfterUpdate;
        }

        @Override
        public void update() {
            hasUpdated = true;
        }

        @Override
        public ArrayList<Account> getAccounts() {
            int balance;
            if (hasUpdated) {
                balance = balanceAfterUpdate;
            } else {
                balance = balanceBeforeUpdate;
            }

            Account account = Mockito.mock(Account.class);
            Mockito.when(account.getBalance()).thenReturn(BigDecimal.valueOf(balance));

            ArrayList<Account> accounts = new ArrayList<>();
            accounts.add(account);

            return accounts;
        }

        @Override
        public BigDecimal getBalance() {
            return getAccounts().get(0).getBalance();
        }
    }

    private static class TestableDataRetrieverTask extends AutoRefreshService.DataRetrieverTask {
        private final Bank bank;
        private boolean hasRefreshedWidget = false;

        private TestableDataRetrieverTask(
                AutoRefreshService autoRefreshService, SharedPreferences prefs, Bank bank) {
            super(autoRefreshService, prefs);

            this.bank = bank;
        }

        @Override
        protected List<Bank> getBanks() {
            List<Bank> returnMe = new ArrayList<>();
            returnMe.add(bank);
            return returnMe;
        }

        @NonNull
        @Override
        protected DBAdapter getDBAdapter() {
            return Mockito.mock(DBAdapter.class);
        }

        @Override
        protected void sendWidgetRefresh() {
            hasRefreshedWidget = true;
        }
    }

    @Test
    public void testIncreaseLessThanNotificationThreshold() throws Exception {
        AutoRefreshService autoRefreshService = Mockito.mock(AutoRefreshService.class);

        SharedPreferences prefs = Mockito.mock(SharedPreferences.class);
        Mockito.when(prefs.getString("notify_min_delta", "0")).thenReturn("300");

        TestableDataRetrieverTask testMe =
                new TestableDataRetrieverTask(autoRefreshService, prefs, new TestableBank(100, 200));
        testMe.doInBackground();

        Assert.assertTrue("Widget should have been refreshed", testMe.hasRefreshedWidget);
    }

    @Test
    public void testNoChange() throws Exception {
        AutoRefreshService autoRefreshService = Mockito.mock(AutoRefreshService.class);

        SharedPreferences prefs = Mockito.mock(SharedPreferences.class);
        Mockito.when(prefs.getString("notify_min_delta", "0")).thenReturn("0");

        TestableDataRetrieverTask testMe =
                new TestableDataRetrieverTask(autoRefreshService, prefs, new TestableBank(100, 100));
        testMe.doInBackground();

        Assert.assertFalse("Widget shouldn't have been refreshed", testMe.hasRefreshedWidget);
    }
}
