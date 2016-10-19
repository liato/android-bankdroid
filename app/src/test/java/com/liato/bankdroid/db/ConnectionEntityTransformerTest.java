package com.liato.bankdroid.db;

import android.content.ContentValues;

import com.bankdroid.core.repository.ConnectionEntity;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ConnectionEntityTransformerTest {

    ConnectionEntityTransformer underTest;

    @Before
    public void setUp() {
        underTest = new ConnectionEntityTransformer();
    }

    @Test
    public void transforming_content_values_to_an_entity_maps_correctly() {
        ConnectionEntity actual = underTest.transform(Fixtures.createValidConnection()).build();
        assertThat("Incorrect transformation", actual, is(connectionEntity()));
    }

    @Test
    public void transforming_an_entity_to_content_values_maps_correctly() {
        ContentValues expected = Fixtures.createValidConnection();
        expected.remove(Database.CONNECTION_SORT_ORDER);

        ContentValues actual = underTest.transform(connectionEntity());
        assertThat("Incorrect content values transformation", actual, is(expected));
    }

    private ConnectionEntity connectionEntity() {
        return ConnectionEntity.builder()
                .id(Fixtures.VALID_CONNECTION_ID)
                .providerId(Fixtures.VALID_CONNECTION_PROVIDER_ID)
                .name(Fixtures.VALID_CONNECTION_NAME)
                .enabled(Fixtures.VALID_CONNECTION_DISABLED != 0)
                .lastUpdated(new DateTime(Fixtures.VALID_CONNECTION_LAST_UPDATED))
                .build();
    }
}
