package org.dasfoo.rover.android.client.menu;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

/**
 * Created by Katarina Sheremet on 6/18/16 4:01 PM.
 * <p/>
 * Class is for managing SharedPreferences to get host, port and password.
 */
public class SharedPreferencesHandler {

    /**
     * Context for getting shared preferences.
     */
    private final Context mContext;

    /**
     * Constructor.
     *
     * @param context context
     */
    public SharedPreferencesHandler(final Context context) {
        this.mContext = context;
    }

    /**
     * Gets host of server from settings.
     *
     * @return host
     * @throws IllegalArgumentException if host is empty
     */
    public final String getHost() throws IllegalArgumentException {
        return getString(Settings.HOST);
    }

    /**
     * Gets port of server from settings.
     *
     * @return port
     * @throws IllegalArgumentException if port is empty
     */
    public final int getPort() throws IllegalArgumentException {
        return Integer.parseInt(getString(Settings.PORT));
    }

    /**
     * Gets password for video.
     *
     * @return password for video
     * @throws IllegalArgumentException if password is empty
     */
    public final String getPassword() throws IllegalArgumentException {
        return getString(Settings.PASSWORD);
    }

    /**
     * Gets string using key from settings.
     *
     * @param key gets SharedPreferences
     * @return string that saved in settings
     * @throws IllegalArgumentException if string is empty
     */
    private String getString(final Settings key) throws IllegalArgumentException {
        final SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(mContext);
        final String host = sharedPreferences.getString(key.toString(), "");
        if (TextUtils.isEmpty(host)) {
            throw new IllegalArgumentException("Empty string in settings");
        }
        return host;
    }
}
