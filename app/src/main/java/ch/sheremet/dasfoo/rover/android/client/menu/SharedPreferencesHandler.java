package ch.sheremet.dasfoo.rover.android.client.menu;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.MissingFormatArgumentException;

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
     * Gets host for video.
     *
     * @return host
     * @throws MissingFormatArgumentException if host is empty
     */
    public final String getVideoHost() throws MissingFormatArgumentException {
        return getString(Settings.VIDEO_HOST);
    }

    /**
     * Gets port for video.
     *
     * @return port
     * @throws MissingFormatArgumentException if port is empty
     */
    public final int getVideoPort() throws MissingFormatArgumentException {
        return Integer.parseInt(getString(Settings.VIDEO_PORT));
    }

    /**
     * Gets grpc host.
     *
     * @return host
     * @throws MissingFormatArgumentException if host is empty
     */
    public final String getGrpcHost() throws MissingFormatArgumentException {
        return getString(Settings.GRPC_HOST);
    }

    /**
     * Gets grpc post.
     *
     * @return grpc port
     * @throws MissingFormatArgumentException if post is empty
     */
    public final int getGrpcPort() throws MissingFormatArgumentException {
        return Integer.parseInt(getString(Settings.GRPC_PORT));
    }

    /**
     * Gets password for video.
     *
     * @return password for video
     * @throws MissingFormatArgumentException if password is empty
     */
    public final String getPassword() throws MissingFormatArgumentException {
        return getString(Settings.VIDEO_PASSWORD);
    }

    /**
     * Gets string using key from settings.
     *
     * @param key gets SharedPreferences
     * @return string that saved in settings
     * @throws MissingFormatArgumentException if string is empty
     */
    private String getString(final Settings key) throws MissingFormatArgumentException {
        final SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(mContext);
        final String host = sharedPreferences.getString(key.toString(), "");
        if (TextUtils.isEmpty(host)) {
            throw new MissingFormatArgumentException("Empty string in settings");
        }
        return host;
    }
}
