package com.liato.bankdroid.banking;
import com.liato.bankdroid.api.configuration.Field;
import com.liato.bankdroid.api.configuration.FieldBuilder;
import com.liato.bankdroid.api.configuration.ProviderConfiguration;
import com.liato.bankdroid.utils.FieldTypeMapper;

import java.util.LinkedList;
import java.util.List;

public class LegacyProviderConfiguration implements ProviderConfiguration {

    public static final String USERNAME = "legacy.username";

    public static final String PASSWORD = "legacy.password";

    public static final String EXTRAS = "legacy.extras";

    private List<Field> mConfiguration;

    private Bank mLegacyBank;

    public LegacyProviderConfiguration(Bank legacyBank) {
        mLegacyBank = legacyBank;
    }

    @Override
    public List<Field> getConnectionConfiguration() {
        if (mConfiguration == null) {
            mConfiguration = createConfiguration();
        }
        return mConfiguration;
    }

    private List<Field> createConfiguration() {
        List<Field> configuration = new LinkedList<>();
        configuration.add(new FieldBuilder(USERNAME)
                .label(mLegacyBank.getContext().getString(mLegacyBank.getInputTitleUsername()))
                .placeholder(mLegacyBank.getInputHintUsername())
                .hidden(mLegacyBank.isInputUsernameHidden())
                .required(true)
                .fieldType(FieldTypeMapper.toFieldType(mLegacyBank.getInputTypeUsername()))
                .build());

        configuration.add(new FieldBuilder(PASSWORD)
                .label(mLegacyBank.getContext().getString(mLegacyBank.getInputTitlePassword()))
                .hidden(mLegacyBank.isInputPasswordHidden())
                .secret(true)
                .required(true)
                .fieldType(FieldTypeMapper.toFieldType(mLegacyBank.getInputTypePassword()))
                .build());

        configuration.add(new FieldBuilder(EXTRAS)
                .label(mLegacyBank.getContext().getString(mLegacyBank.getInputTitleExtras()))
                .hidden(mLegacyBank.isInputExtrasHidden())
                .fieldType(FieldTypeMapper.toFieldType(mLegacyBank.getInputTypeExtras()))
                .build());

        return configuration;
    }
}
