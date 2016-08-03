package com.liato.bankdroid.db;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.io.IOException;

import static com.liato.bankdroid.db.Database.CONNECTION_TABLE_NAME;
import static com.liato.bankdroid.db.Database.DATABASE_VERSION;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ConnectionTableCreationTest {

    private DatabaseHelper underTest;
    private SQLiteDatabase db;

    @Before
    public void setUp() throws IOException {
        underTest = DatabaseHelper.getHelper(RuntimeEnvironment.application);
        db = SQLiteDatabase.openOrCreateDatabase(File.createTempFile("test", ".db"), null);
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void connection_table_is_created_when_onCreate_is_called() {
        underTest.onCreate(db);
        assertThat("Connection table has not been created",
                tableExists(CONNECTION_TABLE_NAME),
                is(true));
    }

    @Test
    public void connection_table_is_created_on_db_upgrades_where_old_version_is_12() {
        underTest.onUpgrade(db, 12, DATABASE_VERSION);
        assertThat("Connection table has not been created",
                tableExists(CONNECTION_TABLE_NAME),
                is(true));
    }

    private boolean tableExists(String tableName) {
        return db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?;",
                new String[]{tableName})
                .getCount() == 1;
    }
}
