package com.liato.bankdroid.db;


import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import com.liato.bankdroid.db.matchers.ForeignKeyConstraintExceptionMatcher;
import com.liato.bankdroid.db.matchers.NullConstraintExceptionMatcher;
import com.liato.bankdroid.db.matchers.UniqueConstraintExceptionMatcher;

import java.io.IOException;

import static com.liato.bankdroid.db.Database.CONNECTION_ID;
import static com.liato.bankdroid.db.Database.CONNECTION_TABLE_NAME;
import static com.liato.bankdroid.db.Database.PROPERTY_CONNECTION_ID;
import static com.liato.bankdroid.db.Database.PROPERTY_KEY;
import static com.liato.bankdroid.db.Database.PROPERTY_TABLE_NAME;
import static com.liato.bankdroid.db.Database.PROPERTY_VALUE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ConnectionPropertiesTableConstraintsTest {

    private static final String VALID_PROPERTY_KEY = "irrelevant_property_key";
    private static final String VALID_PROPERTY_VALUE = "irrelevant_property_value";
    private static final long NON_EXISTING_CONNECTION_ID = 12345;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private SQLiteDatabase db;

    @Before
    public void setUp() throws IOException {
        DatabaseHelper databaseHelper = DatabaseHelper.getHelper(RuntimeEnvironment.application);
        db = DatabaseTestHelper.emptyDatabase();
        databaseHelper.onCreate(db);
        databaseHelper.onConfigure(db);

        db.insertOrThrow(Database.CONNECTION_TABLE_NAME, null, Fixtures.createValidConnection());
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void adding_a_valid_connection_property_saves_the_data_to_the_database() {
        db.insertOrThrow(Database.PROPERTY_TABLE_NAME, null, createValidProperty());

        Cursor actual = db.query(Database.PROPERTY_TABLE_NAME, null, null, null, null, null, null);

        assertThat("No connection properties found in database", actual.moveToFirst(), is(true));
        assertThat("Invalid number of properties found", actual.getCount(), is(1));

        assertThat("Invalid connection id", actual.getLong(actual.getColumnIndex(PROPERTY_CONNECTION_ID)), is(Fixtures.VALID_CONNECTION_ID));
        assertThat("Invalid property key", actual.getString(actual.getColumnIndex(PROPERTY_KEY)), is(VALID_PROPERTY_KEY));
        assertThat("Invalid property value", actual.getString(actual.getColumnIndex(PROPERTY_VALUE)), is(VALID_PROPERTY_VALUE));
    }

    @Test
    public void adding_a_connection_property_without_an_already_existing_connection_is_prohibited() {
        ContentValues property = createValidProperty();
        property.put(PROPERTY_CONNECTION_ID, NON_EXISTING_CONNECTION_ID);

        exception.expect(new ForeignKeyConstraintExceptionMatcher());

        db.insertOrThrow(PROPERTY_TABLE_NAME, null, property);
    }

    @Test
    public void adding_a_connection_property_with_connection_id_set_to_null_is_prohibited() {
        ContentValues property = createValidProperty();
        property.put(PROPERTY_CONNECTION_ID, (String) null);

        exception.expect(new NullConstraintExceptionMatcher(PROPERTY_TABLE_NAME, PROPERTY_CONNECTION_ID));

        db.insertOrThrow(PROPERTY_TABLE_NAME, null, property);
    }

    @Test
    public void adding_a_connection_property_with_key_set_to_null_is_prohibited() {
        ContentValues property = createValidProperty();
        property.put(PROPERTY_KEY, (String) null);

        exception.expect(new NullConstraintExceptionMatcher(PROPERTY_TABLE_NAME, PROPERTY_KEY));

        db.insertOrThrow(PROPERTY_TABLE_NAME, null, property);
    }

    @Test
    public void adding_a_connection_property_with_an_already_existing_combination_of_connection_id_and_property_key_is_prohibited() {
        db.insertOrThrow(PROPERTY_TABLE_NAME, null, createValidProperty());
        Cursor propertyCursor = db.query(PROPERTY_TABLE_NAME, null, null, null, null, null, null);
        assertThat("Property not saved in database", propertyCursor.moveToFirst(), is(true));
        assertThat("Wrong number of properties found in database", propertyCursor.getCount(), is(1));
        propertyCursor.close();

        exception.expect(SQLiteConstraintException.class);
        exception.expect(new UniqueConstraintExceptionMatcher(PROPERTY_CONNECTION_ID, PROPERTY_KEY));

        db.insertOrThrow(PROPERTY_TABLE_NAME, null, createValidProperty());
    }

    @Test
    public void removing_a_connection_will_remove_related_connection_properties() {
        db.insertOrThrow(PROPERTY_TABLE_NAME, null, createValidProperty());
        Cursor propertyCursor = db.query(PROPERTY_TABLE_NAME, null, null, null, null, null, null);

        assertThat("Property not saved in database", propertyCursor.moveToFirst(), is(true));
        assertThat("Wrong number of properties found in database", propertyCursor.getCount(), is(1));
        propertyCursor.close();

        db.delete(CONNECTION_TABLE_NAME, CONNECTION_ID + "= ?", new String[]{Long.toString(Fixtures.VALID_CONNECTION_ID)});

        Cursor actual = db.query(PROPERTY_TABLE_NAME, null, null, null, null, null, null);
        assertThat("Property not deleted", actual.moveToFirst(), is(false));
        assertThat("Wrong number of properties found in database", actual.getCount(), is(0));
    }

    private ContentValues createValidProperty() {
        ContentValues values = new ContentValues();
        values.put(Database.PROPERTY_CONNECTION_ID, Fixtures.VALID_CONNECTION_ID);
        values.put(Database.PROPERTY_KEY, VALID_PROPERTY_KEY);
        values.put(Database.PROPERTY_VALUE, VALID_PROPERTY_VALUE);
        return values;
    }
}
