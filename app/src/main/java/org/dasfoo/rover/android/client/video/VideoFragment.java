package org.dasfoo.rover.android.client.video;

import android.app.Fragment;
import android.media.MediaFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.dasfoo.rover.android.client.R;
import org.dasfoo.rover.android.client.menu.SharedPreferencesHandler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Katarina Sheremet on 6/7/16 12:00 PM.
 * Fragment setups MediaCodec and binds it with Surface.
 */
public class VideoFragment extends Fragment implements View.OnClickListener {

    /**
     * Queue is used for saving index of input buffer.
     */
    private static BlockingQueue<Integer> idBufferQueue = new LinkedBlockingQueue<>();

    /**
     * Class information for logging.
     */
    private static final String TAG = VideoFragment.class.getName();

    /**
     * Format for video.
     */
    private static final String VIDEO_FORMAT = "video/avc"; // h.264

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
     * Thread for getting NAL units.
     */
    private Thread mVideoThread;

    /**
     * TextureView.
     */
    private TextureView textureView;

    @Override
    public final View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                   final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_video, container, false);
        textureView = (TextureView) view.findViewById(R.id.textureView);

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
                    startStreamVideo(handler.getVideoHost(),
                            handler.getVideoPort(), handler.getPassword());
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Empty video settings", e);
                    Toast.makeText(getActivity(),
                            getString(R.string.empty_settings_for_video), Toast.LENGTH_SHORT).show();
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
     * Setters for idBufferQueue.
     * Inserts the specified element at the tail of this queue,
     * waiting if necessary for space to become available.
     *
     * @param id index of InputBuffer
     * @throws InterruptedException if interrupted while waiting
     */
    public static void setIdBufferInQueue(final Integer id) throws InterruptedException {
        idBufferQueue.put(id);
    }

    /**
     * Getters for idBufferQueue.
     * Retrieves and removes the head of this queue,
     * waiting if necessary until an element becomes available.
     *
     * @return head element of this queue
     * @throws InterruptedException if interrupted while waiting
     */
    public static Integer getIdBufferFromQueue() throws InterruptedException {
        return idBufferQueue.take();
    }

    /**
     * Atomically removes all of the elements from this queue.
     * The queue will be empty after this call returns.
     */
    public static void clearIdBufferQueue() {
        idBufferQueue.clear();
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
        mVideoThread = new Thread(
                new MediaStreamRenderer(host, port, password,
                        new Surface(textureView.getSurfaceTexture()),
                mediaFormat));
        mVideoThread.start();
    }

    /**
     * Stop video streaming.
     */
    private void stopStreamVideo() {
        mVideoThread.interrupt();
    }
}

