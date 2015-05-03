package com.liato.bankdroid.db;

public class Database {

    static final String DATABASE_NAME = "data";

    static final int DATABASE_VERSION = 12;

    static final String PROPERTY_TABLE_NAME = "connection_properties";
    public static final String PROPERTY_CONNECTION_ID = "connection_id";
    public static final String PROPERTY_KEY = "property";
    public static final String PROPERTY_VALUE = "value";

    static final String TABLE_CONNECTION_PROPERTIES = "CREATE TABLE " + PROPERTY_TABLE_NAME + " (" +
            PROPERTY_CONNECTION_ID + " INTEGER REFERENCES " + LegacyDatabase.BANK_TABLE_NAME + "(" + LegacyDatabase.BANK_ID + ") ON DELETE CASCADE, " +
            PROPERTY_KEY + " TEXT NOT NULL, " +
            PROPERTY_VALUE + " TEXT, " +
            "PRIMARY KEY (" + PROPERTY_CONNECTION_ID + "," + PROPERTY_KEY + "));";
}
