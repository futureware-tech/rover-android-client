package org.dasfoo.android.h264videostream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Locale;

/**
 * Take NAL units one by one from the specified InputStream.
 */
public class StreamParser {
    /**
     * Maximum size of a NAL unit, in bytes, according to RFC3984.
     */
    public static final int maxUnitSize = 65535;

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
    private final byte[] readSignature = new byte[StreamParser.unitSignature.length];

    /**
     * Holds the NAL unit currently being read from the stream.
     */
    private final byte[] unitBuffer = new byte[StreamParser.maxUnitSize];

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
    private synchronized int skipToAfterSignature(final int maxBytesToRead)
            throws IOException, InterruptedException {
        if (maxBytesToRead < StreamParser.unitSignature.length) {
            throw new IOException("Too small chunk to read (won't keep the signature)");
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
     * <p>
     * Threading: this method is not reentrant (this.unitBuffer).
     * @param  buffer               a buffer that will receive a NAL unit (via put() method).
     * @return                      number of bytes written to the buffer.
     * @throws IOException          if the buffer is not large enough to hold a NAL unit.
     *                              The stream is then broken.
     * @throws InterruptedException if the thread was interrupted
     */
    public synchronized int takeUnit(final ByteBuffer buffer)
            throws IOException, InterruptedException {
        if (!this.mStoppedAtSignature) {
            // This is going to only happen once - on the first call.
            // Assume the signature can't be more than maxUnitSize bytes away.
            int signatureOffset;
            this.mStream.mark(this.unitBuffer.length);
            try {
                signatureOffset = this.skipToAfterSignature(this.unitBuffer.length) -
                    StreamParser.unitSignature.length;
            } finally {
                this.mStream.reset();
            }
            this.ensureStreamRead((int) this.mStream.skip(signatureOffset), signatureOffset);
            this.mStoppedAtSignature = true;
        }

        // Start buffering in mStream from the current position.
        int unitSize;
        this.mStream.mark(this.unitBuffer.length + StreamParser.unitSignature.length);
        try {
            this.ensureStreamRead((int) this.mStream.skip(StreamParser.unitSignature.length),
                    StreamParser.unitSignature.length);
            unitSize = this.skipToAfterSignature(this.unitBuffer.length);
        } finally {
            // Reset mStream position to where we started buffering (that's where the unit begins).
            this.mStream.reset();
        }

        // All the data must have been buffered and therefore readable in a single call.
        this.ensureStreamRead(this.mStream.read(this.unitBuffer, 0, unitSize), unitSize);
        buffer.put(this.unitBuffer, 0, unitSize);
        return unitSize;
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
