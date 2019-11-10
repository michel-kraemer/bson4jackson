package de.undercouch.bson4jackson.io;

/**
 * Provides static methods to change the byte order of single values
 * @author Michel Kraemer
 */
public class ByteOrderUtil {
    /**
     * Flips the byte order of an integer
     * @param i the integer
     * @return the flipped integer
     */
    public static int flip(int i) {
        int result = 0;
        result |= (i & 0xFF) << 24;
        result |= (i & 0xFF00) << 8;
        result |= ((i & 0xFF0000) >> 8) & 0xFF00;
        result |= ((i & 0xFF000000) >> 24) & 0xFF;
        return result;
    }
}
