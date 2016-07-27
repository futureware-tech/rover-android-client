package org.dasfoo.rover.android.client.util;

import android.util.Log;

/**
 * Log wrapper. To disable logging L.isLogEnable = false; To set min log level,
 * when log enabled set L.logLevel = Log.VERBOSE;
 */
public class L {
    /**
     * It enables/disables logs.
     */
    public static boolean isLogEnable = false;

    public static int logLevel = Log.VERBOSE;

    /**
     * Send a VERBOSE log message.
     *
     * @param tag    Used to identify the source of a log message
     * @param string log message
     */
    public static void v(String tag, String string) {
        if (isLogEnable && logLevel <= Log.VERBOSE) {
            if (string == null) {
                string = "null";
            }

            Log.v(tag, string);
        }
    }

    /**
     * Send a DEBUG log message.
     *
     * @param tag    Used to identify the source of a log message
     * @param string log message
     */
    public static void d(String tag, String string) {
        if (isLogEnable && logLevel <= Log.DEBUG) {
            if (string == null) {
                string = "null";
            }
            Log.d(tag, string);
        }
    }

    /**
     * Send an INFO log message.
     *
     * @param tag    Used to identify the source of a log message
     * @param string log message
     */
    public static void i(String tag, String string) {
        if (isLogEnable && logLevel <= Log.INFO) {
            if (string == null) {
                string = "null";
            }
            Log.i(tag, string);
        }
    }

    /**
     * Send a WARN log message.
     *
     * @param tag    Used to identify the source of a log message
     * @param string log message
     */
    public static void w(String tag, String string) {
        if (isLogEnable && logLevel <= Log.WARN) {
            if (string == null) {
                string = "null";
            }
            Log.w(tag, string);
        }
    }

    /**
     * Send an ERROR log message.
     *
     * @param tag    Used to identify the source of a log message
     * @param string log message
     */
    public static void e(String tag, String string) {
        if (isLogEnable && logLevel <= Log.ERROR) {
            if (string == null) {
                string = "null";
            }
            Log.e(tag, string);
        }
    }

    /**
     * Send a WARN and log the exception.
     *
     * @param tag Used to identify the source of a log message
     * @param tr  exception
     */
    public static void w(String tag, Throwable tr) {
        if (isLogEnable && logLevel <= Log.WARN) {
            if (tr == null) {
                tr = new Throwable("Null Throwable set to log");
            }
            Log.w(tag, tr);
        }
    }

    /**
     * Send a VERBOSE log message and log the exception.
     *
     * @param tag    Used to identify the source of a log message
     * @param string log message
     * @param tr     exception
     */
    public static void v(String tag, String string, Throwable tr) {
        if (isLogEnable && logLevel <= Log.VERBOSE) {
            if (string == null) {
                string = "null";
            }
            if (tr == null) {
                tr = new Throwable("Null Throwable set to log");
            }
            Log.v(tag, string, tr);
        }
    }

    /**
     * Send a DEBUG log message and log the exception.
     *
     * @param tag    Used to identify the source of a log message
     * @param string log message
     * @param tr     exception
     */
    public static void d(String tag, String string, Throwable tr) {
        if (isLogEnable && logLevel <= Log.DEBUG) {
            if (string == null) {
                string = "null";
            }
            if (tr == null) {
                tr = new Throwable("Null Throwable set to log");
            }
            Log.d(tag, string, tr);
        }
    }

    /**
     * Send a INFO log message and log the exception.
     *
     * @param tag    Used to identify the source of a log message
     * @param string log message
     * @param tr     exception
     */
    public static void i(String tag, String string, Throwable tr) {
        if (isLogEnable && logLevel <= Log.INFO) {
            if (string == null) {
                string = "null";
            }
            if (tr == null) {
                tr = new Throwable("Null Throwable set to log");
            }
            Log.i(tag, string, tr);
        }
    }

    /**
     * Send a WARN log message and log the exception.
     *
     * @param tag    Used to identify the source of a log message
     * @param string log message
     * @param tr     exception
     */
    public static void w(String tag, String string, Throwable tr) {
        if (isLogEnable && logLevel <= Log.WARN) {
            if (string == null) {
                string = "null";
            }
            if (tr == null) {
                tr = new Throwable("Null Throwable set to log");
            }
            Log.w(tag, string, tr);
        }
    }

    /**
     * Send a ERROR log message and log the exception.
     *
     * @param tag    Used to identify the source of a log message
     * @param string log message
     * @param tr     exception
     */
    public static void e(String tag, String string, Throwable tr) {
        if (isLogEnable && logLevel <= Log.ERROR) {
            if (string == null) {
                string = "null";
            }
            if (tr == null) {
                tr = new Throwable("Null Throwable set to log");
            }
            Log.e(tag, string, tr);
        }
    }
}
