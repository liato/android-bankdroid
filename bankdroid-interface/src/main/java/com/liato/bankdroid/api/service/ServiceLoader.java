package com.liato.bankdroid.api.service;

import com.liato.bankdroid.api.ProviderFactory;

import java.util.Set;

/**
 * The service loader is responsible for loading all available {@link ProviderFactory} known to the
 * application. This is the single point of creating new {@link com.liato.bankdroid.api.Provider}s.
 */
public interface ServiceLoader {

    /**
     * Loads all available {@link ProviderFactory}.
     * @return A set of all available {@link ProviderFactory} within the application.
     */
    Set<ProviderFactory> load();
}
