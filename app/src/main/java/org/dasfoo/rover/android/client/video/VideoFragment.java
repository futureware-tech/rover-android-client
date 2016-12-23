package org.dasfoo.rover.android.client.video;

import android.app.Fragment;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import org.dasfoo.android.h264videostream.MediaStreamRenderer;
import org.dasfoo.rover.android.client.R;
import org.dasfoo.rover.android.client.menu.SharedPreferencesHandler;
import org.dasfoo.rover.android.client.util.LogUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import butterknife.BindView;
import butterknife.OnClick;

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
    private static final String TAG = LogUtil.tagFor(VideoFragment.class);

    /**
     * Format for video.
     */
    private static final String VIDEO_FORMAT = "video/avc"; // h.264

    /**
     * TextureView on Ui layout.
     */
    @BindView(R.id.textureView)
    protected TextureView mTextureView;

    /**
     * Thread for getting NAL units.
     */
    private Thread mVideoThread;

    /**
     * Called when a button has been clicked.
     *
     * @param v The button that was clicked.
     */
    @OnClick({ R.id.start_video_button, R.id.stop_video_button })
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
                    Log.e(TAG, "Empty video settings", e);
                    Toast.makeText(getActivity(),
                            getString(R.string.empty_settings_for_video),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.stop_video_button:
                stopStreamVideo();
                break;
            default:
                Log.v(TAG, "Button is not implemented yet");
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
                    Log.e(TAG, "Malformed url", e);
                    // TODO(ksheremet): do error check and notify user
                } catch (IOException e) {
                    // Maybe it doesn't look good. But I need a code of error.
                    try {
                        Log.e(TAG, String.valueOf(mHttpURLConnection.getResponseCode()));
                        Log.e(TAG, mHttpURLConnection.getResponseMessage());
                        Log.e(TAG, new Scanner(mHttpURLConnection.getErrorStream())
                                .useDelimiter("$")
                                .next());
                    } catch (IOException err) {
                        Log.e(TAG, err.getMessage());
                    }
                    Log.e(TAG, "Input/Output exception", e);
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

