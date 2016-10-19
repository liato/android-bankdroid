package com.liato.bankdroid.db;

import org.apache.commons.io.FileUtils;

import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.io.IOException;

import static android.database.sqlite.SQLiteDatabase.OPEN_READWRITE;

public class DatabaseTestHelper {

    private static final String EMPTY_v11_DB = "db/empty_v11.db";
    private static final String EMPTY_v12_DB = "db/empty_v12.db";

    private final SQLiteDatabase db;

    private DatabaseTestHelper(SQLiteDatabase db) {
        this.db = db;
    }

    SQLiteDatabase db() {
        return this.db;
    }

    boolean tableExists(String tableName) {
        return db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?;",
                new String[]{tableName})
                .getCount() == 1;
    }

    static DatabaseTestHelper withEmptyDatabase() throws IOException {
        return new DatabaseTestHelper(emptyDatabase());
    }

    static DatabaseTestHelper withDatabaseVersion(int version) throws IOException {
        return new DatabaseTestHelper(createTestDatabaseFrom(getPathForVersion(version)));
    }

    private static String getPathForVersion(int version) {
        if(version == 11) {
            return EMPTY_v11_DB;
        }
        else if(version == 12) {
            return EMPTY_v12_DB;
        }
        return null;
    }

    private static SQLiteDatabase createTestDatabaseFrom(String path) throws IOException {
        File testDb = File.createTempFile("test",".db");
        if(path != null) {
            FileUtils.copyURLToFile(Thread.currentThread().getContextClassLoader().getResource(path),
                    testDb);
        }
        return SQLiteDatabase.openDatabase(testDb.getCanonicalPath(), null, OPEN_READWRITE);
    }

    static SQLiteDatabase emptyDatabase() throws IOException {
        return createTestDatabaseFrom(null);
    }
}
