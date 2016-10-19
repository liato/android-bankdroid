package com.liato.bankdroid.db.matchers;


import com.google.common.base.Joiner;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import android.database.sqlite.SQLiteConstraintException;

public class UniqueConstraintExceptionMatcher extends
        TypeSafeMatcher<SQLiteConstraintException> {

    private final String expectedMessage;

    public UniqueConstraintExceptionMatcher(String... columns) {
        expectedMessage = new StringBuilder("[columns ")
                .append(asString(columns))
                .append(" are not unique]")
                .toString();
    }

    private String asString(String... columns) {
        return Joiner.on(", ").join(columns);
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
