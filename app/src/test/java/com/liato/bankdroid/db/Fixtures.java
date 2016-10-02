package com.liato.bankdroid.db;

import android.content.ContentValues;

import org.joda.time.DateTime;

import java.math.BigDecimal;

import static com.liato.bankdroid.db.Database.ACCOUNT_BALANCE;
import static com.liato.bankdroid.db.Database.ACCOUNT_CONNECTION_ID;
import static com.liato.bankdroid.db.Database.ACCOUNT_CURRENCY;
import static com.liato.bankdroid.db.Database.ACCOUNT_HIDDEN;
import static com.liato.bankdroid.db.Database.ACCOUNT_ID;
import static com.liato.bankdroid.db.Database.ACCOUNT_NAME;
import static com.liato.bankdroid.db.Database.ACCOUNT_NOTIFICATIONS_ENABLED;
import static com.liato.bankdroid.db.Database.ACCOUNT_TYPE;
import static com.liato.bankdroid.db.Database.CONNECTION_ENABLED;
import static com.liato.bankdroid.db.Database.CONNECTION_ID;
import static com.liato.bankdroid.db.Database.CONNECTION_LAST_UPDATED;
import static com.liato.bankdroid.db.Database.CONNECTION_NAME;
import static com.liato.bankdroid.db.Database.CONNECTION_PROVIDER_ID;
import static com.liato.bankdroid.db.Database.CONNECTION_SORT_ORDER;

final class Fixtures {

    static final long VALID_CONNECTION_ID = 1;
    static final long VALID_CONNECTION_LAST_UPDATED = DateTime.now().getMillis();
    static final String VALID_CONNECTION_NAME = "not_important";
    static final String VALID_CONNECTION_PROVIDER_ID = "not_important";
    static final long VALID_CONNECTION_SORT_ORDER = 2;
    static final int VALID_CONNECTION_DISABLED = 0;

    static final String VALID_ACCOUNT_ID = "not_relevant_account_id";

    static final long VALID_ACCOUNT_CONNECTION_ID = VALID_CONNECTION_ID;

    static final String VALID_ACCOUNT_TYPE = "not_relevant_account_type";

    static final String VALID_ACCOUNT_NAME = "not_relevant_account_name";

    static final String VALID_ACCOUNT_BALANCE = new BigDecimal(12).toPlainString();

    static final String VALID_ACCOUNT_CURRENCY = "not_relevant_account_currency";

    static final int VALID_ACCOUNT_HIDDEN = 1;

    static final int VALID_ACCOUNT_NOTIFICATIONS = 0;

    private Fixtures() {
    }

    static ContentValues createValidConnection() {
        ContentValues values = new ContentValues();
        values.put(CONNECTION_ENABLED, VALID_CONNECTION_DISABLED);
        values.put(CONNECTION_ID, VALID_CONNECTION_ID);
        values.put(CONNECTION_LAST_UPDATED, VALID_CONNECTION_LAST_UPDATED);
        values.put(CONNECTION_NAME, VALID_CONNECTION_NAME);
        values.put(CONNECTION_PROVIDER_ID, VALID_CONNECTION_PROVIDER_ID);
        values.put(CONNECTION_SORT_ORDER, VALID_CONNECTION_SORT_ORDER);
        return values;
    }

    static ContentValues createValidAccount() {
        ContentValues values = new ContentValues();
        values.put(ACCOUNT_ID, VALID_ACCOUNT_ID);
        values.put(ACCOUNT_CONNECTION_ID, VALID_ACCOUNT_CONNECTION_ID);
        values.put(ACCOUNT_TYPE, VALID_ACCOUNT_TYPE);
        values.put(ACCOUNT_NAME,VALID_ACCOUNT_NAME);
        values.put(ACCOUNT_BALANCE,VALID_ACCOUNT_BALANCE);
        values.put(ACCOUNT_CURRENCY,VALID_ACCOUNT_CURRENCY);
        values.put(ACCOUNT_HIDDEN,VALID_ACCOUNT_HIDDEN);
        values.put(ACCOUNT_NOTIFICATIONS_ENABLED,VALID_ACCOUNT_NOTIFICATIONS);
        return values;
    }
}
