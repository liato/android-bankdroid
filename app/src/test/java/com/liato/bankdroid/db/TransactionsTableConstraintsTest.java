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
public class TransactionsTableConstraintsTest {

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
    public void adding_a_valid_transaction_saves_the_data_to_the_database() {
        fail();
    }

    @Test
    public void adding_a_transaction_with_id_set_to_null_is_prohibited() {
        fail();
    }

    @Test
    public void adding_a_transaction_without_an_existing_account_is_prohibited() {
        fail();
    }

    @Test
    public void adding_a_transaction_with_connection_id_set_to_null_is_prohibited() {
        fail();
    }

    @Test
    public void adding_a_transaction_with_account_id_set_to_null_is_prohibited() {
        fail();
    }

    @Test
    public void adding_a_transaction_with_description_set_to_null_is_prohibited() {
        fail();
    }

    @Test
    public void adding_a_transaction_with_amount_set_to_null_is_prohibited() {
        fail();
    }

    @Test
    public void adding_a_transaction_without_specifying_an_amount_defaults_to_zero() {
        fail();
    }

    @Test
    public void adding_a_transaction_with_currency_set_to_null_is_prohibited() {
        fail();
    }

    @Test
    public void adding_a_transaction_with_date_set_to_null_is_prohibited() {
        fail();
    }

    @Test
    public void adding_a_transaction_with_a_pending_flag_set_to_null_is_prohibited() {
        fail();
    }

    @Test
    public void adding_a_transaction_without_specifying_if_it_is_pending_defaults_to_false() {
        fail();
    }

    @Test
    public void  removing_an_account_will_remove_related_transactions() {
        fail();
    }

    @Test
    public void adding_a_transaction_with_an_already_existing_combination_of_transaction_id_and_account_id_and_connection_id_is_prohibited() {
        fail();
    }

}
