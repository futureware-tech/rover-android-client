package org.dasfoo.rover.android.client.video;

import android.media.MediaCodec;
import android.util.Log;

import org.dasfoo.rover.android.client.BuildConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;

/**
 * Created by Katarina Sheremet on 6/8/16 1:07 PM.
 */
public class VideoDecoderRunnable implements Runnable {
    private static final String TAG = VideoDecoderRunnable.class.getSimpleName();
    private final String mHost;
    private final String mPassword;
    private final int mPort;
    private final MediaCodec mCodec;

    private HttpURLConnection mUrlConnection;

    public VideoDecoderRunnable(final String host, final int port, final String password,
                                final MediaCodec codec) {
        this.mHost = host;
        this.mPort = port;
        this.mPassword = password;
        this.mCodec = codec;
    }

    private void initServerConnection() {
        try {
            //TODO(ksheremet): UriBuilder
            final URL url = new URL("https://" + mHost + ":" + mPort);
            mUrlConnection = (HttpURLConnection) url.openConnection();
            mUrlConnection.setRequestProperty("X-Capture-Server-PASSWORD",
                    mPassword);
            // TODO(ksheremet): Change width and height
            mUrlConnection.setRequestProperty("X-Capture-Server-WIDTH", "320");
            mUrlConnection.setRequestProperty("X-Capture-Server-HEIGHT", "240");
            // TODO(ksheremet): FPS
        } catch (MalformedURLException e) {
            Log.e(TAG, e.toString());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Starts executing the active part of the class' code. This method is
     * called when a thread is started that has been created with a class which
     * implements {@code Runnable}.
     */
    // TODO(ksheremet): refactoring and optimisation
    @Override
    public void run() {
        initServerConnection();
        try {
            InputStream inputStream = mUrlConnection.getInputStream();
            StreamParser p = new StreamParser(inputStream);
            while (!Thread.currentThread().isInterrupted()) {
                int id = VideoFragment.getIdBufferFromQueue();
                ByteBuffer inputBuffer = this.mCodec.getInputBuffer(id);
                int size = p.takeUnit(inputBuffer);
                this.mCodec.queueInputBuffer(id, 0, size, 0, 0);
            }
        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                // TODO(ksheremet): remove .toString() from all throwables in Log()
                Log.e(TAG, "Cannot parse stream:", e);
            }
        } catch (InterruptedException e) {
           // TODO(ksheremet):
            if (BuildConfig.DEBUG) {
                Log.v(TAG, "User stopped stream", e);
            }
        } finally {
            mUrlConnection.disconnect();
        }
    }
}
