package org.dasfoo.rover.android.client.util;

/**
 * Helper methods for Android Logging.
 */
public final class LogUtil {
    /**
     * Hide utility class default constructor.
     */
    private LogUtil() {
    }

    /**
     * Class information for logging.
     *
     * @param c class type to generate a log tag for
     * @return class-scoped string to be used as a tag for Log calls in this clas
     */
    public static String tagFor(final Class c) {
        return c.getSimpleName();
    }
}
