package com.liato.bankdroid.utils;

import org.junit.Assert;
import org.junit.Test;

import java.net.ConnectException;

import eu.nullbyte.android.urllib.Urllib;
import not.bankdroid.at.all.ExceptionThrower;

@SuppressWarnings("CallToPrintStackTrace")
public class ExceptionUtilsTest {
    @Test
    @SuppressWarnings({"PMD.AvoidPrintStackTrace", "PMD.AvoidCatchingNPE", "PMD.SystemPrintln"})
    public void testBankdroidifyException() throws Exception {
        Exception raw = null;
        try {
            //noinspection ConstantConditions
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

    /**
     * Test that we can wrap exceptions without (String) constructors.
     */
    @Test
    @SuppressWarnings({"PMD.AvoidPrintStackTrace", "PMD.SystemPrintln"})
    public void testBankdroidifyWonkyException() {
        ExceptionThrower.WonkyException raw = null;
        try {
            ExceptionThrower.throwWonkyException();
            Assert.fail("Exception expected");
        } catch (ExceptionThrower.WonkyException e) {
            raw = e;
        }

        // Print stack traces, useful if the tests fail
        System.err.println("Before:");
        raw.printStackTrace();

        // Since bankdroidify() won't be able to create a WonkyException, it
        // should fall back to creating something it extends
        ConnectException bankdroidified = ExceptionUtils.bankdroidifyException(raw);

        System.err.println();
        System.err.println("After:");
        bankdroidified.printStackTrace();

        Assert.assertFalse("Test setup: Top frame of initial exception shouldn't be in Bankdroid",
                raw.getStackTrace()[0].getClassName().startsWith("com.liato.bankdroid."));

        Assert.assertTrue("Top frame of bankdroidified exception should be in Bankdroid",
                bankdroidified.getStackTrace()[0].getClassName().startsWith("com.liato.bankdroid."));

        Assert.assertEquals(raw.getMessage(), bankdroidified.getMessage());

        // Verify that e is the cause of bankdroidified
        Assert.assertSame(raw, bankdroidified.getCause());

        // Verify that re-bankdroidifying is a no-op
        Assert.assertSame(bankdroidified, ExceptionUtils.bankdroidifyException(bankdroidified));
    }
}
