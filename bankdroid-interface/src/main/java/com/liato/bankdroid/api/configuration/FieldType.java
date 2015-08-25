package com.liato.bankdroid.api.configuration;

public enum FieldType {
    /**
     * Represent a regular input text field.
     */
    TEXT,
    /**
     * Represent an input field that only allows numbers.
     */
    NUMBER,
    /**
     * Represent an input field that should contain a phone number.
     */
    PHONE,
    /**
     * Represents an input field that should contain an email address.
     */
    EMAIL,
}
