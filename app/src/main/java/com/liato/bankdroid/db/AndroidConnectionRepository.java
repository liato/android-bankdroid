package com.liato.bankdroid.db;

import android.database.sqlite.SQLiteDatabase;

import com.bankdroid.core.repository.ConnectionEntity;
import com.bankdroid.core.repository.ConnectionRepository;

import java.util.Collection;

public class AndroidConnectionRepository implements ConnectionRepository {

    private final SQLiteDatabase db;

    public AndroidConnectionRepository(SQLiteDatabase db) {
        this.db = db;
    }

    @Override
    public long save(ConnectionEntity connection) {
        return 0;
    }

    @Override
    public ConnectionEntity findById(long connectionId) {
        return null;
    }

    @Override
    public Collection<ConnectionEntity> findAll() {
        return null;
    }

    @Override
    public void delete(long connectionId) {
        db.delete(Database.CONNECTION_TABLE_NAME, Database.CONNECTION_ID + " = ?",
                new String[]{Long.toString(connectionId)});
    }

    @Override
    public void disable(long connectionId) {

    }

    @Override
    public void toggleHiddenAccounts(long connectionId) {

    }
}
