package com.liato.bankdroid.db.matchers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import android.database.sqlite.SQLiteConstraintException;

public class NullConstraintExceptionMatcher extends TypeSafeMatcher<SQLiteConstraintException> {

    private final String expectedMessage;

    public NullConstraintExceptionMatcher(String database, String column) {
        expectedMessage = new StringBuilder("[")
                .append(database.toLowerCase())
                .append(".")
                .append(column.toLowerCase())
                .append(" may not be NULL]")
                .toString();
    }

    @Override
    public boolean matchesSafely(SQLiteConstraintException item) {
        return item.getCause() != null &&
                item.getCause().getMessage() != null &&
                item.getCause().getMessage().endsWith(expectedMessage);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("expected error message to end with")
                .appendValue(expectedMessage);
    }
}
