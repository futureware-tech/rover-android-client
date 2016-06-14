package ch.sheremet.dasfoo.rover.android.client.video;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Katarina Sheremet on 6/8/16 1:07 PM.
 */
public class VideoDecoderRunnable implements Runnable {
    private static final String TAG = VideoDecoderRunnable.class.getSimpleName();
    private String mHost;
    private String mPassword;
    private int mPort;

    private HttpURLConnection mUrlConnection;

    public VideoDecoderRunnable(final String host, final int port, final String password) {
        this.mHost = host;
        this.mPort = port;
        this.mPassword = password;
    }

    private void initServerConnection() {
        try {
            //TODO(ksheremet): UriBuilder
            URL url = new URL("https://" + mHost + ":" + mPort);
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
    //TODO: refactoring and optimisation
    @Override
    public void run() {
        initServerConnection();
        try {
            InputStream inputStream = mUrlConnection.getInputStream();
            int i = 4;
            byte[] buffer = new byte[50];
            int value = inputStream.read(buffer, 0, buffer.length);
            if (value != buffer.length) {
                byte[] newBuffer = new byte[value];
                System.arraycopy(buffer, 0, newBuffer, 0, newBuffer.length);
                buffer = newBuffer;
            }
            while (true) {
                for (; i < buffer.length; i++) {
                    if ((buffer[i] == 1) && (buffer[i - 1] == 0) && (buffer[i - 2] == 0)) {
                        byte[] resultArray = new byte[i - 3];
                        // Extract nal unit
                        System.arraycopy(buffer, 0, resultArray, 0, resultArray.length);
                        // Put nal unit to queue
                        VideoFragment.nalQueue.add(resultArray);
                        System.arraycopy(buffer, resultArray.length,
                                buffer, 0, buffer.length - resultArray.length);
                        // Get new data from video stream to refuel the buffer
                        value = inputStream.read(buffer, buffer.length - resultArray.length,
                                resultArray.length);
                        if (value != resultArray.length) {
                            byte[] newBuffer = new byte[value + (buffer.length - resultArray.length)];
                            System.arraycopy(buffer, 0, newBuffer, 0, newBuffer.length);
                            buffer = newBuffer;
                        }
                        i = 3;
                    }
                    if (Thread.interrupted()) {
                        return;
                    }
                }
                // Not found nal unit in current stream. Need to add more elements
                // Get new stream
                byte[] newBuffer = new byte[buffer.length + 50];
                System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
                value = inputStream.read(newBuffer, buffer.length,
                        newBuffer.length - buffer.length);
                if (value != (newBuffer.length - buffer.length)) {
                    byte[] newNewBuffer = new byte[buffer.length + value];
                    System.arraycopy(newBuffer, 0, newNewBuffer, 0, newNewBuffer.length);
                    newBuffer = newNewBuffer;
                }
                buffer = newBuffer;
            }
        } catch (IOException e) {
            Log.v(TAG, e.toString());
        } finally {
            mUrlConnection.disconnect();
        }
    }
}
