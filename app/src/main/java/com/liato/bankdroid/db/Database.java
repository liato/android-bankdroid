package com.liato.bankdroid.db;

public class Database {

    static final String DATABASE_NAME = "data";

    static final int DATABASE_VERSION = 13;

    public static final String PROPERTY_TABLE_NAME = "connection_properties";
    public static final String PROPERTY_CONNECTION_ID = "connection_id";
    public static final String PROPERTY_KEY = "property";
    public static final String PROPERTY_VALUE = "value";

    static final String CONNECTION_TABLE_NAME = "connections";
    public static final String CONNECTION_ID = "_id";
    public static final String CONNECTION_PROVIDER_ID = "provider_id";
    public static final String CONNECTION_NAME = "connection_name";
    public static final String CONNECTION_ENABLED = "enabled";
    public static final String CONNECTION_SORT_ORDER = "sort_order";
    public static final String CONNECTION_LAST_UPDATED = "last_updated";

    static final String ACCOUNTS_TABLE_NAME = "accounts";
    public static final String ACCOUNT_ID = "_id";
    public static final String ACCOUNT_CONNECTION_ID = "connection_id";
    public static final String ACCOUNT_TYPE = "type";
    public static final String ACCOUNT_NAME = "name";
    public static final String ACCOUNT_BALANCE = "balance";
    public static final String ACCOUNT_CURRENCY = "currency";
    public static final String ACCOUNT_HIDDEN = "hidden";
    public static final String ACCOUNT_NOTIFICATIONS_ENABLED = "notifications";

    static final String ACCOUNT_PROPERTIES_TABLE_NAME = "account_properties";
    public static final String ACCOUNT_PROPERTIES_ACCOUNT_ID = "account_id";
    public static final String ACCOUNT_PROPERTIES_CONNECTION_ID = "connection_id";
    public static final String ACCOUNT_PROPERTIES_KEY = "property";
    public static final String ACCOUNT_PROPERTIES_VALUE = "value";

    static final String TRANSACTIONS_TABLE_NAME = "transactions";
    public static final String TRANSACTION_ID = "_id";
    public static final String TRANSACTION_CONNECTION_ID = "connection_id";
    public static final String TRANSACTION_ACCOUNT_ID = "account_id";
    public static final String TRANSACTION_DESCRIPTION = "description";
    public static final String TRANSACTION_AMOUNT = "amount";
    public static final String TRANSACTION_CURRENCY = "currency";
    public static final String TRANSACTION_DATE = "transaction_date";
    public static final String TRANSACTION_PENDING = "pending";

    static final String EQUITIES_TABLE_NAME = "equities";
    public static final String EQUITY_ID = "_id";
    public static final String EQUITY_CONNECTION_ID = "connection_id";
    public static final String EQUITY_ACCOUNT_ID = "account_id";
    public static final String EQUITY_NAME = "name";
    public static final String EQUITY_TYPE = "type";
    public static final String EQUITY_QUANTITY = "quantity";
    public static final String EQUITY_REVENUE = "revenue";
    public static final String EQUITY_COST = "cost";

    static final String PAYMENTS_TABLE_NAME = "payments";
    public static final String PAYMENT_ID = "_id";
    public static final String PAYMENT_CONNECTION_ID = "connection_id";
    public static final String PAYMENT_ACCOUNT_ID = "account_id";
    public static final String PAYMENT_DESCRIPTION = "description";
    public static final String PAYMENT_AMOUNT = "amount";
    public static final String PAYMENT_CURRENCY = "currency";
    public static final String PAYMENT_DUE_DATE = "due_date";

    static final String TABLE_CONNECTION_PROPERTIES = "CREATE TABLE " + PROPERTY_TABLE_NAME + " (" +
            PROPERTY_CONNECTION_ID + " INTEGER REFERENCES " + CONNECTION_TABLE_NAME + "("
            + CONNECTION_ID + ") ON DELETE CASCADE, " +
            PROPERTY_KEY + " TEXT NOT NULL, " +
            PROPERTY_VALUE + " TEXT, " +
            "PRIMARY KEY (" + PROPERTY_CONNECTION_ID + "," + PROPERTY_KEY + "));";

    static final String TABLE_CONNECTION = "CREATE TABLE " + CONNECTION_TABLE_NAME + " (" +
            CONNECTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            CONNECTION_PROVIDER_ID + " TEXT NOT NULL, " +
            CONNECTION_NAME + " TEXT NOT NULL, " +
            CONNECTION_ENABLED + " BOOLEAN DEFAULT true NOT NULL, " +
            CONNECTION_SORT_ORDER + " REAL, " +
            CONNECTION_LAST_UPDATED + " TEXT);";

    static final String TABLE_ACCOUNTS = "CREATE TABLE " + ACCOUNTS_TABLE_NAME + " (" +
            ACCOUNT_ID + " TEXT NOT NULL, " +
            ACCOUNT_CONNECTION_ID + " INTEGER NOT NULL REFERENCES " + CONNECTION_TABLE_NAME + " ("
            + CONNECTION_ID + ") ON DELETE CASCADE, " +
            ACCOUNT_TYPE + " TEXT NOT NULL, " +
            ACCOUNT_NAME + " TEXT NOT NULL, " +
            ACCOUNT_BALANCE + " TEXT NOT NULL DEFAULT (0), " +
            ACCOUNT_CURRENCY + " TEXT NOT NULL, " +
            ACCOUNT_HIDDEN + " BOOLEAN NOT NULL DEFAULT false, " +
            ACCOUNT_NOTIFICATIONS_ENABLED + " BOOLEAN NOT NULL DEFAULT true, " +
            "PRIMARY KEY (" + ACCOUNT_CONNECTION_ID + "," + ACCOUNT_ID + "));";

