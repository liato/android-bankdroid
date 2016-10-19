package com.liato.bankdroid.db.matchers;


import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import android.database.sqlite.SQLiteConstraintException;

public class ForeignKeyConstraintExceptionMatcher extends
        TypeSafeMatcher<SQLiteConstraintException> {

    private static final String EXPECTED_MESSAGE = "foreign key constraint failed]";

    @Override
    public boolean matchesSafely(SQLiteConstraintException item) {
        return item.getCause() != null &&
                item.getCause().getMessage() != null &&
                item.getCause().getMessage().endsWith(EXPECTED_MESSAGE);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("expected error message to end with")
                .appendValue(EXPECTED_MESSAGE);
    }
}
