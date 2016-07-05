package org.dasfoo.rover.android.client.video;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;

/**
 * Take NAL units one by one from the specified InputStream.
 */
public class StreamParser {
    /**
     * NAL unit signature to split the stream.
     */
    private static final byte[] unitSignature = {0, 0, 1};

    /**
     * Input stream specified in constructor, or a wrapper that has mark/reset support.
     */
    private InputStream mStream;

    /**
     * Whether mStream read position is known to be right at the first byte of a NAL unit signature.
     */
    private boolean mStoppedAtSignature;

    /**
     * Holds the signature that has been read from the stream so far.
     */
    private byte[] readSignature = new byte[StreamParser.unitSignature.length];

    /**
     * Default constructor.
     * @param stream data stream with NAL units, separated by unitSignature.
     */
    public StreamParser(final InputStream stream) {
        this.mStream = stream;
        if (!this.mStream.markSupported()) {
            this.mStream = new BufferedInputStream(this.mStream);
        }
    }

    /**
     * Read mStream until the next NAL signature has been read.
     * <p>
     * Threading: this method is not reentrant (this.readSignature).
     * @param  maxBytesToRead       maximum number of bytes to read before throwing IOException
     * @return                      the number of bytes read, including the size of the signature
     * @throws IOException          when the next signature is not within a range of maxBytesToRead
     * @throws InterruptedException if the thread was interrupted
     */
    private int skipToAfterSignature(final int maxBytesToRead)
            throws IOException, InterruptedException {
        if (maxBytesToRead < StreamParser.unitSignature.length) {
            throw new IOException(String.format(Locale.US,
                    "Supplied buffer size %1$d is too small.", maxBytesToRead));
        }

        int bytesRead = 0;
        while (bytesRead < this.readSignature.length) {
            bytesRead += this.ensureStreamRead(this.mStream.read(this.readSignature, bytesRead,
                this.readSignature.length - bytesRead), null);
        }

        while (true) {
            if (Thread.interrupted()) {
                // Make this thread interruptable by e.g. UI actions.
                throw new InterruptedException();
            }
            if (Arrays.equals(this.readSignature, StreamParser.unitSignature)) {
                return bytesRead;
            }
            for (int i = 0; i < this.readSignature.length - 1; i++) {
                this.readSignature[i] = this.readSignature[i + 1];
            }
            if (bytesRead >= maxBytesToRead) {
                throw new IOException(String.format(Locale.US, "Failed to find a signature " +
                        "within %1$d bytes of the data stream.", bytesRead));
            }

            // It's guarranteed that read() will read at least one byte (unless EOF),
            // and we're using BufferedInputStream which optimizes for speed.
            bytesRead += this.ensureStreamRead(this.mStream.read(this.readSignature,
                this.readSignature.length - 1, 1), null);
        }
    }

    /**
     * Take next NAL unit from the stream.
     * @param  buffer               a buffer large enough to have a NAL unit written to it
     * @return                      the number of bytes actually written to the buffer
     * @throws IOException          if the buffer is not large enough to hold a NAL unit.
     *                              The stream is then broken.
     * @throws InterruptedException if the thread was interrupted
     */
    public int takeUnit(final byte[] buffer) throws IOException, InterruptedException {
        if (!this.mStoppedAtSignature) {
            // This is going to only happen once - on the first call.
            // Assume the signature can't be more than buffer.length away.
            int signatureOffset;
            this.mStream.mark(buffer.length);
            try {
                signatureOffset = this.skipToAfterSignature(buffer.length) -
                    StreamParser.unitSignature.length;
            } finally {
                this.mStream.reset();
            }
            this.ensureStreamRead((int) this.mStream.skip(signatureOffset), signatureOffset);
            this.mStoppedAtSignature = true;
        }

        // Start buffering in mStream from the current position.
        int unitSize;
        this.mStream.mark(buffer.length + StreamParser.unitSignature.length);
        try {
            this.ensureStreamRead((int) this.mStream.skip(StreamParser.unitSignature.length),
                    StreamParser.unitSignature.length);
            unitSize = this.skipToAfterSignature(buffer.length);
        } finally {
            // Reset mStream position to where we started buffering (that's where the unit begins).
            this.mStream.reset();
        }

        // All the data must have been buffered and therefore readable in a single call.
        return this.ensureStreamRead(this.mStream.read(buffer, 0, unitSize), unitSize);
    }

    /**
     * Ensure the read operation on the stream has succeeded.
     * @param  operationResult       the value returned by methods like read() or skip()
     * @param  expectedBufferedBytes if non-null, the number of bytes expected to be available
     * @return                       the original value of operationResult (for chaining)
     * @throws IOException           for EOF, or operationResult less than expectedBufferedBytes
     */
    private int ensureStreamRead(final int operationResult, final Integer expectedBufferedBytes)
            throws IOException {
        if (operationResult < 0) {
            throw new IOException("EOF when trying to read from the stream");
        }
        if (expectedBufferedBytes != null && operationResult < expectedBufferedBytes) {
            throw new IOException(String.format(Locale.US,
                    "Unexpected behavior of an input stream, failed to read %1$d bytes " +
                    "of buffered data (only got %1$d)", expectedBufferedBytes, operationResult));
        }
        return operationResult;
    }
}
