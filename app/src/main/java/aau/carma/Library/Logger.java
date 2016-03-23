package aau.carma.Library;

import android.util.Log;

import aau.carma.Configuration;

/**
 * Logs messages if it is enabled in Configuration.java.
 */
public class Logger {
    /**
     * Tag to use for logging.
     */
    private static String Tag = Configuration.Log;

    /**
     * Logs a message with verbose severity.
     * @param message Message to log.
     */
    public static void verbose(String message) {
        if (isEnabled()) {
            Log.v(Tag, message);
        }
    }

    /**
     * Logs a message with warning severity.
     * @param message Message to log.
     */
    public static void warning(String message) {
        if (isEnabled()) {
            Log.w(Tag, message);
        }
    }

    /**
     * Logs a message with error severity.
     * @param message Message to log.
     */
    public static void error(String message) {
        if (isEnabled()) {
            Log.e(Tag, message);
        }
    }

    /**
     * Checks if logging is enabled.
     * @return Whether or not logging is enabled.
     */
    public static boolean isEnabled() {
        return Configuration.LogEnabled;
    }
}
