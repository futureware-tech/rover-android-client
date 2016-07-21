package org.dasfoo.rover.android.client.video;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;

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
    private MediaCodec mCodec;

    /**
     * Connection to the server.
     */
    private HttpURLConnection urlConnection;

    /**
     * Surface output format.
     */
    private MediaFormat mediaFormat;

    /**
     * Constructor.
     * Creates MediaCodec and binds it to Surface on UI.
     * @param host to connect to the server
     * @param port to connect to the server
     * @param password to get access to stream
     * @param surface on UI
     * @param format input video
     * @param width of video
     * @param height of video
     */
    // TODO(ksheremet): Pass MediaFormat in parameters, pass InputStream.
    public MediaStreamRenderer(final String host, final int port, final String password,
                               final Surface surface, final String format,
                               final int width,
                               final int height) {
        this.mHost = host;
        this.mPort = port;
        this.mPassword = password;

        try {
            mediaFormat = MediaFormat.createVideoFormat(format, width,
                    height);
            // Constructor for MediaCodec
            mCodec = MediaCodec.createDecoderByType(format);
            // Set up Callback for mMediaCodec
            setupAsyncMediaCodec();
            // Configure mMediaCodec and bind with TextureView
            mCodec.configure(mediaFormat, surface, null, 0);
        } catch (IOException e) {
            Log.e(TAG, "Codec cannot be created", e);
        }
    }

    /**
     * Sets MediaCodec for asynchronously processing.
     */
    public final void setupAsyncMediaCodec() {
        mCodec.setCallback(new MediaCodec.Callback() {
            /**
             * Called when an input buffer becomes available.
             *
             * @param codec The MediaCodec object.
             * @param inputBufferId The index of the available input buffer.
             */
            @Override
            public void onInputBufferAvailable(@NonNull final MediaCodec codec,
                                               final int inputBufferId) {
                try {
                    VideoFragment.setIdBufferInQueue(inputBufferId);
                } catch (InterruptedException e) {
                    // TODO(ksheremet): make a better handling here
                    Log.e(TAG, "User stopped video:", e);
                }
            }

            /**
             * Called when an output buffer becomes available.
             *
             * @param codec The MediaCodec object.
             * @param index The index of the available output buffer.
             * @param info Info regarding the available output buffer {@link MediaCodec.BufferInfo}.
             */
            @Override
            public void onOutputBufferAvailable(@NonNull final MediaCodec codec, final int index,
                                                @NonNull final MediaCodec.BufferInfo info) {
                // If a valid surface was specified when configuring the codec,
                // passing true renders this output buffer to the surface.
                codec.releaseOutputBuffer(index, true);
            }

            /**
             * Called when the MediaCodec encountered an error
             *
             * @param codec The MediaCodec object.
             * @param e The {@link MediaCodec.CodecException} object describing the error.
             */
            @Override
            public void onError(@NonNull final MediaCodec codec,
                                @NonNull final MediaCodec.CodecException e) {
                Log.e(TAG, "Error occurred in MediaCodec", e);
            }

            /**
             * Called when the output format has changed
             *
             * @param codec The MediaCodec object.
             * @param format The new output format.
             */
            @Override
            public void onOutputFormatChanged(@NonNull final MediaCodec codec,
                                              @NonNull final MediaFormat format) {
            }
        });
    }

    /**
     * Creates connection using host and port.
     *
     * @param host to target server
     * @param port to targer server
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
            mCodec.start();
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
