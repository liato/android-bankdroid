package com.liato.bankdroid.banking;

import com.liato.bankdroid.api.configuration.Field;
import com.liato.bankdroid.api.configuration.FieldBuilder;
import com.liato.bankdroid.api.configuration.FieldType;
import com.liato.bankdroid.api.configuration.ProviderConfiguration;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class BasicProviderConfiguration implements ProviderConfiguration {

    public static final String FIELD_USERNAME = "username";
    public static final String FIELD_PASSWORD = "password";

    private static final ResourceBundle LOCALE = ResourceBundle.getBundle("i18n.application");

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

        fields.add(new FieldBuilder(FIELD_USERNAME, LOCALE)
                .fieldType(FieldType.TEXT)
                .placeholder("")
                .required(true)
                .build()
        );

        fields.add(new FieldBuilder(FIELD_PASSWORD, LOCALE)
                        .fieldType(FieldType.TEXT)
                        .placeholder("")
                        .secret(true)
                        .required(true)
                        .build()
        );
        return fields;
    }
}
