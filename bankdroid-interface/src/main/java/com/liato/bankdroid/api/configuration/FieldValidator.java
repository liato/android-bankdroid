package com.liato.bankdroid.api.configuration;

public interface FieldValidator {

    /**
     * Validates a field value.
     *
     * @param param the parameter to be validated.
     * @throws IllegalArgumentException if the validation fails.
     */
    void validate(String param) throws IllegalArgumentException;

}
