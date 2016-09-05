package com.liato.bankdroid.db;

import com.liato.bankdroid.banking.LegacyBankHelper;
import com.liato.bankdroid.provider.IAccountTypes;
import com.liato.bankdroid.provider.IBankTypes;

import android.content.ContentValues;

import java.math.BigDecimal;

final class LegacyFixtures {

    static final long LEGACY_BANK_ID = 1;
    static final String LEGACY_BANK_CUSTOM_NAME = "not_relevant_name";
    static final int LEGACY_BANK_TYPE = IBankTypes.TESTBANK;
    static final int LEGACY_BANK_DISABLED = 1;
    static final String LEGACY_BANK_UPDATED = "not_relevant_update_timestamp";
    static final int LEGACY_BANK_SORT_ORDER = 3;
    static final String LEGACY_BANK_BALANCE = new BigDecimal(10).toPlainString();

    static final String LEGACY_ACCOUNT_ID = "not_relevant_account_id";

    static final String LEGACY_DB_ACCOUNT_ID = LegacyBankHelper
            .legacyAccountIdOf(LegacyFixtures.LEGACY_BANK_ID, LEGACY_ACCOUNT_ID);

    static final int LEGACY_ACCOUNT_HIDDEN = 1;

    static final String LEGACY_ACCOUNT_BALANCE = new BigDecimal(5).toPlainString();

    static final String LEGACY_ACCOUNT_CURRENCY = "irrelevant_currency";

    static final String LEGACY_ACCOUNT_NAME = "irrelevant_account_name";

    static final int LEGACY_ACCOUNT_TYPE = IAccountTypes.REGULAR;

    static final String LEGACY_ACCOUNT_NOTIFY = "irrelevant_notification";
    static final String LEGACY_BANK_USERNAME = "irrelevant_username";
    static final String LEGACY_BANK_PASSWORD = "irrelevant_password";
    static final String LEGACY_BANK_EXTRAS = "irrelevant_extras";

    private LegacyFixtures() {
    }

    static ContentValues legacyBank() {
        ContentValues values = new ContentValues();
        values.put(LegacyDatabase.BANK_ID, LEGACY_BANK_ID);
        values.put(LegacyDatabase.BANK_CUSTOM_NAME, LEGACY_BANK_CUSTOM_NAME);
        values.put(LegacyDatabase.BANK_TYPE, LEGACY_BANK_TYPE);
        values.put(LegacyDatabase.BANK_DISABLED, LEGACY_BANK_DISABLED);
        values.put(LegacyDatabase.BANK_UPDATED, LEGACY_BANK_UPDATED);
        values.put(LegacyDatabase.BANK_SORT_ORDER, LEGACY_BANK_SORT_ORDER);
        values.put(LegacyDatabase.BANK_BALANCE, LEGACY_BANK_BALANCE);
        return values;
    }

    static ContentValues version11Bank() {
        ContentValues values = new ContentValues();
        values.put(LegacyDatabase.BANK_ID, LEGACY_BANK_ID);
        values.put(LegacyDatabase.BANK_CUSTOM_NAME, LEGACY_BANK_CUSTOM_NAME);
        values.put(LegacyDatabase.BANK_TYPE, LEGACY_BANK_TYPE);
        values.put(LegacyDatabase.BANK_DISABLED, LEGACY_BANK_DISABLED);
        values.put(LegacyDatabase.BANK_UPDATED, LEGACY_BANK_UPDATED);
        values.put(LegacyDatabase.BANK_SORT_ORDER, LEGACY_BANK_SORT_ORDER);
        values.put(LegacyDatabase.BANK_BALANCE, LEGACY_BANK_BALANCE);
        values.put(LegacyDatabase.BANK_USERNAME, LEGACY_BANK_USERNAME);
        values.put(LegacyDatabase.BANK_PASSWORD, LEGACY_BANK_PASSWORD);
        values.put(LegacyDatabase.BANK_EXTRAS, LEGACY_BANK_EXTRAS);
        return values;
    }

    static ContentValues legacyAccount() {
        ContentValues values = new ContentValues();
        values.put(LegacyDatabase.ACCOUNT_ID, LEGACY_DB_ACCOUNT_ID);
        values.put(LegacyDatabase.ACCOUNT_BANK_ID, LegacyFixtures.LEGACY_BANK_ID);
        values.put(LegacyDatabase.ACCOUNT_HIDDEN, LEGACY_ACCOUNT_HIDDEN);
        values.put(LegacyDatabase.ACCOUNT_BALANCE, LEGACY_ACCOUNT_BALANCE);
        values.put(LegacyDatabase.ACCOUNT_CURRENCY, LEGACY_ACCOUNT_CURRENCY);
        values.put(LegacyDatabase.ACCOUNT_NAME, LEGACY_ACCOUNT_NAME);
        values.put(LegacyDatabase.ACCOUNT_TYPE, LEGACY_ACCOUNT_TYPE);
        values.put(LegacyDatabase.ACCOUNT_NOTIFY, LEGACY_ACCOUNT_NOTIFY);
        return values;
    }

}
