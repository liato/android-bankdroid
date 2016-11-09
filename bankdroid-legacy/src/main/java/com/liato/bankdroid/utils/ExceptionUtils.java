package com.liato.bankdroid.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import timber.log.Timber;

public class ExceptionUtils {
    private static final String PREFIX = "com.liato.bankdroid.";

    /**
     * Modify an Exception to make it look like it was ultimately caused by Bankdroid.
     * <p/>
     * The purpose is to make Crashlytics report Urllib exceptions as coming from whatever
     * bank Urllib is trying to access.
     * <p/>
     * For example, this exception:
     * <pre>
     * java.lang.Exception: This is a test Exception
     *     at not.bankdroid.at.all.ExceptionFactory.getException(ExceptionFactory.java:20)
     *     at com.liato.bankdroid.utils.ExceptionUtilsTest.testBlameBankdroid(ExceptionUtilsTest.java:16)
     *     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
     *     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
     *     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
     *     at java.lang.reflect.Method.invoke(Method.java:497)
     *     at ...
     * </pre>
     *
     * Would be turned into this exception:
     * <pre>
     * java.lang.Exception: This is a test Exception
     *     at not.bankdroid.at.all.ExceptionFactory.getException(ExceptionFactory.java:20)
     *     at com.liato.bankdroid.utils.ExceptionUtilsTest.testBlameBankdroid(ExceptionUtilsTest.java:16)
     *     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
     *     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
     *     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
     *     at java.lang.reflect.Method.invoke(Method.java:497)
     *     at ...
     * Caused by: java.lang.Exception: This is a test Exception
     *     at com.liato.bankdroid.utils.ExceptionUtilsTest.testBlameBankdroid(ExceptionUtilsTest.java:16)
     *     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
     *     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
     *     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
     *     at java.lang.reflect.Method.invoke(Method.java:497)
     *     at ...
     *     ... 37 more
     * </pre>
     */
    public static void blameBankdroid(Throwable exception) {
        Throwable ultimateCause = getUltimateCause(exception);
        if (ultimateCause == null) {
            // Unable to find ultimate cause, never mind
            return;
        }

        StackTraceElement[] bankdroidifiedStacktrace =
                bankdroidifyStacktrace(ultimateCause.getStackTrace());
        if (bankdroidifiedStacktrace.length == 0) {
            // No Bankdroid stack frames found, never mind
            return;
        }
        if (bankdroidifiedStacktrace.length == ultimateCause.getStackTrace().length) {
            // Bankdroid already to blame, never mind
            return;
        }

        Throwable fakeCause = cloneException(ultimateCause);
        if (fakeCause == null) {
            Timber.w(new RuntimeException(
                    "Unable to bankdroidify exception of class: " + ultimateCause.getClass()));
            return;
        }

        // Put the bankdroidified stack trace before the fakeCause's actual stack trace
        fakeCause.setStackTrace(concatArrays(bankdroidifiedStacktrace, fakeCause.getStackTrace()));

        ultimateCause.initCause(fakeCause);
    }

    @VisibleForTesting
    static StackTraceElement[] concatArrays(StackTraceElement[] a, StackTraceElement[] b) {
        StackTraceElement[] returnMe = new StackTraceElement[a.length + b.length];
        System.arraycopy(a, 0, returnMe, 0, a.length);
        System.arraycopy(b, 0, returnMe, a.length, b.length);
        return returnMe;
    }

    @VisibleForTesting
    @Nullable
    static Throwable getUltimateCause(Throwable t) {
        int laps = 0;
        Throwable ultimateCause = t;
        while (ultimateCause.getCause() != null) {
            ultimateCause = ultimateCause.getCause();
            if (laps++ > 10) {
                return null;
            }
        }
        return ultimateCause;
    }

    /**
     * Clone message and stacktrace but not the cause.
     */
    @Nullable
    @VisibleForTesting
    static <T extends Throwable> T cloneException(T wrapMe) {
        Class<?> newClass = wrapMe.getClass();
        while (newClass != null) {
            try {
                T returnMe =
                        (T) newClass.getConstructor(String.class).newInstance(wrapMe.getMessage());
                returnMe.setStackTrace(wrapMe.getStackTrace());
                return returnMe;
            } catch (InvocationTargetException e) {
                newClass = newClass.getSuperclass();
            } catch (NoSuchMethodException e) {
                newClass = newClass.getSuperclass();
            } catch (InstantiationException e) {
                newClass = newClass.getSuperclass();
            } catch (IllegalAccessException e) {
                newClass = newClass.getSuperclass();
            }
        }

        return null;
    }

    /**
     * Remove all initial non-Bankdroid frames from a stack.
     *
     * @return A copy of rawStack but with the initial non-Bankdroid frames removed, or null
     * if no sensible answer can be given.
     */
    @VisibleForTesting
    @NonNull
    static StackTraceElement[] bankdroidifyStacktrace(final StackTraceElement[] rawStack) {
        for (int i = 0; i < rawStack.length; i++) {
            StackTraceElement stackTraceElement = rawStack[i];
            if (stackTraceElement.getClassName().startsWith(PREFIX)) {
                return Arrays.copyOfRange(rawStack, i, rawStack.length);
            }
        }

        // No Bankdroid stack frames found, never mind
        return new StackTraceElement[0];
    }
}
