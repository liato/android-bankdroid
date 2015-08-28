package com.liato.bankdroid.banking;

import com.liato.bankdroid.api.configuration.Field;
import com.liato.bankdroid.api.configuration.FieldBuilder;
import com.liato.bankdroid.api.configuration.FieldType;
import com.liato.bankdroid.api.configuration.ProviderConfiguration;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BasicProviderConfiguration implements ProviderConfiguration {

    public static final String FIELD_USERNAME = "username";
    public static final String FIELD_PASSWORD = "password";

    public static final String PROPERTY_USERNAME = "provider.default.config.label.username";
    public static final String PROPERTY_PASSWORD = "provider.default.config.label.password";
    private final List<Field> mFields;

    public BasicProviderConfiguration() {
        mFields = createConfiguration();
    }

    @Override
    public List<Field> getConnectionConfiguration() {
        return mFields;
    }

    private List<Field> createConfiguration() {
        List<Field> fields = new LinkedList<>();

        fields.add(new FieldBuilder(FIELD_USERNAME)
                .fieldType(FieldType.TEXT)
                .label(PROPERTY_USERNAME)
                .required(true)
                .build()
        );

        fields.add(new FieldBuilder(FIELD_PASSWORD)
                        .fieldType(FieldType.TEXT)
                        .label(PROPERTY_PASSWORD)
                        .secret(true)
                        .required(true)
                        .build()
        );
        return fields;
    }
}
