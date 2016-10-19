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
import static com.liato.bankdroid.db.Database.ACCOUNT_ID;
import static com.liato.bankdroid.db.Database.TRANSACTIONS_TABLE_NAME;
import static com.liato.bankdroid.db.Database.TRANSACTION_ACCOUNT_ID;
import static com.liato.bankdroid.db.Database.TRANSACTION_AMOUNT;
import static com.liato.bankdroid.db.Database.TRANSACTION_CONNECTION_ID;
import static com.liato.bankdroid.db.Database.TRANSACTION_CURRENCY;
import static com.liato.bankdroid.db.Database.TRANSACTION_DATE;
import static com.liato.bankdroid.db.Database.TRANSACTION_DESCRIPTION;
import static com.liato.bankdroid.db.Database.TRANSACTION_ID;
import static com.liato.bankdroid.db.Database.TRANSACTION_PENDING;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class TransactionsTableConstraintsTest {

    private static final String VALID_TRANSACTION_ID = "not_relevant_transaction_id";

    private static final String VALID_TRANSACTION_ACCOUNT_ID = Fixtures.VALID_ACCOUNT_ID;

    private static final String VALID_TRANSACTION_AMOUNT = new BigDecimal(56).toPlainString();

    private static final String VALID_TRANSACTION_CURRENCY = "not_relevant_transaction_currency";

    private static final long VALID_TRANSACTION_CONNECTION_ID = Fixtures.VALID_CONNECTION_ID;

    private static final String VALID_TRANSACTION_DATE = "not_relevant_transaction_date";

    private static final String VALID_TRANSACTION_DESCRIPTION
            = "not_relevant_transaction_description";

    private static final int PENDING_TRANSACTION = 1;

    private static final String INVALID_ACCOUNT_ID = "invalid_account_id";

    private static final int NOT_PENDING = 0;


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
        db.insertOrThrow(Database.ACCOUNTS_TABLE_NAME, null, Fixtures.createValidAccount());
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void adding_a_valid_transaction_saves_the_data_to_the_database() {
        db.insertOrThrow(Database.TRANSACTIONS_TABLE_NAME, null, createValidTransaction());

        Cursor actual = db.query(TRANSACTIONS_TABLE_NAME, null, null, null, null, null, null);

        assertThat("Transaction not saved in database", actual.moveToFirst(), is(true));
        assertThat("Invalid number of saved transactions", actual.getCount(), is(1));

        assertThat("Invalid transaction id",
                actual.getString(actual.getColumnIndex(TRANSACTION_ID)),
                is(VALID_TRANSACTION_ID));
        assertThat("Invalid account id",
                actual.getString(actual.getColumnIndex(TRANSACTION_ACCOUNT_ID)),
                is(VALID_TRANSACTION_ACCOUNT_ID));
        assertThat("Invalid amount",
                actual.getString(actual.getColumnIndex(TRANSACTION_AMOUNT)),
                is(VALID_TRANSACTION_AMOUNT));
        assertThat("Invalid currency",
                actual.getString(actual.getColumnIndex(TRANSACTION_CURRENCY)),
                is(VALID_TRANSACTION_CURRENCY));
        assertThat("Invalid connection id",
                actual.getLong(actual.getColumnIndex(TRANSACTION_CONNECTION_ID)),
                is(VALID_TRANSACTION_CONNECTION_ID));
        assertThat("Invalid transaction date",
                actual.getString(actual.getColumnIndex(TRANSACTION_DATE)),
                is(VALID_TRANSACTION_DATE));
        assertThat("Invalid description",
                actual.getString(actual.getColumnIndex(TRANSACTION_DESCRIPTION)),
                is(VALID_TRANSACTION_DESCRIPTION));
        assertThat("Invalid pending flag",
                actual.getInt(actual.getColumnIndex(TRANSACTION_PENDING)),
                is(PENDING_TRANSACTION));
    }

    @Test
    public void adding_a_transaction_with_id_set_to_null_is_prohibited() {
        nullConstraintExceptionIsThrownFor(TRANSACTION_ID);
    }

    @Test
    public void adding_a_transaction_without_an_existing_account_is_prohibited() {
        ContentValues transaction = createValidTransaction();
        transaction.put(TRANSACTION_ACCOUNT_ID, INVALID_ACCOUNT_ID);

        exception.expect(new ForeignKeyConstraintExceptionMatcher());

        db.insertOrThrow(TRANSACTIONS_TABLE_NAME, null, transaction);
    }

    @Test
    public void adding_a_transaction_with_connection_id_set_to_null_is_prohibited() {
        nullConstraintExceptionIsThrownFor(TRANSACTION_CONNECTION_ID);
    }

    @Test
    public void adding_a_transaction_with_account_id_set_to_null_is_prohibited() {
        nullConstraintExceptionIsThrownFor(TRANSACTION_ACCOUNT_ID);
    }

    @Test
    public void adding_a_transaction_with_description_set_to_null_is_prohibited() {
        nullConstraintExceptionIsThrownFor(TRANSACTION_DESCRIPTION);
    }

    @Test
    public void adding_a_transaction_with_amount_set_to_null_is_prohibited() {
        nullConstraintExceptionIsThrownFor(TRANSACTION_AMOUNT);
    }

    @Test
    public void adding_a_transaction_without_specifying_an_amount_defaults_to_zero() {
        ContentValues transaction = createValidTransaction();
        transaction.remove(TRANSACTION_AMOUNT);

        db.insertOrThrow(TRANSACTIONS_TABLE_NAME, null, transaction);

        Cursor actual = db.query(TRANSACTIONS_TABLE_NAME, null, null, null, null, null, null);
        assertThat("Transaction not saved in database", actual.moveToFirst(), is(true));

        assertThat("Invalid default amount",
                actual.getString(actual.getColumnIndex(TRANSACTION_AMOUNT)),
                is(BigDecimal.ZERO.toPlainString()));
    }

    @Test
    public void adding_a_transaction_with_currency_set_to_null_is_prohibited() {
        nullConstraintExceptionIsThrownFor(TRANSACTION_CURRENCY);
    }

    @Test
    public void adding_a_transaction_with_date_set_to_null_is_prohibited() {
        nullConstraintExceptionIsThrownFor(TRANSACTION_DATE);
    }

    @Test
    public void adding_a_transaction_with_a_pending_flag_set_to_null_is_prohibited() {
        nullConstraintExceptionIsThrownFor(TRANSACTION_PENDING);
    }

    @Test
    public void adding_a_transaction_without_specifying_if_it_is_pending_defaults_to_false() {
        ContentValues transaction = createValidTransaction();
        transaction.remove(TRANSACTION_PENDING);

        db.insertOrThrow(TRANSACTIONS_TABLE_NAME, null, transaction);

        Cursor actual = db.query(TRANSACTIONS_TABLE_NAME, null, null, null, null, null, null);
        assertThat("Transaction not saved in database", actual.moveToFirst(), is(true));

        assertThat("Invalid default pending flag", actual.getInt(actual.getColumnIndex(TRANSACTION_PENDING)), is(NOT_PENDING));
    }

    @Test
    public void  removing_an_account_will_remove_related_transactions() {
        db.insertOrThrow(TRANSACTIONS_TABLE_NAME, null, createValidTransaction());
        assertThat("Invalid number of saved transactions", rowCountFor(TRANSACTIONS_TABLE_NAME), is(1));

        db.delete(ACCOUNTS_TABLE_NAME, ACCOUNT_ID + "= ?", new String[]{VALID_TRANSACTION_ACCOUNT_ID});

        assertThat("Transactions not deleted", rowCountFor(TRANSACTIONS_TABLE_NAME), is(0));
    }

    @Test
    public void adding_a_transaction_with_an_already_existing_combination_of_transaction_id_and_account_id_and_connection_id_is_prohibited() {
        db.insertOrThrow(TRANSACTIONS_TABLE_NAME, null, createValidTransaction());

        exception.expect(SQLiteConstraintException.class);
        exception.expect(new UniqueConstraintExceptionMatcher(TRANSACTION_ACCOUNT_ID, TRANSACTION_CONNECTION_ID, TRANSACTION_ID));

        db.insertOrThrow(TRANSACTIONS_TABLE_NAME, null, createValidTransaction());
    }

    private ContentValues createValidTransaction() {
        ContentValues values = new ContentValues();
        values.put(Database.TRANSACTION_ID, VALID_TRANSACTION_ID);
        values.put(Database.TRANSACTION_ACCOUNT_ID, VALID_TRANSACTION_ACCOUNT_ID);
        values.put(Database.TRANSACTION_AMOUNT, VALID_TRANSACTION_AMOUNT);
        values.put(Database.TRANSACTION_CURRENCY, VALID_TRANSACTION_CURRENCY);
        values.put(Database.TRANSACTION_CONNECTION_ID, VALID_TRANSACTION_CONNECTION_ID);
        values.put(Database.TRANSACTION_DATE, VALID_TRANSACTION_DATE);
        values.put(Database.TRANSACTION_DESCRIPTION, VALID_TRANSACTION_DESCRIPTION);
        values.put(Database.TRANSACTION_PENDING, PENDING_TRANSACTION);
        return values;
    }

    private void nullConstraintExceptionIsThrownFor(String column) {
        ContentValues transaction = createValidTransaction();
        transaction.put(column, (String) null);

        exception.expect(new NullConstraintExceptionMatcher(
                TRANSACTIONS_TABLE_NAME,
                column));

        db.insertOrThrow(TRANSACTIONS_TABLE_NAME, null, transaction);
    }

    private int rowCountFor(String tableName) {
        Cursor cursor = db.query(tableName, null, null, null, null, null, null);
        return cursor.moveToFirst() ? cursor.getCount() : 0;
    }

}
