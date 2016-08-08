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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;
import java.math.BigDecimal;

import static com.liato.bankdroid.db.Database.ACCOUNTS_TABLE_NAME;
import static com.liato.bankdroid.db.Database.CONNECTION_TABLE_NAME;
import static com.liato.bankdroid.db.DatabaseTestHelper.withDatabaseVersion;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AccountsTableCreationTest {

    private static final String LEGACY_ACCOUNT_ID = "not_relevant_account_id";

    private static final String LEGACY_DB_ACCOUNT_ID = LegacyBankHelper.legacyAccountIdOf(LegacyFixtures.LEGACY_BANK_ID, LEGACY_ACCOUNT_ID);

    private static final int LEGACY_ACCOUNT_HIDDEN = 1;

    private static final String LEGACY_ACCOUNT_BALANCE = new BigDecimal(5).toPlainString();

    private static final String LEGACY_ACCOUNT_CURRENCY = "irrelevant_currency";

    private static final String LEGACY_ACCOUNT_NAME = "irrelevant_account_name";

    private static final int LEGACY_ACCOUNT_TYPE = IAccountTypes.REGULAR;

    private static final String LEGACY_ACCOUNT_NOTIFY = "irrelevant_notification";

    private static final String VALID_ACCOUNT_TYPE = LegacyBankHelper.fromLegacyAccountType(LEGACY_ACCOUNT_TYPE);

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
        prepareDatabase(withDatabaseVersion(12));
        addLegacyAccount();

        underTest.onUpgrade(db, 12, 13);

        Cursor actual = db.query(Database.ACCOUNTS_TABLE_NAME, null, null, null, null, null, null);
        assertThat(actual.getCount(), is(1));

        actual.moveToFirst();
        assertThat(actual.getString(actual.getColumnIndex(Database.ACCOUNT_ID)), is(LEGACY_ACCOUNT_ID));
        assertThat(actual.getString(actual.getColumnIndex(Database.ACCOUNT_BALANCE)), is(LEGACY_ACCOUNT_BALANCE));
        assertThat(actual.getString(actual.getColumnIndex(Database.ACCOUNT_CURRENCY)), is(LEGACY_ACCOUNT_CURRENCY));
        assertThat(actual.getString(actual.getColumnIndex(Database.ACCOUNT_NAME)), is(LEGACY_ACCOUNT_NAME));
        assertThat(actual.getInt(actual.getColumnIndex(Database.ACCOUNT_HIDDEN)), is(LEGACY_ACCOUNT_HIDDEN));
        assertThat(actual.getString(actual.getColumnIndex(Database.ACCOUNT_TYPE)), is(VALID_ACCOUNT_TYPE));
    }

    private void addLegacyAccount() {
        db.insert(LegacyDatabase.BANK_TABLE_NAME, null, LegacyFixtures.legacyBank());

        ContentValues legacyAccount = new ContentValues();
        legacyAccount.put(LegacyDatabase.ACCOUNT_ID, LEGACY_DB_ACCOUNT_ID);
        legacyAccount.put(LegacyDatabase.ACCOUNT_BANK_ID, LegacyFixtures.LEGACY_BANK_ID);
        legacyAccount.put(LegacyDatabase.ACCOUNT_HIDDEN, LEGACY_ACCOUNT_HIDDEN);
        legacyAccount.put(LegacyDatabase.ACCOUNT_BALANCE, LEGACY_ACCOUNT_BALANCE);
        legacyAccount.put(LegacyDatabase.ACCOUNT_CURRENCY, LEGACY_ACCOUNT_CURRENCY);
        legacyAccount.put(LegacyDatabase.ACCOUNT_NAME, LEGACY_ACCOUNT_NAME);
        legacyAccount.put(LegacyDatabase.ACCOUNT_TYPE, LEGACY_ACCOUNT_TYPE);
        legacyAccount.put(LegacyDatabase.ACCOUNT_NOTIFY, LEGACY_ACCOUNT_NOTIFY);
        db.insert(LegacyDatabase.ACCOUNT_TABLE_NAME, null, legacyAccount);
    }

    private void prepareDatabase(DatabaseTestHelper dbTestHelper) {
        this.dbTestHelper = dbTestHelper;
        this.db = dbTestHelper.db();
    }
}
