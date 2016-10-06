package com.liato.bankdroid.db;

import android.content.ContentValues;

import com.bankdroid.core.repository.AccountEntity;
import com.liato.bankdroid.provider.IAccountTypes;
import com.liato.bankdroid.provider.IBankTypes;

import java.math.BigDecimal;

class AccountEntityTransformer {

    AccountEntity.Builder transform(ContentValues values) {
        return AccountEntity
                // connection id?
                .builder()
                .id(values.getAsString(Database.ACCOUNT_ID))
                .name(values.getAsString(Database.ACCOUNT_NAME))
                .balance(new BigDecimal(values.getAsString(Database.ACCOUNT_BALANCE)))
                .currency(values.getAsString(Database.ACCOUNT_CURRENCY))
                .hidden(values.getAsInteger(Database.ACCOUNT_HIDDEN) != 0)
                .notifications(values.getAsInteger(Database.ACCOUNT_NOTIFICATIONS_ENABLED) != 0)
                ;
    }

    ContentValues transform(AccountEntity account) {
        ContentValues values = new ContentValues();
        values.put(Database.ACCOUNT_ID, account.id());
        values.put(Database.ACCOUNT_NAME, account.name());
        values.put(Database.ACCOUNT_BALANCE, account.balance().toPlainString());
        values.put(Database.ACCOUNT_NOTIFICATIONS_ENABLED, account.notifications() ? 1 : 0);
        values.put(Database.ACCOUNT_HIDDEN, account.hidden() ? 1 : 0);
        values.put(Database.ACCOUNT_CURRENCY, account.currency());
        values.put(Database.ACCOUNT_TYPE, IAccountTypes.REGULAR);
        return values;
    }

}