    static final String TABLE_ACCOUNT_PROPERTIES = "CREATE TABLE " + ACCOUNT_PROPERTIES_TABLE_NAME
            + " (" +
            ACCOUNT_PROPERTIES_CONNECTION_ID + " INTEGER NOT NULL," +
            ACCOUNT_PROPERTIES_ACCOUNT_ID + " TEXT NOT NULL, " +
            ACCOUNT_PROPERTIES_KEY + " TEXT NOT NULL, " +
            ACCOUNT_PROPERTIES_VALUE + " TEXT, " +
            "FOREIGN KEY (" + ACCOUNT_PROPERTIES_ACCOUNT_ID + "," + ACCOUNT_PROPERTIES_CONNECTION_ID
            + ") REFERENCES " + ACCOUNTS_TABLE_NAME + "(" + ACCOUNT_ID + "," + ACCOUNT_CONNECTION_ID
            + ") ON DELETE CASCADE," +
            "PRIMARY KEY (" + ACCOUNT_PROPERTIES_CONNECTION_ID + "," + ACCOUNT_PROPERTIES_ACCOUNT_ID
            + "," + ACCOUNT_PROPERTIES_KEY + "));";

    static final String TABLE_TRANSACTIONS = "CREATE TABLE " + TRANSACTIONS_TABLE_NAME + " (" +
            TRANSACTION_ID + " TEXT NOT NULL, " +
            TRANSACTION_CONNECTION_ID + " INTEGER NOT NULL, " +
            TRANSACTION_ACCOUNT_ID + " TEXT NOT NULL, " +
            TRANSACTION_DESCRIPTION + " TEXT NOT NULL, " +
            TRANSACTION_AMOUNT + " TEXT NOT NULL DEFAULT (0), " +
            TRANSACTION_CURRENCY + " TEXT NOT NULL, " +
            TRANSACTION_DATE + " TEXT NOT NULL, " +
            TRANSACTION_PENDING + " BOOLEAN NOT NULL DEFAULT false, " +
            "FOREIGN KEY (" + TRANSACTION_ACCOUNT_ID + "," + TRANSACTION_CONNECTION_ID
            + ") REFERENCES " + ACCOUNTS_TABLE_NAME + "(" + ACCOUNT_ID + "," + ACCOUNT_CONNECTION_ID
            + ") ON DELETE CASCADE, " +
            "PRIMARY KEY (" + TRANSACTION_ACCOUNT_ID + "," + TRANSACTION_CONNECTION_ID + ","
            + TRANSACTION_ID + "));";

    static final String TABLE_EQUITIES = "CREATE TABLE " + EQUITIES_TABLE_NAME + " (" +
            EQUITY_ID + " TEXT NOT NULL, "+
            EQUITY_CONNECTION_ID + " INTEGER NOT NULL, " +
            EQUITY_ACCOUNT_ID + " TEXT NOT NULL, " +
            EQUITY_NAME + " TEXT NOT NULL, " +
            EQUITY_TYPE + " TEXT NOT NULL, " +
            EQUITY_QUANTITY + " TEXT NOT NULL DEFAULT (0), " +
            EQUITY_COST + " TEXT NOT NULL DEFAULT (0), " +
            EQUITY_REVENUE + " TEXT NOT NULL DEFAULT (0), " +
            "FOREIGN KEY (" + EQUITY_ACCOUNT_ID + "," + EQUITY_CONNECTION_ID + ") REFERENCES "
            + ACCOUNTS_TABLE_NAME + "(" + ACCOUNT_ID + "," + ACCOUNT_CONNECTION_ID
            + ") ON DELETE CASCADE," +
            "PRIMARY KEY (" + EQUITY_ACCOUNT_ID + "," + EQUITY_CONNECTION_ID + "," + EQUITY_ID
            + "));";

    static final String TABLE_PAYMENTS = "CREATE TABLE " + PAYMENTS_TABLE_NAME + " (" +
            PAYMENT_ID + " TEXT NOT NULL, " +
            PAYMENT_CONNECTION_ID + " INTEGER NOT NULL, " +
            PAYMENT_ACCOUNT_ID + " TEXT NOT NULL, " +
            PAYMENT_DESCRIPTION + " TEXT NOT NULL, " +
            PAYMENT_AMOUNT + " TEXT NOT NULL DEFAULT (0), " +
            PAYMENT_CURRENCY + " TEXT NOT NULL, " +
            PAYMENT_DUE_DATE + " TEXT NOT NULL, " +
            "FOREIGN KEY (" + PAYMENT_ACCOUNT_ID + "," + PAYMENT_CONNECTION_ID + ") REFERENCES "
            + ACCOUNTS_TABLE_NAME + "(" + ACCOUNT_ID + "," + ACCOUNT_CONNECTION_ID
            + ") ON DELETE CASCADE, " +
            "PRIMARY KEY (" + PAYMENT_ACCOUNT_ID + "," + PAYMENT_CONNECTION_ID + "," + PAYMENT_ID
            + "));";
}
