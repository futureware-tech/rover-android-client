package org.dasfoo.rover.android.client.video;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Created by Katarina Sheremet on 6/8/16 1:04 PM.
 */
public class MediaCodecHandler {

    /**
     * Class information for logging.
     */
    private static final String TAG = MediaCodecHandler.class.getSimpleName();

    /**
     * MediaCodec.
     */
    private final MediaCodec mediaCodec;

    /**
     * Default constructor.
     * @param mCodec MediaCodec
     */
    public MediaCodecHandler(final MediaCodec mCodec) {
        mediaCodec = mCodec;
    }

    /**
     * Sets MediaCodec for asynchronously processing.
     *
     * @return MediaCodec
     */
    public final MediaCodec setupAsynchMediaCodec() {
        mediaCodec.setCallback(new MediaCodec.Callback() {
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

        return mediaCodec;
    }
}
