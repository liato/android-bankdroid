package not.bankdroid.at.all;

import java.net.ConnectException;

/**
 * For the test in {@link com.liato.bankdroid.utils.ExceptionUtilsTest}
 */
public class ExceptionThrower {
    public static class WonkyException extends ConnectException {
        public WonkyException(int wonky) {
            super("Wonky: " + wonky);
        }
    }

    public static void throwWonkyException() throws WonkyException {
        throw new WonkyException(5);
    }
}
