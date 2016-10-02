package com.liato.bankdroid;

import com.liato.bankdroid.banking.Bank;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

public class DataRetrieverTaskTest {
    // Any positive number would do here
    private static final int BANK_ID = 5;

    private static class TestableDataRetrieverTask extends DataRetrieverTask {
        private final Bank[] banks;
        private final ProgressDialog dialog;

        @Override
        protected void publishProgress(int zeroBasedBankNumber, Bank bank) {
            // This method intentionally left blank
        }

        @Override
        protected Bank getBankFromDb(long bankId, Context parent) {
            Assert.assertEquals(BANK_ID, bankId);
            return banks[0];
        }

        @Override
        protected List<Bank> getBanksFromDb(Context parent) {
            return Arrays.asList(banks);
        }

        @Override
        protected void saveBank(Bank bank, Context context) {
            // This method intentionally left blank
        }

        @Override
        protected boolean isContentProviderEnabled() {
            return false;
        }

        @NonNull
        @Override
        protected ProgressDialog getDialog() {
            return dialog;
        }

        /**
         * Constructor for testing a specific bank
         */
        public TestableDataRetrieverTask(Bank bank) {
            super(Mockito.mock(MainActivity.class), BANK_ID);

            this.banks = new Bank[] { bank };
            this.dialog = Mockito.mock(ProgressDialog.class);
        }

        /**
         * Constructor for testing all banks
         */
        public TestableDataRetrieverTask(Bank[] allBanks) {
            super(Mockito.mock(MainActivity.class));

            this.banks = allBanks;
            this.dialog = Mockito.mock(ProgressDialog.class);
        }
    }

    @Test
    public void testUpdateSingleDisabledBank() throws Exception {
        Bank bank = Mockito.mock(Bank.class);
        Mockito.when(bank.isDisabled()).thenReturn(true);

        TestableDataRetrieverTask testMe = new TestableDataRetrieverTask(bank);
        testMe.doInBackground();

        // Single disabled bank should be updated
        Mockito.verify(bank, Mockito.atLeastOnce()).update();

        // At least at the time of writing this (2016oct2) updating the bank
        // implicitly enables it. Of course having a test for that would be
        // better than not having one, but we don't right now.
    }

    @Test
    public void testUpdateSingleEnabledBank() throws Exception {
        Bank bank = Mockito.mock(Bank.class);
        Mockito.when(bank.isDisabled()).thenReturn(false);

        TestableDataRetrieverTask testMe = new TestableDataRetrieverTask(bank);
        testMe.doInBackground();

        // Single enabled bank should be updated
        Mockito.verify(bank, Mockito.atLeastOnce()).update();
    }

    @Test
    public void testUpdateMultiDisabledBank() throws Exception {
        Bank bank = Mockito.mock(Bank.class);
        Mockito.when(bank.isDisabled()).thenReturn(true);

        TestableDataRetrieverTask testMe = new TestableDataRetrieverTask(new Bank[]{bank});
        testMe.doInBackground();

        // When doing all banks, disabled ones shouldn't update
        Mockito.verify(bank, Mockito.never()).update();
    }

    @Test
    public void testUpdateMultiEnabledBank() throws Exception {
        Bank bank = Mockito.mock(Bank.class);
        Mockito.when(bank.isDisabled()).thenReturn(false);

        TestableDataRetrieverTask testMe = new TestableDataRetrieverTask(new Bank[]{bank});
        testMe.doInBackground();

        // When doing all banks, enabled ones should update
        Mockito.verify(bank, Mockito.atLeastOnce()).update();
    }
}
