package org.dasfoo.rover.android.client.video;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.dasfoo.rover.android.client.video.StreamParser;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class StreamParserTest {
    private byte[] takeUnit(StreamParser p, int maxSize) throws Exception {
        byte[] unit = new byte[maxSize];
        int size = p.takeUnit(unit);
        byte[] sizedUnit = new byte[size];
        System.arraycopy(unit, 0, sizedUnit, 0, size);
        return sizedUnit;
    }

    @Test
    public void correctness_takesOne() throws Exception {
        StreamParser p = new StreamParser(new ByteArrayInputStream(new byte[]{
            0x00, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06,
            0x00, 0x00, 0x01,
        }));
        byte[] unit = this.takeUnit(p, 1024);
        assertArrayEquals(new byte[]{0x00, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06}, unit);
    }

    @Test(expected=IOException.class)
    public void correctness_throwsOnEofWithUnits() throws Exception {
        StreamParser p = new StreamParser(new ByteArrayInputStream(new byte[]{
            0x00, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06,
            0x00, 0x00, 0x01,
        }));
        this.takeUnit(p, 1024);
        this.takeUnit(p, 1024);
    }

    @Test(expected=IOException.class)
    public void correctness_throwsOnEofWithoutUnits() throws Exception {
        StreamParser p = new StreamParser(new ByteArrayInputStream(new byte[]{
            0x00, 0x00,
        }));
        this.takeUnit(p, 1024);
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

        byte[] unit = this.takeUnit(p, 1024);
        assertArrayEquals(new byte[]{0x00, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06}, unit);

        unit = this.takeUnit(p, 1024);
        assertArrayEquals(new byte[]{0x00, 0x00, 0x01, 0x05, 0x04, 0x03, 0x02}, unit);

        unit = this.takeUnit(p, 1024);
        assertArrayEquals(
            new byte[]{0x00, 0x00, 0x01, 0x09, 0x08, 0x07, 0x06, 0x05, 0x04, 0x03, 0x02}, unit);

        unit = this.takeUnit(p, 1024);
        assertArrayEquals(new byte[]{0x00, 0x00, 0x01}, unit);
    }
}
