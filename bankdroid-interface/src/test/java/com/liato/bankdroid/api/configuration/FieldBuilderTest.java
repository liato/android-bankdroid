package com.liato.bankdroid.api.configuration;

import org.junit.Test;

import java.util.Locale;
import java.util.PropertyResourceBundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FieldBuilderTest {

    private static final String PACKAGE = FieldBuilderTest.class.getPackage().getName();
    private static final String REFERENCE = "reference";

    private static final String DEFAULT_BUNDLE = "com.liato.bankdroid.api.configuration.defaultFieldBuilderTest";

    private static final String MISSING_KEY_BUNDLE = "com.liato.bankdroid.api.configuration.missingKeyFieldBuilderTest";

    @Test
    public void testDefaultLocaleFieldBuilder() {
        FieldBuilder builder = new FieldBuilder(REFERENCE, PropertyResourceBundle.getBundle(DEFAULT_BUNDLE));
        Field field = builder.build();

        assertEquals("defaultPlaceholder",field.getPlaceholder());
        assertEquals("defaultLabel", field.getLabel());
    }

    @Test
    public void testMissingKeyLocaleFieldBuilder() {
        FieldBuilder builder = new FieldBuilder(REFERENCE, PropertyResourceBundle.getBundle(MISSING_KEY_BUNDLE));
        Field field = builder.build();

        assertEquals("defaultPlaceholder", field.getPlaceholder());
        assertEquals("field.reference.label", field.getLabel());
    }

    @Test
    public void testLocaleFieldBuilder() {
        FieldBuilder builder = new FieldBuilder(REFERENCE, PropertyResourceBundle.getBundle(DEFAULT_BUNDLE, new Locale("sv_SE")));
        Field field = builder.build();

        assertEquals("defaultPlaceholder", field.getPlaceholder());
        assertEquals("localeLabel", field.getLabel());
    }

    @Test
    public void testFieldBuilder() {
        FieldBuilder builder = new FieldBuilder(REFERENCE);
        Field field = builder.build();

        assertNull(field.getLabel());
        assertNull(field.getPlaceholder());
    }
}
