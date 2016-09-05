package com.liato.bankdroid.db;

public class Database {

    static final String DATABASE_NAME = "data";

    static final int DATABASE_VERSION = 13;

    static final String PROPERTY_TABLE_NAME = "connection_properties";
    public static final String PROPERTY_CONNECTION_ID = "connection_id";
    public static final String PROPERTY_KEY = "property";
    public static final String PROPERTY_VALUE = "value";

    static final String CONNECTION_TABLE_NAME = "connections";

    static final String CONNECTION_ID = "_id";

    static final String CONNECTION_PROVIDER_ID = "provider_id";

    static final String CONNECTION_NAME = "name";

    static final String CONNECTION_ENABLED = "enabled";

    static final String CONNECTION_SORT_ORDER = "sort_order";

    static final String CONNECTION_LAST_UPDATED = "last_updated";

    static final String TABLE_CONNECTION = new StringBuilder("CREATE TABLE ")
            .append(CONNECTION_TABLE_NAME)
            .append(" (")
            .append(CONNECTION_ID)
            .append(" INTEGER PRIMARY KEY AUTOINCREMENT, ")
            .append(CONNECTION_PROVIDER_ID)
            .append(" TEXT NOT NULL,")
            .append(CONNECTION_NAME)
            .append(" TEXT NOT NULL,")
            .append(CONNECTION_ENABLED)
            .append(" BOOLEAN DEFAULT 1 NOT NULL,")
            .append(CONNECTION_SORT_ORDER)
            .append(" REAL,")
            .append(CONNECTION_LAST_UPDATED)
            .append(" TEXT")
            .append(");")
            .toString();

    static final String ACCOUNTS_TABLE_NAME = "accounts";
    static final String ACCOUNT_ID = "id";
    static final String ACCOUNT_CONNECTION_ID = "connection_id";
    static final String ACCOUNT_TYPE = "type";
    static final String ACCOUNT_NAME = "name";
    static final String ACCOUNT_BALANCE = "balance";
    static final String ACCOUNT_CURRENCY = "currency";
    static final String ACCOUNT_HIDDEN = "hidden";
    static final String ACCOUNT_NOTIFICATIONS_ENABLED = "notifications";


    static final String TABLE_ACCOUNTS = new StringBuilder("CREATE TABLE ")
            .append(ACCOUNTS_TABLE_NAME)
            .append(" (")
            .append(ACCOUNT_ID)
            .append(" TEXT NOT NULL, ")
            .append(ACCOUNT_CONNECTION_ID)
            .append(" INTEGER NOT NULL REFERENCES ")
            .append(CONNECTION_TABLE_NAME)
            .append(" (")
            .append(CONNECTION_ID)
            .append(") ON DELETE CASCADE, ")
            .append(ACCOUNT_TYPE)
            .append(" TEXT NOT NULL, ")
            .append(ACCOUNT_NAME)
            .append(" TEXT NOT NULL, ")
            .append(ACCOUNT_BALANCE)
            .append(" TEXT NOT NULL DEFAULT (0), ")
            .append(ACCOUNT_CURRENCY)
            .append(" TEXT NOT NULL, ")
            .append(ACCOUNT_HIDDEN)
            .append(" BOOLEAN NOT NULL DEFAULT 0, ")
            .append(ACCOUNT_NOTIFICATIONS_ENABLED)
            .append(" BOOLEAN NOT NULL DEFAULT 1, ")
            .append("PRIMARY KEY (")
            .append(ACCOUNT_CONNECTION_ID)
            .append(",")
            .append(ACCOUNT_ID)
            .append("));")
            .toString();

    static final String TRANSACTIONS_TABLE_NAME = "transactions";
    static final String TRANSACTION_ID = "_id";
    static final String TRANSACTION_CONNECTION_ID = "connection_id";
    static final String TRANSACTION_ACCOUNT_ID = "account_id";
    static final String TRANSACTION_DESCRIPTION = "description";
    static final String TRANSACTION_AMOUNT = "amount";
    static final String TRANSACTION_CURRENCY = "currency";
    static final String TRANSACTION_DATE = "transaction_date";
    static final String TRANSACTION_PENDING = "pending";

    static final String TABLE_TRANSACTIONS = new StringBuilder("CREATE TABLE ")
            .append(TRANSACTIONS_TABLE_NAME)
            .append(" (")
            .append(TRANSACTION_ID)
            .append(" TEXT NOT NULL, ")
            .append(TRANSACTION_CONNECTION_ID)
            .append(" INTEGER NOT NULL, ")
            .append(TRANSACTION_ACCOUNT_ID)
            .append(" TEXT NOT NULL, ")
            .append(TRANSACTION_DESCRIPTION)
            .append(" TEXT NOT NULL, ")
            .append(TRANSACTION_AMOUNT)
            .append(" TEXT NOT NULL DEFAULT (0), ")
            .append(TRANSACTION_CURRENCY)
            .append(" TEXT NOT NULL, ")
            .append(TRANSACTION_DATE)
            .append(" TEXT NOT NULL, ")
            .append(TRANSACTION_PENDING)
            .append(" BOOLEAN NOT NULL DEFAULT false, ")
            .append("FOREIGN KEY (")
            .append(TRANSACTION_ACCOUNT_ID)
            .append(",")
            .append(TRANSACTION_CONNECTION_ID)
            .append(") REFERENCES ")
            .append(ACCOUNTS_TABLE_NAME)
            .append("(")
            .append(ACCOUNT_ID)
            .append( ",")
            .append(ACCOUNT_CONNECTION_ID)
            .append(") ON DELETE CASCADE, ")
            .append("PRIMARY KEY (")
            .append(TRANSACTION_ACCOUNT_ID)
            .append(",")
            .append(TRANSACTION_CONNECTION_ID)
            .append(",")
            .append(TRANSACTION_ID)
            .append("));")
            .toString();

    static final String TABLE_CONNECTION_PROPERTIES = new StringBuilder("CREATE TABLE ")
            .append(PROPERTY_TABLE_NAME)
            .append(" (")
            .append(PROPERTY_CONNECTION_ID)
            .append(" INTEGER NOT NULL REFERENCES ")
            .append(CONNECTION_TABLE_NAME)
            .append("(")
            .append(CONNECTION_ID)
            .append(") ON DELETE CASCADE, ")
            .append(PROPERTY_KEY)
            .append(" TEXT NOT NULL, ")
            .append(PROPERTY_VALUE)
            .append(" TEXT, ")
            .append("PRIMARY KEY (")
            .append(PROPERTY_CONNECTION_ID)
            .append(",")
            .append(PROPERTY_KEY)
            .append("));").toString();

    private Database() {
    }
}
