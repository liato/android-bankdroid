package com.liato.bankdroid.api.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A builder for building {@link Field} objects.
 */
public class FieldBuilder {

    private BasicField field;

    public FieldBuilder(String reference) {
        if(reference == null || reference.trim().isEmpty()) {
            throw new IllegalArgumentException("reference must be provided.");
        }
        field = new BasicField(reference);
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

    public FieldBuilder encrypted(boolean encrypted) {
        field.encrypted = encrypted;
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

        private String reference;

        private String placeholder;

        private String label;

        private FieldType fieldType;

        private boolean required;

        private boolean hidden;

        private boolean encrypted;

        private List<Entry> values;

        private FieldValidator validator;

        public BasicField(String reference) {
            this.reference = reference;
        }

        @Override
        public String getReference() {
            return reference;
        }

        @Override
        public String getPlaceholder() {
            return placeholder;
        }

        @Override
        public String getLabel() {
            return label;
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
        public boolean isEncrypted() {
            return encrypted;
        }

        @Override
        public List<Entry> getValues() {
            if(values == null) {
                values = Collections.emptyList();
            };
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
    }
}
