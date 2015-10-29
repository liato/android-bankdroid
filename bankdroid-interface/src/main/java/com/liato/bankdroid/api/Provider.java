package com.liato.bankdroid.api;

import com.liato.bankdroid.api.configuration.ProviderConfiguration;

/**
 * Represents a provider. e.g. a bank, stock broker, bus company etc.
 */
public interface Provider {

    /**
     * Returns a unique identifier for the provider.
     * @return the provider id.
     */
    String getId();

    /**
     * Returns the name of the provider.
     * @return The provider's name.
     */
    String getName();

    /**
     * Indicates if a provider implementation is currently broken.
     * @return {@code true} if the provider implementation is broken. Otherwise {@false}.
     */
    boolean isBroken();

    /**
     * Returns the configuration available for the provider.
     * @return the provider configuration.
     */
    ProviderConfiguration getConfiguration();

}
