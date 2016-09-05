package com.liato.bankdroid.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.liato.bankdroid.banking.LegacyProviderConfiguration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.IOException;

import static com.liato.bankdroid.banking.LegacyProviderConfiguration.*;
import static com.liato.bankdroid.db.Database.PROPERTY_CONNECTION_ID;
import static com.liato.bankdroid.db.Database.PROPERTY_KEY;
import static com.liato.bankdroid.db.Database.PROPERTY_TABLE_NAME;
import static com.liato.bankdroid.db.Database.PROPERTY_VALUE;
import static com.liato.bankdroid.db.DatabaseTestHelper.withDatabaseVersion;
import static com.liato.bankdroid.db.DatabaseTestHelper.withEmptyDatabase;
import static com.liato.bankdroid.db.LegacyFixtures.LEGACY_BANK_EXTRAS;
import static com.liato.bankdroid.db.LegacyFixtures.LEGACY_BANK_ID;
import static com.liato.bankdroid.db.LegacyFixtures.LEGACY_BANK_PASSWORD;
import static com.liato.bankdroid.db.LegacyFixtures.LEGACY_BANK_USERNAME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ConnectionPropertiesTableCreationTest {

    private static final String VALID_PROPERTY_KEY = "not_relevant_property_key";
    private static final String VALID_PROPERTY_VALUE = "not_relevant_property_value";
    private SQLiteDatabase db;
    private DatabaseTestHelper dbTestHelper;
    private DatabaseHelper underTest;

    @Before
    public void setUp() {
        underTest = DatabaseHelper.getHelper(RuntimeEnvironment.application);
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void connection_properties_table_is_created_when_onCreate_is_called() throws IOException {
        prepareDatabase(withEmptyDatabase());

        underTest.onCreate(db);
        assertThat(dbTestHelper.tableExists(PROPERTY_TABLE_NAME),
                is(true));
    }

    @Test
    public void connection_properties_table_is_created_on_db_upgrades_where_old_version_is_11() throws IOException {
        prepareDatabase(withDatabaseVersion(11));

        underTest.onUpgrade(db, 11, 12);
        assertThat(dbTestHelper.tableExists(PROPERTY_TABLE_NAME),
                is(true));
    }

    @Test
    public void a_populated_bank_table_from_db_version_11_is_migrated_to_the_connection_properties_table_when_upgrading_to_version_12() throws IOException {
        prepareDatabase(withDatabaseVersion(11));
        db.insertOrThrow(LegacyDatabase.BANK_TABLE_NAME, null, LegacyFixtures.version11Bank());
        underTest.onUpgrade(db, 11, 12);

        Cursor actual = db.query(Database.PROPERTY_TABLE_NAME, null, null, null, null, null, null);
        assertThat(actual.getCount(), is(3));

        actual.moveToFirst();
        assertThat(actual.getLong(actual.getColumnIndex(PROPERTY_CONNECTION_ID)), is(LEGACY_BANK_ID));
        assertThat(actual.getString(actual.getColumnIndex(PROPERTY_KEY)), is(USERNAME));
        assertThat(actual.getString(actual.getColumnIndex(PROPERTY_VALUE)), is(LEGACY_BANK_USERNAME));

        actual.moveToNext();
        assertThat(actual.getLong(actual.getColumnIndex(PROPERTY_CONNECTION_ID)), is(LEGACY_BANK_ID));
        assertThat(actual.getString(actual.getColumnIndex(PROPERTY_KEY)), is(PASSWORD));
        assertThat(actual.getString(actual.getColumnIndex(PROPERTY_VALUE)), is(LEGACY_BANK_PASSWORD));

        actual.moveToNext();
        assertThat(actual.getLong(actual.getColumnIndex(PROPERTY_CONNECTION_ID)), is(LEGACY_BANK_ID));
        assertThat(actual.getString(actual.getColumnIndex(PROPERTY_KEY)), is(EXTRAS));
        assertThat(actual.getString(actual.getColumnIndex(PROPERTY_VALUE)), is(LEGACY_BANK_EXTRAS));

    }

    @Test
    public void  upgrading_db_to_version_13_will_update_connection_properties_connection_id_to_reference_connection_table() throws IOException {
        prepareDatabase(withDatabaseVersion(12));
        db.insertOrThrow(LegacyDatabase.BANK_TABLE_NAME, null, LegacyFixtures.legacyBank());
        db.insertOrThrow(Database.PROPERTY_TABLE_NAME, null, legacyProperty());
        underTest.onUpgrade(db, 12, 13);

        Cursor actual = db.query(Database.PROPERTY_TABLE_NAME, null, null, null, null, null, null);
        assertThat(actual.getCount(), is(1));

        actual.moveToFirst();
        assertThat(actual.getLong(actual.getColumnIndex(PROPERTY_CONNECTION_ID)), is(LEGACY_BANK_ID));
        assertThat(actual.getString(actual.getColumnIndex(PROPERTY_KEY)), is(VALID_PROPERTY_KEY));
        assertThat(actual.getString(actual.getColumnIndex(PROPERTY_VALUE)), is(VALID_PROPERTY_VALUE));
    }

    private ContentValues legacyProperty() {
        ContentValues values = new ContentValues();
        values.put(Database.PROPERTY_CONNECTION_ID, LegacyFixtures.LEGACY_BANK_ID);
        values.put(Database.PROPERTY_KEY, VALID_PROPERTY_KEY);
        values.put(Database.PROPERTY_VALUE, VALID_PROPERTY_VALUE);
        return values;
    }

    private void prepareDatabase(DatabaseTestHelper dbTestHelper) {
        this.dbTestHelper = dbTestHelper;
        this.db = dbTestHelper.db();
        underTest.onConfigure(db);
    }
}
