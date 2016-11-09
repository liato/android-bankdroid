package not.bankdroid.at.all;

import java.net.ConnectException;

/**
 * For the test in {@link com.liato.bankdroid.utils.ExceptionUtilsTest}
 */
public class ExceptionFactory {
    public static class WonkyException extends ConnectException {
        public WonkyException(int wonky) {
            super("Wonky: " + wonky);
        }
    }

    public static WonkyException getWonkyException() {
        return new WonkyException(5);
    }

    public static Exception getException() {
        return new Exception("This is a test Exception");
    }

    public static Exception getExceptionWithCause() {
        return new Exception("This Exception has a cause", getException());
    }
}
