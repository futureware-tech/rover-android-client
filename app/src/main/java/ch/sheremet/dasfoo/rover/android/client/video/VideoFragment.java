package ch.sheremet.dasfoo.rover.android.client.video;

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
import java.util.MissingFormatArgumentException;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import ch.sheremet.dasfoo.rover.android.client.MainActivity;
import ch.sheremet.dasfoo.rover.android.client.R;
import ch.sheremet.dasfoo.rover.android.client.property.PropertyReader;

/**
 * Created by Katarina Sheremet on 6/7/16 12:00 PM.
 */
public class VideoFragment extends Fragment implements TextureView.SurfaceTextureListener,
        View.OnClickListener {

    private static final String VIDEO_FORMAT = "video/avc"; // h.264

    // Log tag.
    private static final String TAG = VideoFragment.class.getName();

    public static volatile BlockingQueue<byte[]> nalQueue = new LinkedBlockingQueue<>();

    private Thread mVideoThread;

    //MediaCodec
    private MediaCodec mMediaCodec;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        TextureView textureView = (TextureView) view.findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(this);

        Button playVideo = (Button) view.findViewById(R.id.start_video_button);
        playVideo.setOnClickListener(this);

        Button stopVideo = (Button) view.findViewById(R.id.stop_video_button);
        stopVideo.setOnClickListener(this);
        return view;
    }

    @Override
    public void onSurfaceTextureAvailable(final SurfaceTexture surfaceTexture, int width, int height) {
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
    public void onSurfaceTextureSizeChanged(final SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(final SurfaceTexture surface) {
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_video_button:
                // Start thread reading on server
                try {
                    PropertyReader propertyReader = new PropertyReader(getActivity());
                    Properties properties = propertyReader.getProperties("network.properties");
                    String host = ((MainActivity) getActivity()).getHost();
                    int port = ((MainActivity) getActivity()).getPort();
                    String password = properties.getProperty("password");
                    mVideoThread = new Thread(new VideoDecoderRunnable(host, port, password));
                    mVideoThread.start();
                    // Start mMediaCodec
                    mMediaCodec.start();
                } catch (MissingFormatArgumentException e) {
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
