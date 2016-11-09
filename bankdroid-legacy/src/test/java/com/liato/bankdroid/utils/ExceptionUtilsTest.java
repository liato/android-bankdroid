package com.liato.bankdroid.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ConnectException;

import eu.nullbyte.android.urllib.Urllib;
import not.bankdroid.at.all.ExceptionFactory;

public class ExceptionUtilsTest {
    @Test
    public void testBlameBankdroid() {
        Exception e = ExceptionFactory.getException();
        String before = toStringWithStacktrace(e);
        ExceptionUtils.blameBankdroid(e);
        String after = toStringWithStacktrace(e);
        String description =
                String.format("\n---- Before ----\n%s---- After ----\n%s----", before, after);

        String[] afterLines = after.split("\n");
        int lastCausedByIndex = 0;
        for (int i = 0; i < afterLines.length; i++) {
            if (afterLines[i].startsWith("Caused by: ")) {
                lastCausedByIndex = i;
            }
        }

        Assert.assertNotEquals(description, 0, lastCausedByIndex);
        Assert.assertTrue(description,
                afterLines[lastCausedByIndex + 1].startsWith("\tat com.liato.bankdroid."));
    }

    /**
     * Like {@link #testBlameBankdroid()} but with an Exception with a cause.
     */
    @Test
    public void testBlameBankdroidWithCause() {
        Exception e = ExceptionFactory.getExceptionWithCause();
        String before = toStringWithStacktrace(e);
        ExceptionUtils.blameBankdroid(e);
        String after = toStringWithStacktrace(e);
        String description =
                String.format("\n---- Before ----\n%s---- After ----\n%s----", before, after);

        String[] afterLines = after.split("\n");
        int firstCausedByIndex = 0;
        for (int i = 0; i < afterLines.length; i++) {
            if (afterLines[i].startsWith("Caused by: ")) {
                firstCausedByIndex = i;
                break;
            }
        }
        Assert.assertNotEquals(description, 0, firstCausedByIndex);
        Assert.assertTrue(description,
                afterLines[firstCausedByIndex + 1].startsWith("\tat not.bankdroid.at.all."));

        int lastCausedByIndex = 0;
        for (int i = 0; i < afterLines.length; i++) {
            if (afterLines[i].startsWith("Caused by: ")) {
                lastCausedByIndex = i;
            }
        }
        Assert.assertNotEquals(description, 0, lastCausedByIndex);
        Assert.assertTrue(description,
                afterLines[lastCausedByIndex + 1].startsWith("\tat com.liato.bankdroid."));
    }

    @Test
    public void testBlameBankdroidAlreadyToBlame() {
        // Creating it here we're already inside of Bankdroid code, blaming bankdroid should be a
        // no-op
        Exception e = new Exception();

        String before = toStringWithStacktrace(e);

        ExceptionUtils.blameBankdroid(e);
        String after = toStringWithStacktrace(e);

        Assert.assertEquals(before, after);
    }

    private String toStringWithStacktrace(Exception e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        printWriter.close();
        return stringWriter.toString();
    }

    @Test
    public void testBankdroidifyStacktrace() {
        StackTraceElement[] bankdroidified = new StackTraceElement[] {
                new StackTraceElement("not.bankdroid.SomeClass", "someMethod", "SomeClass.java", 42),
                new StackTraceElement("com.liato.bankdroid.SomeOtherClass", "someOtherMethod", "SomeOtherClass.java", 43),
        };
        bankdroidified = ExceptionUtils.bankdroidifyStacktrace(bankdroidified);

        StackTraceElement[] expected = new StackTraceElement[] {
                new StackTraceElement("com.liato.bankdroid.SomeOtherClass", "someOtherMethod", "SomeOtherClass.java", 43),
        };

        Assert.assertArrayEquals(expected, bankdroidified);

        // Test re-bankdroidification
        Assert.assertArrayEquals(expected, ExceptionUtils.bankdroidifyStacktrace(bankdroidified));
    }

    @Test
    public void testCloneExceptionWonky() {
        ExceptionFactory.WonkyException raw = ExceptionFactory.getWonkyException();

        @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
        ConnectException cloned = ExceptionUtils.cloneException(raw);

        assert cloned != null;
        Assert.assertEquals(raw.getMessage(), cloned.getMessage());
        Assert.assertArrayEquals(raw.getStackTrace(), cloned.getStackTrace());
        Assert.assertEquals(
                "Cloning an uninstantiable Exception should return an instance of its super class",
                raw.getClass().getSuperclass(), cloned.getClass());
    }

    @Test
    @SuppressWarnings({"PMD.AvoidCatchingNPE"})
    public void testCloneExceptionNPE() {
        NullPointerException raw = null;
        try {
            //noinspection ConstantConditions
            new Urllib(null);
            Assert.fail("Exception expected");
        } catch (NullPointerException e) {
            raw = e;
        }

        @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
        NullPointerException cloned = ExceptionUtils.cloneException(raw);

        assert cloned != null;
        Assert.assertEquals(raw.getMessage(), cloned.getMessage());
        Assert.assertArrayEquals(raw.getStackTrace(), cloned.getStackTrace());
        Assert.assertEquals(raw.getClass(), cloned.getClass());
    }

    @Test(timeout = 1000)
    public void testGetUltimateCauseRecursive() {
        Exception recursive = new Exception();
        Exception intermediate = new Exception(recursive);
        recursive.initCause(intermediate);
        Assert.assertNull(ExceptionUtils.getUltimateCause(recursive));
    }

    @Test
    public void testConcatArrays() {
        StackTraceElement s1 = new StackTraceElement("a", "b", "c", 123);
        StackTraceElement s2 = new StackTraceElement("d", "e", "f", 456);
        StackTraceElement[] concatenated =
                ExceptionUtils.concatArrays(
                        new StackTraceElement[]{s1}, new StackTraceElement[]{s2});
        Assert.assertArrayEquals(new StackTraceElement[]{ s1, s2 }, concatenated);
    }
}
