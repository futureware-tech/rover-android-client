package org.dasfoo.rover.android.client.video;

import android.media.MediaCodec;
import android.util.Log;

import org.dasfoo.rover.android.client.BuildConfig;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.MalformedInputException;

/**
 * Created by Katarina Sheremet on 6/8/16 1:07 PM.
 * <p/>
 * Class is used for creating connection to the server to get video and do
 * processing of stream.
 */
public class MediaStreamRenderer implements Runnable {

    /**
     * Class information for logging.
     */
    private static final String TAG = MediaStreamRenderer.class.getSimpleName();

    /**
     * Host for connection to the server.
     */
    private final String mHost;

    /**
     * Password for connection to the server.
     */
    private final String mPassword;

    /**
     * Port for connection to the server.
     */
    private final int mPort;

    /**
     * MediaCodec.
     */
    private final MediaCodec mCodec;

    /**
     * Connection to the server.
     */
    private HttpURLConnection urlConnection;


    /**
     * Default constructor.
     *
     * @param host     for connecting to the server
     * @param port     for connecting to the server
     * @param password to get access to the video
     * @param codec    for processing stream
     */
    public MediaStreamRenderer(final String host, final int port, final String password,
                               final MediaCodec codec) {
        this.mHost = host;
        this.mPort = port;
        this.mPassword = password;
        this.mCodec = codec;
    }

    /**
     * Creates connection using host and port.
     *
     * @param host to target server
     * @param port to targen server
     * @return connection to the server
     * @throws IOException when Malformed host or port. Or input/output exception.
     */
    private HttpURLConnection createConnection(final String host, final int port)
            throws IOException {
        HttpURLConnection mUrlConnection;
        //TODO(ksheremet): UriBuilder
        final URL url = new URL("https://" + host + ":" + port);
        mUrlConnection = (HttpURLConnection) url.openConnection();
        Log.v(TAG, mUrlConnection.getClass().getSimpleName());
        return mUrlConnection;
    }

    /**
     * Sets up properties for video. Width, height, etc.
     */
    private void setUpVideoProperties() {
        urlConnection.setRequestProperty("X-Capture-Server-PASSWORD", mPassword);
        // TODO(ksheremet): Take this from settings
        urlConnection.setRequestProperty("X-Capture-Server-WIDTH",
                String.valueOf(VideoFragment.VIDEO_WIDTH));
        urlConnection.setRequestProperty("X-Capture-Server-HEIGHT",
                String.valueOf(VideoFragment.VIDEO_HEIGHT));
    }

    /**
     * Starts executing the active part of the class' code. This method is
     * called when a thread is started that has been created with a class which
     * implements {@code Runnable}.
     */
    @Override
    public final void run() {
        try {
            urlConnection = createConnection(mHost, mPort);
            setUpVideoProperties();
            InputStream inputStream = urlConnection.getInputStream();
            StreamParser streamParser = new StreamParser(inputStream);
            while (!Thread.currentThread().isInterrupted()) {
                int id = VideoFragment.getIdBufferFromQueue();
                ByteBuffer inputBuffer = this.mCodec.getInputBuffer(id);
                int size = streamParser.takeUnit(inputBuffer);
                this.mCodec.queueInputBuffer(id, 0, size, 0, 0);
            }
        } catch (MalformedInputException e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Malformed url:", e);
            }
        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                // TODO(ksheremet): remove .toString() from all throwables in Log()
                Log.e(TAG, "Cannot parse stream:", e);
            }
        } catch (InterruptedException e) {
            if (BuildConfig.DEBUG) {
                Log.v(TAG, "User stopped stream", e);
            }
        } finally {
            urlConnection.disconnect();
            mCodec.stop();
            // Release codec
            mCodec.release();
            VideoFragment.clearIdBufferQueue();
        }
    }
}
