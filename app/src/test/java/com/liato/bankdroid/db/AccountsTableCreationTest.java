package com.liato.bankdroid.db;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;

import static com.liato.bankdroid.db.Database.ACCOUNTS_TABLE_NAME;
import static com.liato.bankdroid.db.Database.CONNECTION_TABLE_NAME;
import static com.liato.bankdroid.db.DatabaseTestHelper.withDatabaseVersion;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AccountsTableCreationTest {

    private DatabaseHelper underTest;

    private DatabaseTestHelper dbTestHelper;
    private SQLiteDatabase db;

    @Before
    public void setUp() throws IOException {
        underTest = DatabaseHelper.getHelper(RuntimeEnvironment.application);
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void accounts_table_is_created_when_onCreate_is_called() throws IOException {
        prepareDatabase(DatabaseTestHelper.withEmptyDatabase());

        underTest.onCreate(db);
        assertThat(dbTestHelper.tableExists(ACCOUNTS_TABLE_NAME),
                is(true));
    }

    @Test
    public void accounts_table_is_created_on_db_upgrades_where_old_version_is_12()
            throws IOException {
        prepareDatabase(withDatabaseVersion(12));

        underTest.onUpgrade(db, 12, 13);
        assertThat("Connection table has not been created",
                dbTestHelper.tableExists(CONNECTION_TABLE_NAME),
                is(true));
    }

    @Test
    public void a_populated_accounts_table_from_db_versions_less_than_12_is_migrated_to_the_v13_accounts_table()
            throws IOException {

    }

    private void prepareDatabase(DatabaseTestHelper dbTestHelper) {
        this.dbTestHelper = dbTestHelper;
        this.db = dbTestHelper.db();
    }
}
