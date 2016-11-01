package com.liato.bankdroid.api.configuration;

public class Entry {

    private final String mKey;
    private final String mValue;

    public Entry(String key, String value) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("key cannot be null or empty.");
        }
        mKey = key;
        mValue = value;
    }

    public String getKey() {
        return mKey;
    }

    public String getValue() {
        return mValue;
    }

    @Override
    public String toString() {
        return mValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Entry other = (Entry) obj;
        return mKey.equals(other.mKey);
    }

    @Override
    public int hashCode() {
        return mKey.hashCode();
    }
}
