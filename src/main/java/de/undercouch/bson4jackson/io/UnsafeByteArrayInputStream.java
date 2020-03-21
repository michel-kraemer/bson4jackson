package de.undercouch.bson4jackson.io;

import java.io.InputStream;

/**
 * An input stream that serves the content of an array. Compared to
 * {@link java.io.ByteArrayInputStream} this class is not thread-safe.
 * @author Michel Kraemer
 */
public class UnsafeByteArrayInputStream extends InputStream {
    /**
     * The buffer to serve
     */
    protected final byte[] _buf;

    /**
     * The current position in the buffer
     */
    protected int _pos;

    /**
     * The index one greater than the last byte to serve
     */
    protected int _count;

    /**
     * The current marked position
     */
    protected int _mark;

    /**
     * Creates a new stream that serves the whole given array
     * @param buf the array to serve
     */
    public UnsafeByteArrayInputStream(byte[] buf) {
        this(buf, 0, buf.length);
    }

    /**
     * Creates a new stream that serves part of the given array
     * @param buf the array to serve
     * @param off the index of the first byte to serve
     * @param len the number of bytes to serve
     */
    public UnsafeByteArrayInputStream(byte[] buf, int off, int len) {
        _buf = buf;
        _pos = off;
        _count = Math.min(off + len, buf.length);
        _mark = off;
    }

    @Override
    public int read() {
        return _pos >= _count ? -1 : (_buf[_pos++] & 0xFF);
    }

    @Override
    public int read(byte[] b, int off, int len) {
        if (_pos >= _count) {
            return -1;
        }

        int avail = _count - _pos;
        int cnt = Math.min(len, avail);
        System.arraycopy(_buf, _pos, b, off, cnt);
        _pos += cnt;
        return cnt;
    }

    @Override
    public long skip(long n) {
        if (n <= 0) {
            return 0;
        }

        int avail = _count - _pos;
        if (avail <= 0) {
            return 0;
        }
        if (avail < n) {
            n = avail;
        }
        _pos += n;
        return n;
    }

    @Override
    public int available() {
        return _count - _pos;
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public void mark(int readlimit) {
        _mark = _pos;
    }

    @Override
    public void reset() {
        _pos = _mark;
    }

    @Override
    public void close() {
        // nothing to do here
    }
}
