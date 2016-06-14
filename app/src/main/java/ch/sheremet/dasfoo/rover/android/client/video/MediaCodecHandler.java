package ch.sheremet.dasfoo.rover.android.client.video;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.support.annotation.NonNull;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created by Katarina Sheremet on 6/8/16 1:04 PM.
 */
public class MediaCodecHandler {
    private static final String TAG = MediaCodecHandler.class.getSimpleName();

    private final MediaCodec mediaCodec;

    public MediaCodecHandler(final MediaCodec mCodec) {
        mediaCodec = mCodec;
    }

    // Set a MediaCodec for asynchronously processing
    public final MediaCodec setupAsynchMediaCodec() {
        mediaCodec.setCallback(new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(@NonNull final MediaCodec codec,
                                               final int inputBufferId) {
                final ByteBuffer inputBuffer = codec.getInputBuffer(inputBufferId);
                try {
                    // Get Nal unit from queue
                    byte[] unit = VideoFragment.nalQueue.take();
                    inputBuffer.put(unit);
                    codec.queueInputBuffer(inputBufferId, 0, unit.length, 0, 0);
                } catch (InterruptedException e) {
                    Log.e(TAG, e.getMessage());
                }
            }

            @Override
            public void onOutputBufferAvailable(@NonNull final MediaCodec codec, final int index,
                                                @NonNull final MediaCodec.BufferInfo info) {
                // If a valid surface was specified when configuring the codec,
                // passing true renders this output buffer to the surface.
                codec.releaseOutputBuffer(index, true);
            }

            @Override
            public void onError(@NonNull final MediaCodec codec,
                                @NonNull final MediaCodec.CodecException e) {
            }

            @Override
            public void onOutputFormatChanged(@NonNull final MediaCodec codec,
                                              @NonNull final MediaFormat format) {
            }
        });

        return mediaCodec;
    }
}
