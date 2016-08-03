package com.liato.bankdroid.db;

import com.liato.bankdroid.db.matchers.NullConstraintExceptionMatcher;

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

import java.io.File;
import java.io.IOException;

import static com.liato.bankdroid.db.Database.CONNECTION_ENABLED;
import static com.liato.bankdroid.db.Database.CONNECTION_ID;
import static com.liato.bankdroid.db.Database.CONNECTION_LAST_UPDATED;
import static com.liato.bankdroid.db.Database.CONNECTION_NAME;
import static com.liato.bankdroid.db.Database.CONNECTION_PROVIDER_ID;
import static com.liato.bankdroid.db.Database.CONNECTION_SORT_ORDER;
import static com.liato.bankdroid.db.Database.CONNECTION_TABLE_NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ConnectionTableConstraintsTest {

    private static final long VALID_CONNECTION_ID = 1;

    private static final String VALID_LAST_UPDATED = "not_important";

    private static final String VALID_CONNECTION_NAME = "not_important";

    private static final String VALID_PROVIDER_ID = "not_important";

    private static final long VALID_SORT_ORDER = 2;

    private static final int ENABLED = 1;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private SQLiteDatabase db;

    @Before
    public void setUp() throws IOException {
        DatabaseHelper databaseHelper = DatabaseHelper.getHelper(RuntimeEnvironment.application);
        db = SQLiteDatabase.openOrCreateDatabase(File.createTempFile("test", ".db"), null);
        databaseHelper.onCreate(db);
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void adding_a_valid_connection_saves_the_data_to_the_database() {
        db.insertOrThrow(CONNECTION_TABLE_NAME, null, createValidConnection());
        Cursor actual = db.query(CONNECTION_TABLE_NAME, null, null, null, null, null, null);

        assertThat(actual.moveToFirst(), is(true));
        assertThat(actual.getCount(), is(1));

        assertThat(actual.getLong(actual.getColumnIndex(CONNECTION_ID)), is(VALID_CONNECTION_ID));
        assertThat(actual.getString(actual.getColumnIndex(CONNECTION_NAME)), is(VALID_CONNECTION_NAME));
        assertThat(actual.getString(actual.getColumnIndex(CONNECTION_PROVIDER_ID)), is(VALID_PROVIDER_ID));
        assertThat(actual.getString(actual.getColumnIndex(CONNECTION_LAST_UPDATED)), is(VALID_LAST_UPDATED));
        assertThat(actual.getLong(actual.getColumnIndex(CONNECTION_SORT_ORDER)), is(VALID_SORT_ORDER));
        assertThat(actual.getInt(actual.getColumnIndex(CONNECTION_ENABLED)), is(ENABLED));
    }

    @Test
    public void adding_a_connection_with_provider_id_set_to_null_is_prohibited() {
        ContentValues connection = createValidConnection();
        connection.remove(CONNECTION_PROVIDER_ID);

        exception.expect(SQLiteConstraintException.class);
        exception.expect(columnIsNull(CONNECTION_PROVIDER_ID));

        db.insertOrThrow(CONNECTION_TABLE_NAME, null, connection);
    }

    @Test
    public void adding_a_connection_with_name_set_to_null_is_prohibited() {
        ContentValues connection = createValidConnection();
        connection.remove(CONNECTION_NAME);

        exception.expect(SQLiteConstraintException.class);
        exception.expect(columnIsNull(CONNECTION_NAME));

        db.insertOrThrow(CONNECTION_TABLE_NAME, null, connection);
    }

    @Test
    public void adding_a_connection_without_specifying_if_it_is_enabled_defaults_to_true() {
        ContentValues connection = createValidConnection();
        connection.remove(CONNECTION_ENABLED);

        db.insertOrThrow(CONNECTION_TABLE_NAME, null, connection);
        Cursor actual = db.query(CONNECTION_TABLE_NAME, null, null, null, null, null, null);

        assertThat(actual.moveToFirst(), is(true));
        assertThat(actual.getCount(), is(1));
        assertThat(actual.getInt(actual.getColumnIndex(CONNECTION_ENABLED)), is(ENABLED));
    }

    @Test
    public void adding_a_connection_with_enabled_set_to_null_is_prohibited() {
        ContentValues connection = createValidConnection();
        connection.put(CONNECTION_ENABLED, (String) null);

        exception.expect(SQLiteConstraintException.class);
        exception.expect(columnIsNull(CONNECTION_ENABLED));

        db.insertOrThrow(CONNECTION_TABLE_NAME, null, connection);
    }

    private ContentValues createValidConnection() {
        ContentValues values = new ContentValues();
        values.put(CONNECTION_ENABLED, true);
        values.put(CONNECTION_ID, VALID_CONNECTION_ID);
        values.put(CONNECTION_LAST_UPDATED, VALID_LAST_UPDATED);
        values.put(CONNECTION_NAME, VALID_CONNECTION_NAME);
        values.put(CONNECTION_PROVIDER_ID, VALID_PROVIDER_ID);
        values.put(CONNECTION_SORT_ORDER, VALID_SORT_ORDER);
        return values;
    }

    private NullConstraintExceptionMatcher columnIsNull(String column) {
        return new NullConstraintExceptionMatcher(
                CONNECTION_TABLE_NAME,
                column);
    }
}
