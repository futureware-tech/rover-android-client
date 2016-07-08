package org.dasfoo.rover.android.client.video;

import android.app.Fragment;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
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

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.dasfoo.rover.android.client.R;
import org.dasfoo.rover.android.client.menu.SharedPreferencesHandler;

/**
 * Created by Katarina Sheremet on 6/7/16 12:00 PM.
 * Fragment setups MediaCodec and binds it with Surface.
 */
public class VideoFragment extends Fragment implements TextureView.SurfaceTextureListener,
        View.OnClickListener {

    /**
     * Queue is used for saving NAL units.
     */
    //TODO(ksheremet): create setters and getters and make private
    public static volatile BlockingQueue<byte[]> nalQueue = new LinkedBlockingQueue<>();

    /**
     * Formant for video.
     */
    private static final String VIDEO_FORMAT = "video/avc"; // h.264
    /**
     * Class information for logging.
     */
    private static final String TAG = VideoFragment.class.getName();

    /**
     * Thread for getting NAL units.
     */
    private Thread mVideoThread;

    /**
     * MediaCodec.
     */
    private MediaCodec mMediaCodec;

    @Override
    public final View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                   final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_video, container, false);
        final TextureView textureView = (TextureView) view.findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(this);

        final Button playVideo = (Button) view.findViewById(R.id.start_video_button);
        playVideo.setOnClickListener(this);

        final Button stopVideo = (Button) view.findViewById(R.id.stop_video_button);
        stopVideo.setOnClickListener(this);
        return view;
    }

    @Override
    public final void onSurfaceTextureAvailable(final SurfaceTexture surfaceTexture,
                                                final int width, final int height) {
        Surface surface = new Surface(surfaceTexture);
        try {
            // Surface output format
            MediaFormat format = MediaFormat.createVideoFormat(VIDEO_FORMAT, width, height);
            // Constructor for MediaCodec
            mMediaCodec = MediaCodec.createDecoderByType(VIDEO_FORMAT);
            // Set up Callback for mMediaCodec
            MediaCodecHandler mediaCodecHandler = new MediaCodecHandler(mMediaCodec);
            mMediaCodec = mediaCodecHandler.setupAsynchMediaCodec();
            // Configure mMediaCodec
            mMediaCodec.configure(format, surface, null, 0);
        } catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(final SurfaceTexture surface,
                                            final int width, final int height) {
    }

    @Override
    public final boolean onSurfaceTextureDestroyed(final SurfaceTexture surface) {
        mMediaCodec.stop();
        mMediaCodec.release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(final SurfaceTexture surface) {
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public final void onClick(final View v) {
        switch (v.getId()) {
            case R.id.start_video_button:
                // Start thread reading on server
                try {
                    final SharedPreferencesHandler handler =
                            new SharedPreferencesHandler(getActivity());
                    mVideoThread = new Thread(new VideoDecoderRunnable(handler.getVideoHost(),
                            handler.getVideoPort(), handler.getPassword()));
                    mVideoThread.start();
                    // Start mMediaCodec
                    mMediaCodec.start();
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, e.getMessage());
                    Toast.makeText(getActivity(),
                            "Host and Port for video are empty", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.stop_video_button:
                mVideoThread.interrupt();
                mMediaCodec.stop();
                break;
            default:
                Log.v(TAG, "Button is not implemented yet");
                break;
        }
    }
}
