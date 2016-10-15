package com.liato.bankdroid.api;

/**
 * Is responsible for creating new instances of {@link Provider} objects. This should be the single
 * point of where {@link Provider} objects are created.
 */
public interface ProviderFactory {

    /**
     * Create a new {@link Provider} instance.
     * @return a new instance of a {@link Provider}.
     */
    Provider create();
}
