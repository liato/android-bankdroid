package com.liato.bankdroid.db;

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
}
