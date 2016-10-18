package com.liato.bankdroid.api.configuration;

import java.util.Collections;
import java.util.List;

import java.util.ResourceBundle;

/**
 * A builder for building {@link Field} objects.
 */
public class FieldBuilder {

    private BasicField field;

    /**
     * Create a new {@link Field} builder without i18n support.
     */
    public FieldBuilder(String reference) {
      this(reference, null);
    }

    /**
     * Create a new {@link Field} builder with i18n support for {@link Field#getLabel()} and {@link Field#getPlaceholder()}.
     * The following keys needs to be in the ResourceBundle:
     * {@code field.{reference}.label} - Locale label value
     * {@code field.{reference}.placeholder} - Locale placeholder value.
     *
     * Setting {@link #placeholder(String)} or {@link #label(String) specifically will override the i18n values.
     * Otherwise they will be set to the value specified in the Locale bundle or in the default bundle if not present.
     * If a key is not present at all in the ResourceBundle the key will will be returned.
     * @param reference Field reference
     * @param bundle The ResourceBundle to be used for i18n support.
     */
    public FieldBuilder(String reference, ResourceBundle bundle) {
        if(reference == null || reference.trim().isEmpty()) {
            throw new IllegalArgumentException("reference must be provided.");
        }
        field = new BasicField(reference, bundle);
    }

    public Field build() {
        return field;
    }

    public FieldBuilder label(String label) {
        field.label = label;
        return this;
    }

    public FieldBuilder placeholder(String placeholder) {
        field.placeholder = placeholder;
        return this;
    }

    public FieldBuilder fieldType(FieldType fieldType) {
        field.fieldType = fieldType;
        return this;
    }

    public FieldBuilder required(boolean required) {
        field.required = required;
        return this;
    }

    public FieldBuilder hidden(boolean hidden) {
        field.hidden = hidden;
        return this;
    }

    public FieldBuilder secret(boolean secret) {
        field.secret = secret;
        return this;
    }

    public FieldBuilder values(List<Entry> values) {
        field.values = values;
        return this;
    }

    public FieldBuilder validator(FieldValidator validator) {
        field.validator = validator;
        return this;
    }

    private class BasicField implements Field {

        private ResourceBundle resourceBundle;

        private String reference;

        private String placeholder;

        private String label;

        private FieldType fieldType;

        private boolean required;

        private boolean hidden;

        private boolean secret;

        private List<Entry> values;

        private FieldValidator validator;

        BasicField(String reference, ResourceBundle bundle) {
            this.reference = reference;
            this.resourceBundle = bundle;
        }

        @Override
        public String getReference() {
            return reference;
        }

        @Override
        public String getPlaceholder() {
            return placeholder == null ? getLocaleString("placeholder") : placeholder;
        }

        @Override
        public String getLabel() {
            return label == null ? getLocaleString("label") : label;
        }

        @Override
        public FieldType getFieldType() {
            return fieldType;
        }

        @Override
        public boolean isRequired() {
            return required;
        }

        @Override
        public boolean isHidden() {
            return hidden;
        }

        @Override
        public boolean isSecret() {
            return secret;
        }

        @Override
        public List<Entry> getValues() {
            if(values == null) {
                values = Collections.emptyList();
            }
            return values;
        }

        @Override
        public void validate(String value) throws IllegalArgumentException {
            if(isRequired()) {
                if(value == null || value.trim().isEmpty()) {
                    throw new IllegalArgumentException(String.format("%s is required", getLabel()));
                }
                if(validator != null) {
                    validator.validate(value);
                }
            }
        }

        private String getLocaleString(String key) {
            if(!isLocale()) {
                return null;
            }
            String propertyKey = String.format("field.%s.%s", getReference(), key);
            return resourceBundle.containsKey(propertyKey) ? resourceBundle.getString(propertyKey)
                    : propertyKey;
        }

        private boolean isLocale() {
            return resourceBundle != null;
        }
    }
}
