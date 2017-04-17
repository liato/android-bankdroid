package com.liato.bankdroid.db;

import com.liato.bankdroid.BuildConfig;
import com.liato.bankdroid.repository.entities.ConnectionEntity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = "src/main/AndroidManifest.xml")
public class ConnectionRepositoryImplTest {

    @Test
    public void should_save_connection_() {
        DatabaseHelper helper = DatabaseHelper.getHelper(RuntimeEnvironment.application);
        ConnectionRepositoryImpl underTest = new ConnectionRepositoryImpl(helper);
        ConnectionEntity connection = new ConnectionEntity(-1, "test");
        connection.setName("frans");
        connection.setBalance(new BigDecimal(100));
        long id = underTest.save(connection);
        assertThat(id, is(not(ConnectionEntity.DEFAULT_ID)));
    }
}
