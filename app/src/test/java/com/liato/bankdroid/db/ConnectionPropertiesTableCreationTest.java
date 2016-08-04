package com.liato.bankdroid.db;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ConnectionPropertiesTableCreationTest {

    @Test
    public void connection_properties_table_is_created_when_onCreate_is_called() throws IOException {
        fail();
    }

    @Test
    public void connection_properties_table_is_created_on_db_upgrades_where_old_version_is_11() {
        fail();
    }

    @Test
    public void a_populated_bank_table_from_db_version_11_is_migrated_to_the_connection_properties_table_when_upgrading_to_version_12() {
        fail();
    }

    @Test
    public void  upgrading_db_to_version_13_will_update_connection_properties_connection_id_to_reference_connection_table() {
        fail();
    }
}
