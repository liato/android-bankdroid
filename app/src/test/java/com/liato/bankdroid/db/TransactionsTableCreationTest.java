package com.liato.bankdroid.db;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class TransactionsTableCreationTest {

    @Test
    public void transactions_table_is_created_when_onCreate_is_called() throws IOException {
        fail();
    }

    @Test
    public void transactions_table_is_created_on_db_upgrades_where_old_version_is_12() {
        fail();
    }

    @Test
    public void a_populated_transactions_table_from_v12_is_migrated_to_new_structure_when_upgrading_to_v13() {
        fail();
    }
}
