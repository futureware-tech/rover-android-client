package ch.sheremet.dasfoo.rover.android.client.property;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Katarina Sheremet on 6/10/16 2:48 PM.
 */

/** This class is created for temporary purpose. It will be deleted
 * after development menu with settings.
 * TODO(ksheremet): Remove it
 */
public class PropertyReader {
    private static final String TAG = PropertyReader.class.getSimpleName();
    private Context mContext;
    private Properties mProperties;

    public PropertyReader(final Context context) {
        this.mContext = context;
        mProperties = new Properties();
    }

    public final Properties getProperties(final String file) {
        try {
            AssetManager assetManager = mContext.getAssets();
            InputStream inputStream = assetManager.open(file);
            mProperties.load(inputStream);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return mProperties;
    }
}
