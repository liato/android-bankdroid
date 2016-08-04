package com.liato.bankdroid.db;

import com.liato.bankdroid.banking.LegacyBankHelper;
import com.liato.bankdroid.provider.IBankTypes;

import org.apache.commons.io.FileUtils;
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

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

import static android.database.sqlite.SQLiteDatabase.OPEN_READWRITE;
import static com.liato.bankdroid.db.Database.CONNECTION_PROVIDER_ID;
import static com.liato.bankdroid.db.Database.CONNECTION_TABLE_NAME;
import static com.liato.bankdroid.db.Database.DATABASE_VERSION;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ConnectionTableCreationTest {

    private static final long BANK_ID = 1;

    private static final String BANK_CUSTOM_NAME = "not_relevant_name";

    private static final int BANK_TYPE = IBankTypes.TESTBANK;

    private static final String PROVIDER_ID = LegacyBankHelper.getReferenceFromLegacyId(BANK_TYPE);

    private static final int BANK_DISABLED = 1;

    private static final String BANK_UPDATED = "not_relevant_update_timestamp";

    private static final int BANK_SORT_ORDER = 3;

    private static final String BANK_BALANCE = new BigDecimal(10).toPlainString();

    private static final int DISABLED = 0;

    private static final int INVALID_BANK_TYPE = -1;

    private static final String EMPTY_v12_DB = "db/empty_v12.db";

    private DatabaseHelper underTest;
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
    public void connection_table_is_created_when_onCreate_is_called() throws IOException {
        db = createEmptyTestDatabase();

        underTest.onCreate(db);
        assertThat("Connection table has not been created",
                tableExists(CONNECTION_TABLE_NAME),
                is(true));
    }

    @Test
    public void connection_table_is_created_on_db_upgrades_where_old_version_is_12()
            throws IOException {
        db = createTestDatabaseFrom(EMPTY_v12_DB);

        underTest.onUpgrade(db, 12, DATABASE_VERSION);
        assertThat("Connection table has not been created",
                tableExists(CONNECTION_TABLE_NAME),
                is(true));
    }

    @Test
    public void a_populated_bank_table_from_db_versions_less_than_12_is_migrated_to_the_connection_table()
            throws IOException {
        db = createTestDatabaseFrom(EMPTY_v12_DB);
        db.insertOrThrow(LegacyDatabase.BANK_TABLE_NAME, null, legacyBank());

        underTest.onUpgrade(db, 12, 13);

        assertThat(tableExists(LegacyDatabase.BANK_TABLE_NAME), is(false));

        Cursor actual = db.query(Database.CONNECTION_TABLE_NAME, null, null, null, null, null, null);
        assertThat(actual.getCount(), is(1));

        actual.moveToFirst();
        assertThat(actual.getLong(actual.getColumnIndex(Database.CONNECTION_ID)), is(BANK_ID));
        assertThat(actual.getString(actual.getColumnIndex(Database.CONNECTION_NAME)), is(BANK_CUSTOM_NAME));
        assertThat(actual.getString(actual.getColumnIndex(CONNECTION_PROVIDER_ID)), is(PROVIDER_ID));
        assertThat(actual.getInt(actual.getColumnIndex(Database.CONNECTION_ENABLED)), is(DISABLED));
        assertThat(actual.getString(actual.getColumnIndex(Database.CONNECTION_LAST_UPDATED)), is(BANK_UPDATED));
        assertThat(actual.getInt(actual.getColumnIndex(Database.CONNECTION_SORT_ORDER)), is(BANK_SORT_ORDER));
    }

    @Test
    public void a_bank_that_is_not_available_anymore_is_ignored_during_migration_to_v13()
            throws IOException {
        db = createTestDatabaseFrom(EMPTY_v12_DB);
        ContentValues legacyBankWithInvalidBankType = legacyBank();
        legacyBankWithInvalidBankType.put(LegacyDatabase.BANK_TYPE, INVALID_BANK_TYPE);
        db.insertOrThrow(LegacyDatabase.BANK_TABLE_NAME, null, legacyBankWithInvalidBankType);

        underTest.onUpgrade(db, 12, 13);

        Cursor actual = db.query(Database.CONNECTION_TABLE_NAME, null, null, null, null, null, null);
        assertThat(actual.getCount(), is(0));

    }

    private ContentValues legacyBank() {
        ContentValues values = new ContentValues();
        values.put(LegacyDatabase.BANK_ID, BANK_ID);
        values.put(LegacyDatabase.BANK_CUSTOM_NAME, BANK_CUSTOM_NAME);
        values.put(LegacyDatabase.BANK_TYPE, BANK_TYPE);
        values.put(LegacyDatabase.BANK_DISABLED, BANK_DISABLED);
        values.put(LegacyDatabase.BANK_UPDATED, BANK_UPDATED);
        values.put(LegacyDatabase.BANK_SORT_ORDER, BANK_SORT_ORDER);
        values.put(LegacyDatabase.BANK_BALANCE, BANK_BALANCE);
        return values;
    }

    private boolean tableExists(String tableName) {
        return db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?;",
                new String[]{tableName})
                .getCount() == 1;
    }

    private SQLiteDatabase createTestDatabaseFrom(String source) throws IOException {
        File testDb = File.createTempFile("test",".db");
        if(source != null) {
            FileUtils.copyURLToFile(getClass().getClassLoader().getResource(source),
                    testDb);
        }

        return SQLiteDatabase.openDatabase(testDb.getCanonicalPath(), null, OPEN_READWRITE);
    }

    private SQLiteDatabase createEmptyTestDatabase() throws IOException {
        return createTestDatabaseFrom(null);
    }
}
