package de.undercouch.bson4jackson.io;

import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link BoundedInputStream}
 * @author James Roper
 */
public class BoundedInputStreamTest {

    private byte[] bytes = new byte[] { 10, 20, 30, 40 };

    @Test
    public void testReadWhenBoundSmaller() throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        BoundedInputStream is = new BoundedInputStream(bais, 3);
        try {
            assertEquals(10, is.read());
            assertEquals(20, is.read());
            assertEquals(30, is.read());
            assertEquals(-1, is.read());
            assertEquals(-1, is.read());
            assertEquals(40, bais.read());
        } finally {
            is.close();
        }
    }

    @Test
    public void testReadWhenBoundLarger() throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        BoundedInputStream is = new BoundedInputStream(bais, 5);
        try {
            assertEquals(10, is.read());
            assertEquals(20, is.read());
            assertEquals(30, is.read());
            assertEquals(40, is.read());
            assertEquals(-1, is.read());
            assertEquals(-1, is.read());
            assertEquals(-1, bais.read());
        } finally {
            is.close();
        }
    }

    @Test
    public void testReadLargeBufferWhenBoundSmaller() throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        BoundedInputStream is = new BoundedInputStream(bais, 3);
        try {
            byte[] buf = new byte[10];
            assertEquals(3, is.read(buf));
            assertArrayEquals(bytes, buf, 0, 3);
            assertEquals(-1, is.read(buf));
            assertEquals(40, bais.read());
        } finally {
            is.close();
        }
    }

    @Test
    public void testReadSmallBufferWhenBoundSmaller() throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        BoundedInputStream is = new BoundedInputStream(bais, 3);
        try {
            byte[] buf = new byte[2];
            assertEquals(2, is.read(buf));
            assertArrayEquals(bytes, buf, 0, 2);
            assertEquals(1, is.read(buf));
            assertArrayEquals(bytes, buf, 2, 1);
            assertEquals(-1, is.read(buf));
            assertEquals(40, bais.read());
        } finally {
            is.close();
        }
    }

    @Test
    public void testReadLargeBufferWhenBoundLarger() throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        BoundedInputStream is = new BoundedInputStream(bais, 5);
        try {
            byte[] buf = new byte[10];
            assertEquals(4, is.read(buf));
            assertArrayEquals(bytes, buf, 0, 4);
            assertEquals(-1, is.read(buf));
            assertEquals(-1, bais.read());
        } finally {
            is.close();
        }
    }

    @Test
    public void testReadSmallBufferWhenBoundLarger() throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        BoundedInputStream is = new BoundedInputStream(bais, 5);
        try {
            byte[] buf = new byte[2];
            assertEquals(2, is.read(buf));
            assertArrayEquals(bytes, buf, 0, 2);
            assertEquals(2, is.read(buf));
            assertArrayEquals(bytes, buf, 2, 2);
            assertEquals(-1, is.read(buf));
            assertEquals(-1, bais.read());
        } finally {
            is.close();
        }
    }

    @Test
    public void testLessBytesThanRequestedRead() throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes) {
            @Override
            public int read(byte[] b, int off, int len) {
                if (len > 2) {
                    return super.read(b, off, len - 1);
                } else {
                    return super.read(b, off, len);
                }
            }
        };
        BoundedInputStream is = new BoundedInputStream(bais, 4);
        try {
            byte[] buf = new byte[3];
            assertEquals(2, is.read(buf));
            assertArrayEquals(bytes, buf, 0, 2);
            assertEquals(2, is.read(buf));
            assertArrayEquals(bytes, buf, 2, 2);
            assertEquals(-1, is.read(buf));
            assertEquals(-1, bais.read());
        } finally {
            is.close();
        }
    }

    private static void assertArrayEquals(byte[] expected, byte[] actual, int off, int len) {
        for (int i = 0; i < len; i++) {
            assertEquals("element [" + i + "] not matching", expected[i + off], actual[i]);
        }
    }
}
