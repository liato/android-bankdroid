package com.liato.bankdroid.utils;

import com.liato.bankdroid.api.configuration.FieldType;

import android.text.InputType;

// TODO Move to app module when all legacy banks have been converted.
public class FieldTypeMapper {

    public static FieldType toFieldType(int androidFieldType) {
        switch(androidFieldType) {
            case InputType.TYPE_CLASS_NUMBER:
                return FieldType.NUMBER;
            case InputType.TYPE_CLASS_PHONE:
                return FieldType.PHONE;
            case InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
                return FieldType.EMAIL;
            default:
                return FieldType.TEXT;
        }
    }
    public static int fromFieldType(FieldType fieldType) {
        switch(fieldType) {
            case NUMBER:
                return InputType.TYPE_CLASS_NUMBER;
            case PHONE:
                return InputType.TYPE_CLASS_PHONE;
            case EMAIL:
                return InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
            default:
                return InputType.TYPE_CLASS_TEXT;
        }
    }
}
