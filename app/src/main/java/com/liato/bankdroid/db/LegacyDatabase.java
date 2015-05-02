package com.liato.bankdroid.db;

class LegacyDatabase {

    static final String BANK_TABLE_NAME = "banks";

    static final String BANK_ID = "_id";

    static final String BANK_BALANCE = "balance";

    static final String BANK_TYPE = "banktype";

    static final String BANK_USERNAME = "username";

    static final String BANK_PASSWORD = "password";

    static final String BANK_CUSTOM_NAME = "custname";

    static final String BANK_UPDATED = "updated";

    static final String BANK_SORT_ORDER = "sortorder";

    static final String BANK_CURRENCY = "currency";

    static final String BANK_DISABLED = "disabled";

    static final String BANK_HIDE_ACCOUNTS = "hideAccounts";

    static final String BANK_EXTRAS = "extras";


    static final String ACCOUNT_TABLE_NAME = "accounts";

    static final String ACCOUNT_BANK_ID = "bankid";

    static final String ACCOUNT_ID = "id";

    static final String ACCOUNT_BALANCE = "balance";

    static final String ACCOUNT_CURRENCY = "currency";

    static final String ACCOUNT_TYPE = "acctype";

    static final String ACCOUNT_NAME = "name";

    static final String ACCOUNT_HIDDEN = "hidden";

    static final String ACCOUNT_NOTIFY = "notify";

    static final String ACCOUNT_ALIAS_FOR = "aliasfor";


    static final String TRANSACTION_TABLE_NAME = "transactions";

    static final String TRANSACTION_ID = "_id";

    static final String TRANSACTION_DATE = "transdate";

    static final String TRANSACTION_DESCRIPTION = "btransaction";

    static final String TRANSACTION_AMOUNT = "amount";

    static final String TRANSACTION_CURRENCY = "currency";

    static final String TRANSACTION_ACCOUNT_ID = "account";

    static final String TABLE_BANKS = new StringBuilder("create table ")
            .append(BANK_TABLE_NAME)
            .append(" (")
            .append(BANK_ID)
            .append(" integer primary key autoincrement, ")
            .append(BANK_BALANCE)
            .append(" text not null, ")
            .append(BANK_TYPE)
            .append(" integer not null, ")
            .append(BANK_CUSTOM_NAME)
            .append(" text, ")
            .append(BANK_UPDATED)
            .append(" text, ")
            .append(BANK_SORT_ORDER)
            .append(" real, ")
            .append(BANK_CURRENCY)
            .append(" text, ")
            .append(BANK_DISABLED)
            .append(" integer, ")
            .append(BANK_HIDE_ACCOUNTS)
            .append(" integer);").toString();

    static final String TABLE_ACCOUNTS = new StringBuilder("create table ")
            .append(ACCOUNT_TABLE_NAME)
            .append(" (")
            .append(ACCOUNT_BANK_ID)
            .append(" integer not null, ")
            .append(ACCOUNT_ID)
            .append(" text not null, ")
            .append(ACCOUNT_BALANCE)
            .append(" text not null, ")
            .append(ACCOUNT_TYPE)
            .append(" integer not null, ")
            .append(ACCOUNT_HIDDEN)
            .append(" integer not null, ")
            .append(ACCOUNT_NOTIFY)
            .append(" integer not null, ")
            .append(ACCOUNT_CURRENCY)
            .append(" text, ")
            .append(ACCOUNT_NAME)
            .append(" text not null, ")
            .append(ACCOUNT_ALIAS_FOR)
            .append(" text);").toString();

    static final String TABLE_TRANSACTIONS = new StringBuilder("create table ")
            .append(TRANSACTION_TABLE_NAME)
            .append(" (")
            .append(TRANSACTION_ID)
            .append(" integer primary key autoincrement, ")
            .append(TRANSACTION_DATE)
            .append(" text not null, ")
            .append(TRANSACTION_DESCRIPTION)
            .append(" text not null, ")
            .append(TRANSACTION_AMOUNT)
            .append(" text not null, ")
            .append(TRANSACTION_CURRENCY)
            .append(" text, ")
            .append(TRANSACTION_ACCOUNT_ID)
            .append(" text not null);").toString();

    private LegacyDatabase() {
    }
}
