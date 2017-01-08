package org.dasfoo.rover.android.client.menu;

/**
 * Created by Katarina Sheremet on 6/17/16 11:21 AM.
 * <p>
 * This class is used for saving settings keys.
 */
public enum Settings {
    /**
     * Host is for connecting to video.
     */
    HOST("pref_host"),
    /**
     * Port is for connecting to video.
     */
    PORT("pref_port"),
    /**
     * User-selected account name.
     */
    ACCOUNT_NAME("pref_account_name");

    /**
     * Constructor variable.
     */
    private final String mName;

    /**
     * Constructor.
     *
     * @param s string parameter for constructor
     */
    Settings(final String s) {
        mName = s;
    }

    /**
     * Stringifier.
     *
     * @return string representation of enum element
     */
    public String toString() {
        return this.mName;
    }
}
