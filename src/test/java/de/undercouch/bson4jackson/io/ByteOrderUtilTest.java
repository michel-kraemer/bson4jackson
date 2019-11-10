package de.undercouch.bson4jackson.io;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link ByteOrderUtil}
 * @author Michel Kraemer
 */
public class ByteOrderUtilTest {
    @Test
    public void flipInt() {
        assertEquals(0xDDCCBBAA, ByteOrderUtil.flip(0xAABBCCDD));
        assertEquals(-129, ByteOrderUtil.flip(Integer.MAX_VALUE));
        assertEquals(128, ByteOrderUtil.flip(Integer.MIN_VALUE));
    }
}
