package com.liato.bankdroid.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AndroidConnectionRepositoryTest {

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

    private int rowCount() {
        Cursor cursor = db.query(Database.CONNECTION_TABLE_NAME, null, null, null, null, null, null);
        return cursor.getCount();
    }
}
