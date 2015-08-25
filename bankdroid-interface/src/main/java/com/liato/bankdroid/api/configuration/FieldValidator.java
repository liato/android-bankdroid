package com.liato.bankdroid.api.configuration;

public interface FieldValidator {

    /**
     * Validates a field value.
     *
     * @param value the value to be validated.
     * @throws IllegalArgumentException if the validation fails.
     */
    void validate(String value) throws IllegalArgumentException;

}
