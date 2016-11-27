package org.dasfoo.android.h264videostream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class StreamParserTest {
    private byte[] takeUnit(StreamParser p) throws Exception {
        ByteBuffer b = ByteBuffer.allocate(StreamParser.maxUnitSize);
        p.takeUnit(b);

        int size = b.position();
        b.rewind();

        byte[] data = new byte[size];
        b.get(data);
        return data;
    }

    @Test
    public void correctness_takesOne() throws Exception {
        StreamParser p = new StreamParser(new ByteArrayInputStream(new byte[]{
            0x00, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06,
            0x00, 0x00, 0x01,
        }));
        byte[] unit = this.takeUnit(p);
        assertArrayEquals(new byte[]{0x00, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06}, unit);
    }

    @Test(expected=IOException.class)
    public void correctness_throwsOnEofWithUnits() throws Exception {
        StreamParser p = new StreamParser(new ByteArrayInputStream(new byte[]{
            0x00, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06,
            0x00, 0x00, 0x01,
        }));
        this.takeUnit(p);
        this.takeUnit(p);
    }

    @Test(expected=IOException.class)
    public void correctness_throwsOnEofWithoutUnits() throws Exception {
        StreamParser p = new StreamParser(new ByteArrayInputStream(new byte[]{
            0x00, 0x00,
        }));
        this.takeUnit(p);
    }

    @Test
    public void correctness_skipsGarbageAndTakesFew() throws Exception {
        StreamParser p = new StreamParser(new ByteArrayInputStream(new byte[]{
            0x00, 0x00, 0x15, 0x14, 0x00, 0x01, 0x01, 0x00,
            0x00, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06,
            0x00, 0x00, 0x01, 0x05, 0x04, 0x03, 0x02,
            0x00, 0x00, 0x01, 0x09, 0x08, 0x07, 0x06, 0x05, 0x04, 0x03, 0x02,
            0x00, 0x00, 0x01,
            0x00, 0x00, 0x01,
        }));

        byte[] unit = this.takeUnit(p);
        assertArrayEquals(new byte[]{0x00, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06}, unit);

        unit = this.takeUnit(p);
        assertArrayEquals(new byte[]{0x00, 0x00, 0x01, 0x05, 0x04, 0x03, 0x02}, unit);

        unit = this.takeUnit(p);
        assertArrayEquals(
            new byte[]{0x00, 0x00, 0x01, 0x09, 0x08, 0x07, 0x06, 0x05, 0x04, 0x03, 0x02}, unit);

        unit = this.takeUnit(p);
        assertArrayEquals(new byte[]{0x00, 0x00, 0x01}, unit);
    }
}
