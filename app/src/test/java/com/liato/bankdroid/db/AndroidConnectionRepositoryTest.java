package com.liato.bankdroid.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.bankdroid.core.repository.ConnectionEntity;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AndroidConnectionRepositoryTest {

    private static final String EXPECTED_PROPERTY_KEY = "irrelevant_property_key";
    private static final String EXPECTED_PROPERTY_VALUE = "irrelevant_property_value";
    private SQLiteDatabase db;

    private AndroidConnectionRepository underTest;

    @Before
    public void setUp() throws IOException {
        DatabaseHelper databaseHelper = DatabaseHelper.getHelper(RuntimeEnvironment.application);
        db = DatabaseTestHelper.emptyDatabase();
        databaseHelper.onCreate(db);
        databaseHelper.onConfigure(db);
        underTest = new AndroidConnectionRepository(db);
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void deleting_a_connection_will_delete_it_from_the_db() {
        ContentValues expected = Fixtures.createValidConnection();
        db.insertOrThrow(Database.CONNECTION_TABLE_NAME, null, expected);
        assertThat(rowCount(), is(1));

        underTest.delete(expected.getAsLong(Database.CONNECTION_ID));

        assertThat(rowCount(), is(0));
    }

    @Test
    public void finding_a_connection_by_id_will_return_a_connection_entity_when_match() {
        ContentValues expectedConnection = Fixtures.createValidConnection();
        db.insertOrThrow(Database.CONNECTION_TABLE_NAME, null, expectedConnection);
        db.insertOrThrow(Database.PROPERTY_TABLE_NAME, null,
                createValidProperty(expectedConnection.getAsLong(Database.CONNECTION_ID))
        );
        assertThat(rowCount(), is(1));

        ConnectionEntity actual = underTest.findById(expectedConnection.getAsLong(Database.CONNECTION_ID));
        assertThat(actual, is(notNullValue()));
        assertThat(actual.id(), is(expectedConnection.getAsLong(Database.CONNECTION_ID)));

        Map<String, String> actualProperties = actual.properties();
        assertThat(actualProperties.size(), is(1));
        assertThat(actualProperties.containsKey(EXPECTED_PROPERTY_KEY), is(true));
        assertThat(actualProperties.get(EXPECTED_PROPERTY_KEY), is(EXPECTED_PROPERTY_VALUE));

    }

    @Test
    public void saving_a_connection_will_persist_it_to_the_database() {
        fail();
    }

    private ContentValues createValidProperty(long expectedConnectionId) {
        ContentValues values = new ContentValues();
        values.put(Database.PROPERTY_CONNECTION_ID, expectedConnectionId);
        values.put(Database.PROPERTY_KEY, EXPECTED_PROPERTY_KEY);
        values.put(Database.PROPERTY_VALUE, EXPECTED_PROPERTY_VALUE);
        return values;
    }

    private int rowCount() {
        Cursor cursor = db.query(Database.CONNECTION_TABLE_NAME, null, null, null, null, null, null);
        return cursor.getCount();
    }
}
