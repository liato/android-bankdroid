package com.liato.bankdroid.configuration;

import com.liato.bankdroid.api.configuration.Field;
import com.liato.bankdroid.api.configuration.FieldBuilder;
import com.liato.bankdroid.api.configuration.FieldType;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DefaultConnectionConfiguration {

    public static final String NAME = "provider.configuration.name";

    private final static List<Field> configuration = createConfiguration();

    private static List<Field> createConfiguration() {
        List<Field> configuration = new ArrayList<>();
        configuration.add(new FieldBuilder(NAME, ResourceBundle.getBundle("i18n.application"))
                .placeholder("")
                .fieldType(FieldType.TEXT)
                .build());
        return configuration;
    }

    public static List<Field> fields() {
        return configuration;
    }
}
