package com.liato.bankdroid.db;

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

import static com.liato.bankdroid.db.Database.TRANSACTIONS_TABLE_NAME;
import static com.liato.bankdroid.db.Database.TRANSACTION_DATE;
import static com.liato.bankdroid.db.Database.TRANSACTION_DESCRIPTION;
import static com.liato.bankdroid.db.DatabaseTestHelper.withDatabaseVersion;
import static com.liato.bankdroid.db.DatabaseTestHelper.withEmptyDatabase;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class TransactionsTableCreationTest {

    private static final long LEGACY_TRANSACTION_ID = 42;

    private static final String LEGACY_TRANSACTION_AMOUNT = new BigDecimal(152).toPlainString();

    private static final String LEGACY_TRANSACTION_CURRENCY = "irrelevant_transaction_currency";

    private static final String LEGACY_TRANSACTION_DATE = "irrelevant_transaction_date";

    private static final String LEGACY_TRANSACTION_DESCRIPTION
            = "irrelevant_transaction_description";

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
    public void transactions_table_is_created_when_onCreate_is_called() throws IOException {
        prepareDatabase(withEmptyDatabase());

        underTest.onCreate(db);
        assertThat("Transactions table has not been created",
                dbTestHelper.tableExists(TRANSACTIONS_TABLE_NAME),
                is(true));
    }

    @Test
    public void transactions_table_is_created_on_db_upgrades_where_old_version_is_12()
            throws IOException {
        prepareDatabase(withDatabaseVersion(12));

        underTest.onUpgrade(db, 12, 13);
        assertThat("Transactions table has not been created",
                dbTestHelper.tableExists(TRANSACTIONS_TABLE_NAME),
                is(true));
    }

    @Test
    public void a_populated_transactions_table_from_v12_is_migrated_to_new_structure_when_upgrading_to_v13()
            throws IOException {
        prepareDatabase(withDatabaseVersion(12));
        addLegacyTransaction();
        underTest.onUpgrade(db, 12, 13);

        Cursor actual = db.query(Database.TRANSACTIONS_TABLE_NAME, null, null, null, null, null, null);
        assertThat("Transaction has not been migrated", actual.getCount(), is(1));

        actual.moveToFirst();

        assertThat("Invalid transaction id", actual.getLong(actual.getColumnIndex(Database.TRANSACTION_ID)), is(LEGACY_TRANSACTION_ID));
        assertThat("Invalid account id", actual.getString(actual.getColumnIndex(Database.TRANSACTION_ACCOUNT_ID)), is(LegacyFixtures.LEGACY_ACCOUNT_ID));
        assertThat("Invalid connection id", actual.getLong(actual.getColumnIndex(Database.TRANSACTION_CONNECTION_ID)), is(LegacyFixtures.LEGACY_BANK_ID));
        assertThat("Invalid amount", actual.getString(actual.getColumnIndex(Database.TRANSACTION_AMOUNT)), is(LEGACY_TRANSACTION_AMOUNT));
        assertThat("Invalid currency", actual.getString(actual.getColumnIndex(Database.TRANSACTION_CURRENCY)), is(LEGACY_TRANSACTION_CURRENCY));
        assertThat("Invalid transaction date", actual.getString(actual.getColumnIndex(TRANSACTION_DATE)), is(LEGACY_TRANSACTION_DATE));
        assertThat("Invalid description", actual.getString(actual.getColumnIndex(TRANSACTION_DESCRIPTION)), is(LEGACY_TRANSACTION_DESCRIPTION));
    }

    private void addLegacyTransaction() {
        db.insert(LegacyDatabase.BANK_TABLE_NAME, null, LegacyFixtures.legacyBank());
        db.insert(LegacyDatabase.ACCOUNT_TABLE_NAME, null, LegacyFixtures.legacyAccount());

        ContentValues legacyTransaction = new ContentValues();
        legacyTransaction.put(LegacyDatabase.TRANSACTION_ID, LEGACY_TRANSACTION_ID);
        legacyTransaction.put(LegacyDatabase.TRANSACTION_ACCOUNT_ID, LegacyFixtures.LEGACY_DB_ACCOUNT_ID);
        legacyTransaction.put(LegacyDatabase.TRANSACTION_AMOUNT, LEGACY_TRANSACTION_AMOUNT);
        legacyTransaction.put(LegacyDatabase.TRANSACTION_CURRENCY, LEGACY_TRANSACTION_CURRENCY);
        legacyTransaction.put(LegacyDatabase.TRANSACTION_DATE, LEGACY_TRANSACTION_DATE);
        legacyTransaction.put(LegacyDatabase.TRANSACTION_DESCRIPTION, LEGACY_TRANSACTION_DESCRIPTION);
        db.insert(TRANSACTIONS_TABLE_NAME, null, legacyTransaction);
    }

    private void prepareDatabase(DatabaseTestHelper dbTestHelper) {
        this.dbTestHelper = dbTestHelper;
        this.db = dbTestHelper.db();
    }
}
