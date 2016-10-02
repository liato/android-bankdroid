package com.liato.bankdroid.db;

import android.content.ContentValues;

import com.bankdroid.core.repository.TransactionEntity;

import org.joda.time.DateTime;

import java.math.BigDecimal;

public class TransactionEntityTransformer {

    public TransactionEntity.Builder transform(ContentValues values) {
        return TransactionEntity
                .builder()
                .id(values.getAsString(Database.TRANSACTION_ID))
                .description(values.getAsString(Database.TRANSACTION_DESCRIPTION))
                .amount(new BigDecimal(values.getAsString(Database.TRANSACTION_AMOUNT)))
                .currency(values.getAsString(Database.TRANSACTION_CURRENCY))
                .date(new DateTime(values.getAsLong(Database.TRANSACTION_DATE)))
                .pending(values.getAsInteger(Database.TRANSACTION_PENDING) != 0);
    }

    public ContentValues transform(TransactionEntity transaction) {
        ContentValues values = new ContentValues();
        values.put(Database.TRANSACTION_ID, transaction.id());
        values.put(Database.TRANSACTION_DESCRIPTION, transaction.description());
        values.put(Database.TRANSACTION_AMOUNT, transaction.amount().toPlainString());
        values.put(Database.TRANSACTION_CURRENCY, transaction.currency());
        values.put(Database.TRANSACTION_DATE, transaction.date().getMillis());
        values.put(Database.TRANSACTION_PENDING, transaction.pending() ? 1 : 0);
        return values;
    }
}
