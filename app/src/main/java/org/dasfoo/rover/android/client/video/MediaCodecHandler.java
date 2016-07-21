package org.dasfoo.rover.android.client.video;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;

/**
 * Created by Katarina Sheremet on 6/8/16 1:04 PM.
 * Class configures MediaCodec.
 */
public class MediaCodecHandler {

    /**
     * Class information for logging.
     */
    private static final String TAG = MediaCodecHandler.class.getSimpleName();

    /**
     * MediaCodec.
     */
    private MediaCodec mMediaCodec;

    /**
     * Surface output format.
     */
    private MediaFormat mediaFormat;


    /**
     * Constructor.
     * Creates MediaCodec using format. Creates Surface output format
     * @param format video, that comes from input
     * @param width video on surface
     * @param height video on surface
     */
    public MediaCodecHandler(final String format, final int width, final int height) {
        try {
            mediaFormat = MediaFormat.createVideoFormat(format, width,
                    height);
            // Constructor for MediaCodec
            mMediaCodec = MediaCodec.createDecoderByType(format);
            // Set up Callback for mMediaCodec
            setupAsynchMediaCodec();
        } catch (IOException e) {
            Log.e(TAG, "Codec cannot be created", e);
        }
    }

    /**
     * Getters.
     *
     * @return MediaCodec
     */
    public final MediaCodec getMediaCodec() {
        return mMediaCodec;
    }

    /**
     * Configure MediaCodec and binds with video surface.
     *
     * @param surface from UI.
     */
    public final void bindWithSurface(final Surface surface) {
        // Configure mMediaCodec and bind with TextureView
        mMediaCodec.configure(mediaFormat, surface, null, 0);
    }

    /**
     * Sets MediaCodec for asynchronously processing.
     */
    public final void setupAsynchMediaCodec() {
        mMediaCodec.setCallback(new MediaCodec.Callback() {
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
}
