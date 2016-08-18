package com.liato.bankdroid.db;


import com.liato.bankdroid.banking.LegacyBankHelper;
import com.liato.bankdroid.provider.IAccountTypes;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;

import static com.liato.bankdroid.db.Database.ACCOUNTS_TABLE_NAME;
import static com.liato.bankdroid.db.DatabaseTestHelper.withDatabaseVersion;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AccountsTableCreationTest {

    private static final String VALID_ACCOUNT_TYPE = LegacyBankHelper.fromLegacyAccountType(LegacyFixtures.LEGACY_ACCOUNT_TYPE);

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
        assertThat("Accounts table has not been created",
                dbTestHelper.tableExists(ACCOUNTS_TABLE_NAME),
                is(true));
    }

    @Test
    public void a_populated_accounts_table_from_db_versions_less_than_12_is_migrated_to_the_v13_accounts_table()
            throws IOException {
        prepareDatabase(withDatabaseVersion(12));
        addLegacyAccount();

        underTest.onUpgrade(db, 12, 13);

        Cursor actual = db.query(Database.ACCOUNTS_TABLE_NAME, null, null, null, null, null, null);
        assertThat(actual.getCount(), is(1));

        actual.moveToFirst();
        assertThat(actual.getString(actual.getColumnIndex(Database.ACCOUNT_ID)), is(LegacyFixtures.LEGACY_ACCOUNT_ID));
        assertThat(actual.getString(actual.getColumnIndex(Database.ACCOUNT_BALANCE)), is(LegacyFixtures.LEGACY_ACCOUNT_BALANCE));
        assertThat(actual.getString(actual.getColumnIndex(Database.ACCOUNT_CURRENCY)), is(LegacyFixtures.LEGACY_ACCOUNT_CURRENCY));
        assertThat(actual.getString(actual.getColumnIndex(Database.ACCOUNT_NAME)), is(LegacyFixtures.LEGACY_ACCOUNT_NAME));
        assertThat(actual.getInt(actual.getColumnIndex(Database.ACCOUNT_HIDDEN)), is(LegacyFixtures.LEGACY_ACCOUNT_HIDDEN));
        assertThat(actual.getString(actual.getColumnIndex(Database.ACCOUNT_TYPE)), is(VALID_ACCOUNT_TYPE));
    }

    private void addLegacyAccount() {
        db.insert(LegacyDatabase.BANK_TABLE_NAME, null, LegacyFixtures.legacyBank());
        db.insert(LegacyDatabase.ACCOUNT_TABLE_NAME, null, LegacyFixtures.legacyAccount());
    }

    private void prepareDatabase(DatabaseTestHelper dbTestHelper) {
        this.dbTestHelper = dbTestHelper;
        this.db = dbTestHelper.db();
    }
}
