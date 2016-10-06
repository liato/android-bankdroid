package com.liato.bankdroid.db;

import android.content.ContentValues;

import com.bankdroid.core.repository.ConnectionEntity;

import org.joda.time.DateTime;

class ConnectionEntityTransformer {

    ConnectionEntity.Builder transform(ContentValues values) {
        return ConnectionEntity.builder()
                .id(values.getAsLong(Database.CONNECTION_ID))
                .enabled(values.getAsInteger(Database.CONNECTION_ENABLED) != 0)
                .lastUpdated(new DateTime(values.getAsLong(Database.CONNECTION_LAST_UPDATED)))
                .name(values.getAsString(Database.CONNECTION_NAME))
                .providerId(values.getAsString(Database.CONNECTION_PROVIDER_ID));
    }

    ContentValues transform(ConnectionEntity connection) {
        ContentValues values = new ContentValues();
        if(connection.id() != ConnectionEntity.DEFAULT_ID) {
            values.put(Database.CONNECTION_ID, connection.id());
        }
        values.put(Database.CONNECTION_PROVIDER_ID, connection.providerId());
        if(connection.lastUpdated() != null) {
            values.put(Database.CONNECTION_LAST_UPDATED, connection.lastUpdated().getMillis());
        }
        values.put(Database.CONNECTION_NAME, connection.name());
        values.put(Database.CONNECTION_ENABLED, connection.enabled() ? 1 : 0);
        return values;
    }
}
