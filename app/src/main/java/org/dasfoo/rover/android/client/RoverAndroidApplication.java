package org.dasfoo.rover.android.client;

import android.app.Application;

import org.dasfoo.rover.android.client.util.L;

/**
 * Created by Katarina Sheremet on 7/27/16 9:36 AM.
 */
public class RoverAndroidApplication extends Application {
    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     * Implementations should be as quick as possible (for example using
     * lazy initialization of state) since the time spent in this function
     * directly impacts the performance of starting the first activity,
     * service, or receiver in a process.
     * If you override this method, be sure to call super.onCreate().
     */
    @Override
    public void onCreate() {
        super.onCreate();
        L.isLogEnable = getResources().getBoolean(R.bool.log_enable);
    }
}
