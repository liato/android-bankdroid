package com.liato.bankdroid.api.configuration;

import java.util.List;

/**
 * Holds the configuration for a provider.
 */
public interface ProviderConfiguration {

    /**
     * Returns the fields that should be available for configuring a provider connection.
     * @return Returns a list of available fields for provider connection configuration.
     * If no configuration is available an empty list is returned.
     */
    List<Field> getConnectionConfiguration();
}
