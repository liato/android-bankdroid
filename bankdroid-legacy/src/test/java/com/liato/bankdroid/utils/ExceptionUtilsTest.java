package com.liato.bankdroid.utils;

import org.junit.Assert;
import org.junit.Test;

import eu.nullbyte.android.urllib.Urllib;

public class ExceptionUtilsTest {
    @Test
    @SuppressWarnings("PMD") // This is for the stack trace printing, we really want to do it here
    public void bankdroidifyException() throws Exception {
        Exception raw = null;
        try {
            new Urllib(null);
            Assert.fail("Exception expected");
        } catch (NullPointerException e) {
            raw = e;
        }

        // Print stack traces, useful if the tests fail
        System.err.println("Before:");
        raw.printStackTrace();

        System.err.println();
        System.err.println("After:");
        Exception bankdroidified = ExceptionUtils.bankdroidifyException(raw);
        bankdroidified.printStackTrace();

        Assert.assertFalse("Test setup: Top frame of initial exception shouldn't be in Bankdroid",
                raw.getStackTrace()[0].getClassName().startsWith("com.liato.bankdroid."));

        Assert.assertTrue("Top frame of bankdroidified exception should be in Bankdroid",
                bankdroidified.getStackTrace()[0].getClassName().startsWith("com.liato.bankdroid."));

        // Verify that e is the cause of bankdroidified
        Assert.assertSame(raw, bankdroidified.getCause());

        // Verify that re-bankdroidifying is a no-op
        Assert.assertSame(bankdroidified, ExceptionUtils.bankdroidifyException(bankdroidified));
    }
}
