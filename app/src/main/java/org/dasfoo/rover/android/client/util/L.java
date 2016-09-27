package org.dasfoo.rover.android.client.util;

import android.util.Log;

/**
 * Log wrapper. To disable logging L.isLogEnabled = false; To set min log level,
 * when log enabled set L.logLevel = Log.VERBOSE;
 */
public final class L {
    /**
     * Level of logs.
     */
    private static int logLevel = Log.VERBOSE;

    /**
     * Prevents the default parameter-less constructor from being used.
     */
    private L() {
        // Utility classes should not have a public or default constructor
    }

    /**
     * Sets level of logs. By default it is Log.VERBOSE.
     *
     * @param logLevel lever of logs
     */
    public static void setLogLevel(int logLevel) {
        L.logLevel = logLevel;
    }

    /**
     * It checks String on Null.
     * If it is null, create String with "null" value.
     *
     * @param s log message
     * @return String with log message or "null"
     */
    private static String stringOrNull(final String s) {
        if (s == null) {
            return "null";
        }
        return s;
    }

    /**
     * It checks Throwable on Null.
     * If it is null, create Throwable to notify that variable is null.
     *
     * @param tr exception
     * @return exception
     */
    private static Throwable throwableOrNull(final Throwable tr) {
        if (tr == null) {
            return new Throwable("Null Throwable set to log");
        }
        return tr;
    }

    /**
     * Extract class information for logger.
     *
     * @param c class for logging
     * @return string of class information for logger.
     */
    public static String tagFor(final Class c) {
        return c.getSimpleName();
    }

    /**
     * Send a VERBOSE log message.
     *
     * @param tag    Used to identify the source of a log message
     * @param string log message
     */
    public static void v(final String tag, final String string) {
        if (logLevel <= Log.VERBOSE) {
            Log.v(tag, stringOrNull(string));
        }
    }

    /**
     * Send a VERBOSE log message and log the exception.
     *
     * @param tag    Used to identify the source of a log message
     * @param string log message
     * @param tr     exception
     */
    public static void v(final String tag, final String string, final Throwable tr) {
        if (logLevel <= Log.VERBOSE) {
            Log.v(tag, stringOrNull(string), throwableOrNull(tr));
        }
    }

    /**
     * Send a DEBUG log message.
     *
     * @param tag    Used to identify the source of a log message
     * @param string log message
     */
    public static void d(final String tag, final String string) {
        if (logLevel <= Log.DEBUG) {
            Log.d(tag, stringOrNull(string));
        }
    }

    /**
     * Send a DEBUG log message and log the exception.
     *
     * @param tag    Used to identify the source of a log message
     * @param string log message
     * @param tr     exception
     */
    public static void d(final String tag, final String string, final Throwable tr) {
        if (logLevel <= Log.DEBUG) {
            Log.d(tag, stringOrNull(string), throwableOrNull(tr));
        }
    }

    /**
     * Send an INFO log message.
     *
     * @param tag    Used to identify the source of a log message
     * @param string log message
     */
    public static void i(final String tag, final String string) {
        if (logLevel <= Log.INFO) {
            Log.i(tag, stringOrNull(string));
        }
    }

    /**
     * Send a INFO log message and log the exception.
     *
     * @param tag    Used to identify the source of a log message
     * @param string log message
     * @param tr     exception
     */
    public static void i(final String tag, final String string, final Throwable tr) {
        if (logLevel <= Log.INFO) {
            Log.i(tag, stringOrNull(string), throwableOrNull(tr));
        }
    }

    /**
     * Send a WARN log message.
     *
     * @param tag    Used to identify the source of a log message
     * @param string log message
     */
    public static void w(final String tag, final String string) {
        if (logLevel <= Log.WARN) {
            Log.w(tag, stringOrNull(string));
        }
    }

    /**
     * Send an ERROR log message.
     *
     * @param tag    Used to identify the source of a log message
     * @param string log message
     */
    public static void e(final String tag, final String string) {
        if (logLevel <= Log.ERROR) {
            Log.e(tag, stringOrNull(string));
        }
    }

    /**
     * Send a ERROR log message and log the exception.
     *
     * @param tag    Used to identify the source of a log message
     * @param string log message
     * @param tr     exception
     */
    public static void e(final String tag, final String string, final Throwable tr) {
        if (logLevel <= Log.ERROR) {
            Log.e(tag, stringOrNull(string), throwableOrNull(tr));
        }
    }

    /**
     * Send a WARN and log the exception.
     *
     * @param tag Used to identify the source of a log message
     * @param tr  exception
     */
    public static void w(final String tag, final Throwable tr) {
        if (logLevel <= Log.WARN) {
            Log.w(tag, throwableOrNull(tr));
        }
    }

    /**
     * Send a WARN log message and log the exception.
     *
     * @param tag    Used to identify the source of a log message
     * @param string log message
     * @param tr     exception
     */
    public static void w(final String tag, final String string, final Throwable tr) {
        if (logLevel <= Log.WARN) {
            Log.w(tag, stringOrNull(string), throwableOrNull(tr));
        }
    }
}
