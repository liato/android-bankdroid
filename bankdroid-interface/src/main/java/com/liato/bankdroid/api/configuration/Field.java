package com.liato.bankdroid.api.configuration;

import java.util.List;

/**
 * Represent an input field that is rendered for the user.
 */
public interface Field {

    /**
     * Returns the reference to this field. This value needs to be unique per configuration.
     *
     * @return the reference to this field.
     */
    String getReference();

    /**
     * Get the placeholder for this field.
     * The placeholder specifies a short hint that describes the expected value of an
     * field (e.g. a sample value or a short description of the expected format).
     * The short hint is displayed in the input field before the user enters a value.
     *
     * @return the placeholder for this field.
     */
    String getPlaceholder();

    /**
     * Get the label for this field.
     *
     * @return the label for this field.
     */
    String getLabel();

    /**
     * Get the field type for this field.
     *
     * @return The field type for this field. Defaults to {@link FieldType#TEXT}.
     */
    FieldType getFieldType();

    /**
     * Returns {@code true} if the field is a required field.
     *
     * The {@link #validate(String)} will fail if this value is true and the string to be validated
     * are empty or {@code null}.
     * @return {@code true} if the field is a required field. Otherwise {@code false}.
     */
    boolean isRequired();

    /**
     * Returns {@code true} if the field should be hidden for the end user when it is rendered.
     *
     * @return {@code true} if the field should be hidden. Otherwise {@code false}.
     */
    boolean isHidden();

    /**
     * Returns {@code true} if, and only if, the field value should be encrypted before it is
     * stored. An encrypted field should be treated as a sensitive field and hence be rendered as a
     * password field.
     * @return {@code true} if the field value should be encrypted before it is stored. Otherwise
     * {@code false}.
     */
    boolean isEncrypted();

    /**
     * Returns a list of available values for this field. If this list is not empty the field
     * should be rendered as a combo box or a radio button group.
     * @return A list of available values for the field.
     */
    List<Entry> getValues();

    /**
     * Validate the user input before changes are accepted by the system. This method should
     * at least validate the {@link #isRequired()} method.
     *
     * @param value The value to be validated.
     * @throws IllegalArgumentException is thrown if the validation fails. A detailed error message
     * is included in the exception.
     */
    void validate(String value) throws IllegalArgumentException;
}
