package com.liato.bankdroid.db;

import com.liato.bankdroid.db.matchers.ForeignKeyConstraintExceptionMatcher;
import com.liato.bankdroid.db.matchers.NullConstraintExceptionMatcher;
import com.liato.bankdroid.db.matchers.UniqueConstraintExceptionMatcher;

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

import java.io.IOException;
import java.math.BigDecimal;

import static com.liato.bankdroid.db.Database.ACCOUNTS_TABLE_NAME;
import static com.liato.bankdroid.db.Database.ACCOUNT_BALANCE;
import static com.liato.bankdroid.db.Database.ACCOUNT_CONNECTION_ID;
import static com.liato.bankdroid.db.Database.ACCOUNT_CURRENCY;
import static com.liato.bankdroid.db.Database.ACCOUNT_HIDDEN;
import static com.liato.bankdroid.db.Database.ACCOUNT_ID;
import static com.liato.bankdroid.db.Database.ACCOUNT_NAME;
import static com.liato.bankdroid.db.Database.ACCOUNT_NOTIFICATIONS_ENABLED;
import static com.liato.bankdroid.db.Database.ACCOUNT_TYPE;
import static com.liato.bankdroid.db.Database.CONNECTION_ID;
import static com.liato.bankdroid.db.Database.CONNECTION_TABLE_NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AccountsTableConstraintsTest {

    private static final long NON_EXISTING_CONNECTION_ID = -1;

    private static final int NOT_HIDDEN = 0;

    private static final int NOTIFICATIONS_ENABLED = 1;

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
    public void adding_a_valid_account_saves_the_data_to_the_database() {
        db.insertOrThrow(ACCOUNTS_TABLE_NAME, null, Fixtures.createValidAccount());

        Cursor actual = db.query(ACCOUNTS_TABLE_NAME, null, null, null, null, null, null);

        assertThat(actual.moveToFirst(), is(true));
        assertThat(actual.getCount(), is(1));

        assertThat(actual.getString(actual.getColumnIndex(ACCOUNT_ID)), is(Fixtures.VALID_ACCOUNT_ID));
        assertThat(actual.getLong(actual.getColumnIndex(ACCOUNT_CONNECTION_ID)), is(Fixtures.VALID_ACCOUNT_CONNECTION_ID));
        assertThat(actual.getString(actual.getColumnIndex(ACCOUNT_TYPE)), is(Fixtures.VALID_ACCOUNT_TYPE));
        assertThat(actual.getString(actual.getColumnIndex(ACCOUNT_NAME)),is(Fixtures.VALID_ACCOUNT_NAME));
        assertThat(actual.getString(actual.getColumnIndex(ACCOUNT_BALANCE)),is(Fixtures.VALID_ACCOUNT_BALANCE));
        assertThat(actual.getString(actual.getColumnIndex(ACCOUNT_CURRENCY)), is(Fixtures.VALID_ACCOUNT_CURRENCY));
        assertThat(actual.getInt(actual.getColumnIndex(ACCOUNT_HIDDEN)), is(Fixtures.VALID_ACCOUNT_HIDDEN));
        assertThat(actual.getInt(actual.getColumnIndex(ACCOUNT_NOTIFICATIONS_ENABLED)),is(Fixtures.VALID_ACCOUNT_NOTIFICATIONS));
    }

    @Test
    public void adding_an_account_without_an_existing_connection_is_prohibited() {
        ContentValues account = Fixtures.createValidAccount();
        account.put(ACCOUNT_CONNECTION_ID, NON_EXISTING_CONNECTION_ID);

        exception.expect(new ForeignKeyConstraintExceptionMatcher());

        db.insertOrThrow(ACCOUNTS_TABLE_NAME, null, account);
    }

    @Test
    public void adding_an_account_with_connection_set_to_null_is_prohibited() {
        nullConstraintExceptionIsThrownFor(ACCOUNT_CONNECTION_ID);
    }

    @Test
    public void adding_an_account_with_account_id_set_to_null_is_prohibited() {
        nullConstraintExceptionIsThrownFor(ACCOUNT_ID);
    }

    @Test
    public void removing_a_connection_will_remove_related_accounts() {
        db.insertOrThrow(ACCOUNTS_TABLE_NAME, null, Fixtures.createValidAccount());
        Cursor savedAccounts = db.query(ACCOUNTS_TABLE_NAME, null, null, null, null, null, null);

        assertThat(savedAccounts.moveToFirst(), is(true));
        assertThat(savedAccounts.getCount(), is(1));
        savedAccounts.close();

        db.delete(CONNECTION_TABLE_NAME, CONNECTION_ID + "= ?", new String[]{Long.toString(Fixtures.VALID_ACCOUNT_CONNECTION_ID)});

        Cursor actual = db.query(ACCOUNTS_TABLE_NAME, null, null, null, null, null, null);
        assertThat(actual.moveToFirst(), is(false));
        assertThat(actual.getCount(), is(0));
    }

    @Test
    public void adding_an_account_with_account_type_set_to_null_is_prohibited() {
        nullConstraintExceptionIsThrownFor(ACCOUNT_TYPE);
    }

    @Test
    public void adding_an_account_with_the_name_set_to_null_is_prohibited() {
        nullConstraintExceptionIsThrownFor(ACCOUNT_NAME);
    }

    @Test
    public void adding_an_account_with_balance_set_to_null_is_prohibited() {
        nullConstraintExceptionIsThrownFor(ACCOUNT_BALANCE);
    }

    @Test
    public void adding_an_account_with_no_balance_specified_defaults_to_zero() {
        ContentValues account = Fixtures.createValidAccount();
        account.remove(ACCOUNT_BALANCE);

        db.insertOrThrow(ACCOUNTS_TABLE_NAME, null, account);

        Cursor actual = db.query(ACCOUNTS_TABLE_NAME, null, null, null, null, null, null);
        assertThat(actual.moveToFirst(), is(true));

        assertThat(actual.getString(actual.getColumnIndex(ACCOUNT_BALANCE)), is(BigDecimal.ZERO.toPlainString()));
    }

    @Test
    public void adding_an_account_with_hidden_set_to_null_is_prohibited() {
        nullConstraintExceptionIsThrownFor(ACCOUNT_HIDDEN);
    }

    @Test
    public void adding_an_account_with_no_hidden_field_defaults_to_false() {
        ContentValues account = Fixtures.createValidAccount();
        account.remove(ACCOUNT_HIDDEN);

        db.insertOrThrow(ACCOUNTS_TABLE_NAME, null, account);

        Cursor actual = db.query(ACCOUNTS_TABLE_NAME, null, null, null, null, null, null);
        assertThat(actual.moveToFirst(), is(true));

        assertThat(actual.getInt(actual.getColumnIndex(ACCOUNT_HIDDEN)), is(NOT_HIDDEN));
    }

    @Test
    public void adding_an_account_with_currency_set_to_null_is_prohibited() {
        nullConstraintExceptionIsThrownFor(ACCOUNT_CURRENCY);
    }

    @Test
    public void adding_an_account_with_notification_enabled_set_to_null_is_prohibited() {
        nullConstraintExceptionIsThrownFor(ACCOUNT_NOTIFICATIONS_ENABLED);
    }

    @Test
    public void adding_an_account_without_specifying_notifications_enabled_defaults_to_true() {
        ContentValues account = Fixtures.createValidAccount();
        account.remove(ACCOUNT_NOTIFICATIONS_ENABLED);

        db.insertOrThrow(ACCOUNTS_TABLE_NAME, null, account);

        Cursor actual = db.query(ACCOUNTS_TABLE_NAME, null, null, null, null, null, null);
        assertThat(actual.moveToFirst(), is(true));

        assertThat(actual.getInt(actual.getColumnIndex(ACCOUNT_NOTIFICATIONS_ENABLED)), is(NOTIFICATIONS_ENABLED));
    }

    @Test
    public void adding_an_account_with_an_already_existing_combination_of_account_id_and_connection_id_is_prohibited() {
        db.insertOrThrow(ACCOUNTS_TABLE_NAME, null, Fixtures.createValidAccount());
        Cursor savedAccounts = db.query(ACCOUNTS_TABLE_NAME, null, null, null, null, null, null);
        assertThat(savedAccounts.moveToFirst(), is(true));
        assertThat(savedAccounts.getCount(), is(1));
        savedAccounts.close();

        exception.expect(SQLiteConstraintException.class);
        exception.expect(new UniqueConstraintExceptionMatcher(ACCOUNT_CONNECTION_ID, ACCOUNT_ID));

        db.insertOrThrow(ACCOUNTS_TABLE_NAME, null, Fixtures.createValidAccount());
    }

    private void nullConstraintExceptionIsThrownFor(String column) {
        ContentValues account = Fixtures.createValidAccount();
        account.put(column, (String) null);

        exception.expect(new NullConstraintExceptionMatcher(
                ACCOUNTS_TABLE_NAME,
                column));

        db.insertOrThrow(ACCOUNTS_TABLE_NAME, null, account);
    }
}
