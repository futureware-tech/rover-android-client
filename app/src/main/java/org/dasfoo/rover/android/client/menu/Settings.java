package org.dasfoo.rover.android.client.menu;

/**
 * Created by Katarina Sheremet on 6/17/16 11:21 AM.
 *
 * This class is used for saving settings keys.
 */
public enum Settings {
    /**
     * Host is for connecting to video.
     */
    VIDEO_HOST("pref_video_host"),
    /**
     * Port is for connecting to video.
     */
    VIDEO_PORT("pref_video_port"),
    /**
     * Password is for server to get video.
     */
    VIDEO_PASSWORD("pref_video_password"),
    /**
     * Host is for getting devices information from server.
     */
    GRPC_HOST("pref_grpc_host"),
    /**
     * Port is for getting devices information from server.
     */
    GRPC_PORT("pref_grpc_port");

    /**
     * Constructor variable.
     */
    private final String name;

    /**
     * @param s string parameter for constructor
     */
    Settings(final String s) {
        name = s;
    }

    /**
     * @return string representation of enum element
     */
    public String toString() {
        return this.name;
    }
}
