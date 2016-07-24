package org.dasfoo.rover.android.client.video;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;

import org.dasfoo.rover.android.client.BuildConfig;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.MalformedInputException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
     * Queue is used for saving index of input buffer.
     */
    private static BlockingQueue<Integer> idBufferQueue = new LinkedBlockingQueue<>();

    /**
     * MediaCodec.
     */
    private MediaCodec mCodec;

    /**
     * It implements onBeforeStream and onAfterStream methods.
     * It is used for setting up InputStream.
     */
    private MediaStreamRendererCallback mCallback;

    /**
     * It contains stream that reorganizes in Nal units.
     */
    private InputStream mInputStream;

    /**
     * Constructor.
     * Creates MediaCodec and binds it to Surface on UI.
     *
     * @param surface     on UI
     * @param mediaFormat output video format
     * @param callback sets up onBeforeStream and onAfterStream. It is used
     *                 for InputStream
     */
    public MediaStreamRenderer(final Surface surface, final MediaFormat mediaFormat,
                               final MediaStreamRendererCallback callback) {
        this.mCallback = callback;
        try {
            // Constructor for MediaCodec
            mCodec = MediaCodec.createDecoderByType(mediaFormat.getString(MediaFormat.KEY_MIME));
            // Set up Callback for mMediaCodec
            setupAsyncMediaCodec();
            // Configure mMediaCodec and bind with TextureView
            mCodec.configure(mediaFormat, surface, null, 0);
        } catch (IOException e) {
            Log.e(TAG, "Codec cannot be created", e);
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
    private void setIdBufferInQueue(final Integer id)
            throws InterruptedException {
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
    private Integer getIdBufferFromQueue() throws InterruptedException {
        return idBufferQueue.take();
    }

    /**
     * Atomically removes all of the elements from this queue.
     * The queue will be empty after this call returns.
     */
    private void clearIdBufferQueue() {
        idBufferQueue.clear();
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
                    setIdBufferInQueue(inputBufferId);
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
     * Starts executing the active part of the class' code. This method is
     * called when a thread is started that has been created with a class which
     * implements {@code Runnable}.
     */
    @Override
    public final void run() {
        mCodec.start();
        mCallback.onBeforeStream(this);
        try {
            StreamParser streamParser = new StreamParser(mInputStream);
            while (!Thread.currentThread().isInterrupted()) {
                int id = getIdBufferFromQueue();
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
            mCallback.onAfterStream(this);
            mCodec.stop();
            // Release codec
            mCodec.release();
            clearIdBufferQueue();
        }
    }

    /**
     * It sets up InputStream for processing.
     *
     * @param inputStream InputStream
     */
    public final void setInputStream(final InputStream inputStream) {
        this.mInputStream = inputStream;
    }

    /**
     * It is callback class used for setting up InputStream for parsing.
     */
    public abstract static class MediaStreamRendererCallback {

        /**
         * Callback is used before processing Stream.
         * It sets up InputStream for MediaStreamRenderer.
         * // TODO(ksheremet): unable/disable buttons.
         *
         * @param streamRenderer current MediaStreamRenderer
         */
        public abstract void onBeforeStream(MediaStreamRenderer streamRenderer);

        /**
         * @param streamRenderer current MediaStreamRenderer
         */
        public abstract void onAfterStream(MediaStreamRenderer streamRenderer);
    }
}
