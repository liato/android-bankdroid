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
public class AccountPropertiesTableConstraintsTest {

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
    public void adding_a_valid_account_property_saves_the_data_to_the_database() {
        fail();
    }

    @Test
    public void  adding_an_account_property_without_an_existing_account_is_prohibited() {
        fail();
    }

    @Test
    public void adding_an_account_property_with_connection_id_set_to_null_is_prohibited() {
        fail();
    }

    @Test
    public void adding_an_account_property_with_account_id_set_to_null_is_prohibited() {
        fail();
    }

    @Test
    public void adding_an_account_property_with_key_set_to_null_is_prohibited() {
        fail();
    }

    @Test
    public void adding_an_account_property_with_an_already_existing_combination_of_connection_id_and_account_id_and_key_is_prohibited() {
        fail();
    }

    @Test
    public void removing_an_account_will_remove_related_account_properties() {
        fail();
    }
}
