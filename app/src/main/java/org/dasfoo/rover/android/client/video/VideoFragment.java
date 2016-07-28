package org.dasfoo.rover.android.client.video;

import android.app.Fragment;
import android.media.MediaFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.dasfoo.rover.android.client.BuildConfig;
import org.dasfoo.rover.android.client.R;
import org.dasfoo.rover.android.client.menu.SharedPreferencesHandler;
import org.dasfoo.rover.android.client.util.L;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by Katarina Sheremet on 6/7/16 12:00 PM.
 * Fragment setups MediaCodec and binds it with Surface.
 */
public class VideoFragment extends Fragment implements View.OnClickListener {

    /**
     * Height of video.
     */
    // TODO(ksheremet): take this from settings
    public static final int VIDEO_HEIGHT = 240;

    /**
     * Width of video.
     */
    // TODO(ksheremet): take this from settings
    public static final int VIDEO_WIDTH = 320;

    /**
     * Class information for logging.
     */
    private static final String TAG = L.tagFor(VideoFragment.class);

    /**
     * Format for video.
     */
    private static final String VIDEO_FORMAT = "video/avc"; // h.264

    /**
     * Thread for getting NAL units.
     */
    private Thread mVideoThread;

    /**
     * TextureView on Ui layout.
     */
    private TextureView mTextureView;

    @Override
    public final View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                   final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_video, container, false);
        mTextureView = (TextureView) view.findViewById(R.id.textureView);

        final Button playVideo = (Button) view.findViewById(R.id.start_video_button);
        playVideo.setOnClickListener(this);

        final Button stopVideo = (Button) view.findViewById(R.id.stop_video_button);
        stopVideo.setOnClickListener(this);
        return view;
    }

    /**
     * Called when a button has been clicked.
     *
     * @param v The button that was clicked.
     */
    @Override
    public final void onClick(final View v) {
        switch (v.getId()) {
            case R.id.start_video_button:
                try {
                    final SharedPreferencesHandler handler =
                            new SharedPreferencesHandler(getActivity());
                    startStreamVideo(handler.getHost(),
                            handler.getPort(), handler.getPassword());
                } catch (IllegalArgumentException e) {
                    L.e(TAG, "Empty video settings", e);
                    Toast.makeText(getActivity(),
                            getString(R.string.empty_settings_for_video),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.stop_video_button:
                stopStreamVideo();
                break;
            default:
                L.v(TAG, "Button is not implemented yet");
                break;
        }
    }

    /**
     * Start video streaming in new Thread.
     *
     * @param host     to target server.
     * @param port     to target server.
     * @param password for accessing server.
     */
    private void startStreamVideo(final String host, final int port, final String password) {
        // Create Thread for streaming
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(VIDEO_FORMAT, VIDEO_WIDTH,
                VIDEO_HEIGHT);
        MediaStreamRenderer.Callback mediaStreamCallback = new MediaStreamRenderer.Callback() {

            /**
             * Connection to the server.
             */
            private HttpURLConnection mHttpURLConnection;

            @Override
            public void onBeforeStream(final MediaStreamRenderer streamRenderer) {
                //TODO(ksheremet): UriBuilder
                try {
                    final URL url = new URL("https://" + host + ":" + port);
                    mHttpURLConnection = (HttpURLConnection) url.openConnection();
                    mHttpURLConnection.setRequestProperty("X-Capture-Server-PASSWORD",
                            password);
                    // TODO(ksheremet): Take this from settings
                    mHttpURLConnection.setRequestProperty("X-Capture-Server-WIDTH",
                            String.valueOf(VideoFragment.VIDEO_WIDTH));
                    mHttpURLConnection.setRequestProperty("X-Capture-Server-HEIGHT",
                            String.valueOf(VideoFragment.VIDEO_HEIGHT));
                    streamRenderer.setInputStream(mHttpURLConnection.getInputStream());
                } catch (MalformedURLException e) {
                    if (BuildConfig.DEBUG) {
                        L.e(TAG, "Malformed url", e);
                    }
                    // TODO(ksheremet): do error check and notify user
                } catch (IOException e) {
                    if (BuildConfig.DEBUG) {
                        // Maybe it doesn't look good. But I need a code of error.
                        try {
                            L.e(TAG, String.valueOf(mHttpURLConnection.getResponseCode()));
                            L.e(TAG, mHttpURLConnection.getResponseMessage());
                            L.e(TAG, new Scanner(mHttpURLConnection.getErrorStream())
                                    .useDelimiter("$")
                                    .next());
                        } catch (IOException err) {
                            L.e(TAG, err.getMessage());
                        }
                        L.e(TAG, "Input/Output exception", e);
                    }
                }
            }

            @Override
            public void onAfterStream(final MediaStreamRenderer streamRenderer) {
                mHttpURLConnection.disconnect();
            }
        };

        MediaStreamRenderer mediaStreamRenderer = new MediaStreamRenderer(
                new Surface(mTextureView.getSurfaceTexture()),
                mediaFormat, mediaStreamCallback);
        mVideoThread = new Thread(mediaStreamRenderer);
        mVideoThread.start();
    }

    /**
     * It interrupts thread with streaming processing.
     */
    private void stopStreamVideo() {
        mVideoThread.interrupt();
    }
}

