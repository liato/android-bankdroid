package com.liato.bankdroid.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import timber.log.Timber;

public class ExceptionUtils {
    private static final String PREFIX = "com.liato.bankdroid.";

    /**
     * Take an exception thrown and make it look like it came from Bankdroid.
     * <p/>
     * Specifically, if Urllib.java, called by Bankdroid code, throws an exception,
     * rewrite the exception so that it appears as if it was thrown from the
     * Bankdroid method calling Urllib, but caused by the original Exception.
     */
    public static <T extends Throwable> T bankdroidifyException(T exception) {
        StackTraceElement[] bankdroidifiedStacktrace =
                bankdroidifyStacktrace(exception.getStackTrace());
        if (bankdroidifiedStacktrace.length == exception.getStackTrace().length) {
            // Unable to bankdroidify stacktrace, never mind
            return exception;
        }

        T returnMe;
        try {
            returnMe = (T)exception.getClass().getConstructor(String.class)
                    .newInstance(exception.getMessage());
        } catch (InstantiationException e) {
            Timber.e(e, "Unable to Bankdroidify exception of type %s", exception.getClass());
            return exception;
        } catch (InvocationTargetException e) {
            Timber.e(e, "Unable to Bankdroidify exception of type %s", exception.getClass());
            return exception;
        } catch (IllegalAccessException e) {
            Timber.e(e, "Unable to Bankdroidify exception of type %s", exception.getClass());
            return exception;
        } catch (NoSuchMethodException e) {
            Timber.e(e, "Unable to Bankdroidify exception of type %s", exception.getClass());
            return exception;
        }

        returnMe.initCause(exception);

        returnMe.setStackTrace(bankdroidifiedStacktrace);

        return returnMe;
    }

    /**
     * Remove all initial non-Bankdroid frames from a stack.
     *
     * @return A copy of rawStack but with the initial non-Bankdroid frames removed
     */
    private static StackTraceElement[] bankdroidifyStacktrace(final StackTraceElement[] rawStack) {
        for (int i = 0; i < rawStack.length; i++) {
            StackTraceElement stackTraceElement = rawStack[i];
            if (stackTraceElement.getClassName().startsWith(PREFIX)) {
                return Arrays.copyOfRange(rawStack, i, rawStack.length);
            }
        }

        // No Bankdroid stack frames found, never mind
        return rawStack;
    }
}
