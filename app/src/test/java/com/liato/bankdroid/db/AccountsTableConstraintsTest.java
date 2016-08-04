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
public class AccountsTableConstraintsTest {

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
    public void adding_a_valid_account_saves_the_data_to_the_database() {
        fail();
    }

    @Test
    public void adding_an_account_without_an_existing_connection_is_prohibited() {
        fail();
    }

    @Test
    public void adding_an_account_with_connection_set_to_null_is_prohibited() {
        fail();
    }

    @Test
    public void adding_an_account_with_account_id_set_to_null_is_prohibited() {
        fail();
    }

    @Test
    public void removing_a_connection_will_remove_related_accounts() {
        fail();
    }

    @Test
    public void adding_an_account_with_account_type_set_to_null_is_prohibited() {
        fail();
    }

    @Test
    public void adding_an_account_with_the_name_set_to_null_is_prohibited() {
        fail();
    }

    @Test
    public void adding_an_account_with_balance_set_to_null_is_prohibited() {
        fail();
    }

    @Test
    public void adding_an_account_with_no_balance_specified_defaults_to_zero() {
        fail();
    }

    @Test
    public void adding_an_account_with_hidden_set_to_null_is_prohibited() {
        fail();
    }

    @Test
    public void adding_an_account_with_no_hidden_field_defaults_to_false() {
        fail();
    }

    @Test
    public void adding_an_account_with_currency_set_to_null_is_prohibited() {
        fail();
    }

    @Test
    public void adding_an_account_with_notification_enabled_set_to_null_is_prohibited() {
        fail();
    }

    @Test
    public void adding_an_account_without_specifying_notifications_enabled_defaults_to_true() {
        fail();
    }

    @Test
    public void adding_an_account_with_an_already_existing_combination_of_account_id_and_connection_id_is_prohibited() {
        fail();
    }
}
