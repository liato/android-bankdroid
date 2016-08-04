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

import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;

import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ConnectionPropertiesTableConstraintsTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private SQLiteDatabase db;

    @Before
    public void setUp() throws IOException {
        DatabaseHelper databaseHelper = DatabaseHelper.getHelper(RuntimeEnvironment.application);
        db = DatabaseTestHelper.emptyDatabase();
        databaseHelper.onCreate(db);
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void adding_a_valid_connection_property_saves_the_data_to_the_database() {
        fail();
    }

    @Test
    public void adding_a_connection_property_without_an_already_existing_connection_is_prohibited() {
        fail();
    }

    @Test
    public void adding_a_connection_property_with_connection_id_set_to_null_is_prohibited() {
        fail();
    }

    @Test
    public void adding_a_connection_property_with_key_set_to_null_is_prohibited() {
        fail();
    }

    @Test
    public void adding_a_connection_property_with_an_already_existing_combination_of_connection_id_and_property_key_is_prohibited() {
        fail();
    }

    @Test
    public void removing_a_connection_will_remove_related_connection_properties() {
        fail();
    }
}
