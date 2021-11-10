package de.undercouch.bson4jackson.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * <p>A random-access buffer that resizes itself. This buffer differentiates
 * from {@link java.io.ByteArrayOutputStream} in the following points:</p>
 * <ul>
 * <li>It allows specifying the byte order.</li>
 * <li>It assigns several internal buffers instead of one and therefore
 * saves garbage collection cycles.</li>
 * <li>It is able to flush some of its internal buffers to an output stream
 * or to a writable channel.</li>
 * </ul>
 * <p>The buffer has an initial size. This is also the size of each internal
 * buffer, so if a new buffer has to be allocated it will take exactly
 * that many bytes.</p>
 * <p>By calling {@link #flushTo(OutputStream)} or {@link #flushTo(WritableByteChannel)}
 * some of this buffer's internal buffers are flushed and then deallocated. The
 * buffer maintains an internal counter for all flushed buffers. This allows the
 * {@link #writeTo(OutputStream)} and {@link #writeTo(WritableByteChannel)}
 * methods to only write non-flushed buffers. So, this class can be used for
 * streaming by flushing internal buffers from time to time and at the end
 * writing the rest:</p>
 * <pre>
 * ...
 * buf.flushTo(out);
 * ...
 * buf.flushTo(out);
 * ...
 * buf.flushTo(out);
 * ...
 * buf.writeTo(out);</pre>
 * <p>If flushing is never used a single call to one of the <code>writeTo</code>
 * methods is enough to write the whole buffer.</p>
 * <p>Once the buffer has been written to an output stream or channel, putting
 * elements into it is not possible anymore and will lead to an
 * {@link java.lang.IndexOutOfBoundsException}.</p>
 * @author Michel Kraemer
 */
public class DynamicOutputBuffer {
    /**
     * The default byte order if nothing is specified
     */
    public final static ByteOrder DEFAULT_BYTE_ORDER = ByteOrder.BIG_ENDIAN;

    /**
     * A unique key to make the first buffer re-usable
     */
    protected static final StaticBuffers.Key BUFFER_KEY = StaticBuffers.Key.BUFFER2;

    /**
     * The default initial buffer size if nothing is specified
     */
    public final static int DEFAULT_BUFFER_SIZE = Math.max(StaticBuffers.GLOBAL_MIN_SIZE, 1024 * 8);

    /**
     * The byte order of this buffer
     */
    protected final ByteOrder _order;

    /**
     * The size of each internal buffer (also the initial buffer size)
     */
    protected final int _bufferSize;

    /**
     * The current write position
     */
    protected int _position;

    /**
     * The position of the first byte that has not been already
     * flushed. Any attempt to put something into the buffer at
     * a position before this first byte is invalid and causes
     * a {@link IndexOutOfBoundsException} to be thrown.
     */
    protected int _flushPosition;

    /**
     * The current buffer size (changes dynamically)
     */
    protected int _size;

    /**
     * A linked list of internal buffers
     */
    protected List<ByteBuffer> _buffers = new ArrayList<>(1);

    /**
     * The character set used in {@link #putUTF8(String)}. Will be
     * created lazily in {@link #getUTF8Charset()}
     */
    protected Charset _utf8;

    /**
     * The encoder used in {@link #putUTF8(String)}. Will be created
     * lazily in {@link #getUTF8Encoder()}
     */
    protected CharsetEncoder _utf8Encoder;

    /**
     * A queue of buffers that have already been flushed and are
     * free to reuse.
     * @see #_reuseBuffersCount
     */
    protected Queue<ByteBuffer> _buffersToReuse;

    /**
     * The number of buffers to reuse
     * @see #_buffersToReuse
     */
    protected int _reuseBuffersCount = 0;

    /**
     * Creates a dynamic buffer with BIG_ENDIAN byte order and
     * a default initial buffer size of {@link #DEFAULT_BUFFER_SIZE} bytes.
     */
    public DynamicOutputBuffer() {
        this(DEFAULT_BYTE_ORDER);
    }

    /**
     * Creates a dynamic buffer with BIG_ENDIAN byte order and
     * the given initial buffer size.
     * @param initialSize the initial buffer size
     */
    public DynamicOutputBuffer(int initialSize) {
        this(DEFAULT_BYTE_ORDER, initialSize);
    }

    /**
     * Creates a dynamic buffer with the given byte order and
     * a default initial buffer size of {@link #DEFAULT_BUFFER_SIZE} bytes.
     * @param order the byte order
     */
    public DynamicOutputBuffer(ByteOrder order) {
        this(order, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Creates a dynamic buffer with the given byte order and
     * the given initial buffer size.
     * @param order the byte order
     * @param initialSize the initial buffer size
     */
    public DynamicOutputBuffer(ByteOrder order, int initialSize) {
        if (initialSize <= 0) {
            throw new IllegalArgumentException("Initial buffer size must be larger than 0");
        }

        _order = order;
        _bufferSize = initialSize;
        clear();
    }

    /**
     * Sets the number of buffers to save for reuse after they have been
     * invalidated by {@link #flushTo(OutputStream)} or {@link #flushTo(WritableByteChannel)}.
     * Invalidated buffers will be saved in an internal queue. When the buffer
     * needs a new internal buffer, it first attempts to reuse an existing one
     * before allocating a new one.
     * @param count the number of buffers to save for reuse
     */
    public void setReuseBuffersCount(int count) {
        _reuseBuffersCount = count;
        if (_buffersToReuse != null) {
            if (_reuseBuffersCount == 0) {
                _buffersToReuse = null;
            } else {
                while (_reuseBuffersCount < _buffersToReuse.size()) {
                    _buffersToReuse.poll();
                }
            }
        }
    }

    /**
     * Allocates a new buffer or attempts to reuse an existing one.
     * @return a new buffer with the current buffer size and the current byte order
     */
    protected ByteBuffer allocateBuffer() {
        if (_buffersToReuse != null && !_buffersToReuse.isEmpty()) {
            ByteBuffer bb = _buffersToReuse.poll();
            bb.rewind();
            bb.limit(bb.capacity());
            return bb;
        }
        ByteBuffer r = StaticBuffers.getInstance().byteBuffer(BUFFER_KEY, _bufferSize);
        r.limit(_bufferSize);
        return r.order(_order);
    }

    /**
     * Removes a buffer from the list of internal buffers and saves it for
     * reuse if this feature is enabled.
     * @param n the number of the buffer to remove
     */
    protected void deallocateBuffer(int n) {
        ByteBuffer bb = _buffers.set(n, null);
        if (bb != null && _reuseBuffersCount > 0) {
            if (_buffersToReuse == null) {
                _buffersToReuse = new LinkedList<>();
            }
            if (_reuseBuffersCount > _buffersToReuse.size()) {
                _buffersToReuse.add(bb);
            }
        }
    }

    /**
     * Adds a new buffer to the list of internal buffers
     * @return the new buffer
     */
    protected ByteBuffer addNewBuffer() {
        ByteBuffer bb = allocateBuffer();
        _buffers.add(bb);
        return bb;
    }

    /**
     * Gets the buffer that holds the byte at the given absolute position.
     * Automatically adds new internal buffers if the position lies outside
     * the current range of all internal buffers.
     * @param position the position
     * @return the buffer at the requested position
     */
    protected ByteBuffer getBuffer(int position) {
        int n = position / _bufferSize;
        while (n >= _buffers.size()) {
            addNewBuffer();
        }
        return _buffers.get(n);
    }

    /**
     * Adapts the buffer size so it is at least equal to the
     * given number of bytes. This method does not add new
     * internal buffers.
     * @param size the minimum buffer size
     */
    protected void adaptSize(int size) {
        if (size > _size) {
            _size = size;
        }
    }

    /**
     * @return the lazily created UTF-8 character set
     */
    protected Charset getUTF8Charset() {
        if (_utf8 == null) {
            _utf8 = StandardCharsets.UTF_8;
        }
        return _utf8;
    }

    /**
     * @return the lazily created UTF-8 encoder
     */
    protected CharsetEncoder getUTF8Encoder() {
        if (_utf8Encoder == null) {
            _utf8Encoder = getUTF8Charset().newEncoder();
        }
        return _utf8Encoder;
    }

    /**
     * @return the current buffer size (changes dynamically)
     */
    public int size() {
        return _size;
    }

    /**
     * Clear the buffer and reset size and write position
     */
    public void clear() {
        // release a static buffer if possible
        if (_buffersToReuse != null && !_buffersToReuse.isEmpty()) {
            StaticBuffers.getInstance().releaseByteBuffer(BUFFER_KEY, _buffersToReuse.peek());
        } else if (!_buffers.isEmpty()) {
            StaticBuffers.getInstance().releaseByteBuffer(BUFFER_KEY, _buffers.get(0));
        }

        if (_buffersToReuse != null) {
            _buffersToReuse.clear();
        }
        _buffers.clear();
        _position = 0;
        _flushPosition = 0;
        _size = 0;
    }

    /**
     * Puts a byte into the buffer at the current write position
     * and increases the write position accordingly.
     * @param b the byte to put
     */
    public void putByte(byte b) {
        putByte(_position, b);
        ++_position;
    }

    /**
     * Puts several bytes into the buffer at the given position
     * and increases the write position accordingly.
     * @param bs an array of bytes to put
     */
    public void putBytes(byte... bs) {
        putBytes(_position, bs);
        _position += bs.length;
    }

    /**
     * Puts a byte array into the buffer at the given position with the given
     * offset and increases the write position accordingly.
     * @param bs an array of bytes to put
     * @param offset the offset within the array of the first byte to be read
     * @param length the number of bytes to be read from the given array
     */
    public void putBytes(byte[] bs, int offset, int length) {
        putBytes(_position, bs, offset, length);
        _position += length;
    }

    /**
     * Puts a byte into the buffer at the given position. Does
     * not increase the write position.
     * @param pos the position where to put the byte
     * @param b the byte to put
     */
    public void putByte(int pos, byte b) {
        adaptSize(pos + 1);
        ByteBuffer bb = getBuffer(pos);
        int i = pos % _bufferSize;
        bb.put(i, b);
    }

    /**
     * Puts several bytes into the buffer at the given position.
     * Does not increase the write position.
     * @param pos the position where to put the bytes
     * @param bs an array of bytes to put
     */
    public void putBytes(int pos, byte... bs) {
        putBytes(pos, bs, 0, bs.length);
    }

    /**
     * Puts a byte array into the buffer at the given position with the given
     * offset. Does not increase the write position.
     * @param pos the position where to put the bytes
     * @param bs an array of bytes to put
     * @param offset the offset within the array of the first byte to be read
     * @param length the number of bytes to be read from the given array
     */
    public void putBytes(int pos, byte[] bs, int offset, int length) {
        adaptSize(pos + length);
        ByteBuffer bb;
        while (length > 0) {
            bb = getBuffer(pos);
            int index = pos % _bufferSize;
            bb.position(index);
            int chunkLength = Math.min(bb.limit() - index, length);
            bb.put(bs, offset, chunkLength);
            pos += chunkLength;
            offset += chunkLength;
            length -= chunkLength;
        }
    }

    /**
     * Puts a 32-bit integer into the buffer at the current write position
     * and increases write position accordingly.
     * @param i the integer to put
     */
    public void putInt(int i) {
        putInt(_position, i);
        _position += 4;
    }

    /**
     * Puts a 32-bit integer into the buffer at the given position. Does
     * not increase the write position.
     * @param pos the position where to put the integer
     * @param i the integer to put
     */
    public void putInt(int pos, int i) {
        adaptSize(pos + 4);
        ByteBuffer bb = getBuffer(pos);
        int index = pos % _bufferSize;
        if (bb.limit() - index >= 4) {
            bb.putInt(index, i);
        } else {
            byte b0 = (byte)i;
            byte b1 = (byte)(i >> 8);
            byte b2 = (byte)(i >> 16);
            byte b3 = (byte)(i >> 24);

            if (_order == ByteOrder.BIG_ENDIAN) {
                putBytes(pos, b3, b2, b1, b0);
            } else {
                putBytes(pos, b0, b1, b2, b3);
            }
        }
    }

    /**
     * Puts a 64-bit integer into the buffer at the current write position
     * and increases the write position accordingly.
     * @param l the 64-bit integer to put
     */
    public void putLong(long l) {
        putLong(_position, l);
        _position += 8;
    }

    /**
     * Puts a 64-bit integer into the buffer at the given position. Does
     * not increase the write position.
     * @param pos the position where to put the integer
     * @param l the 64-bit integer to put
     */
    public void putLong(int pos, long l) {
        adaptSize(pos + 8);
        ByteBuffer bb = getBuffer(pos);
        int index = pos % _bufferSize;
        if (bb.limit() - index >= 8) {
            bb.putLong(index, l);
        } else {
            byte b0 = (byte)l;
            byte b1 = (byte)(l >> 8);
            byte b2 = (byte)(l >> 16);
            byte b3 = (byte)(l >> 24);
            byte b4 = (byte)(l >> 32);
            byte b5 = (byte)(l >> 40);
            byte b6 = (byte)(l >> 48);
            byte b7 = (byte)(l >> 56);

            if (_order == ByteOrder.BIG_ENDIAN) {
                putBytes(pos, b7, b6, b5, b4, b3, b2, b1, b0);
            } else {
                putBytes(pos, b0, b1, b2, b3, b4, b5, b6, b7);
            }
        }
    }

    /**
     * Puts a 32-bit floating point number into the buffer at the current
     * write position and increases the write position accordingly.
     * @param f the float to put
     */
    public void putFloat(float f) {
        putFloat(_position, f);
        _position += 4;
    }

    /**
     * Puts a 32-bit floating point number into the buffer at the given
     * position. Does not increase the write position.
     * @param pos the position where to put the float
     * @param f the float to put
     */
    public void putFloat(int pos, float f) {
        putInt(pos, Float.floatToRawIntBits(f));
    }

    /**
     * Puts a 64-bit floating point number into the buffer at the current
     * write position and increases the write position accordingly.
     * @param d the double to put
     */
    public void putDouble(double d) {
        putDouble(_position, d);
        _position += 8;
    }

    /**
     * Puts a 64-bit floating point number into the buffer at the given
     * position. Does not increase the write position.
     * @param pos the position where to put the double
     * @param d the double to put
     */
    public void putDouble(int pos, double d) {
        putLong(pos, Double.doubleToRawLongBits(d));
    }

    /**
     * Puts a character sequence into the buffer at the current
     * write position and increases the write position accordingly.
     * @param s the character sequence to put
     */
    public void putString(CharSequence s) {
        putString(_position, s);
        _position += (s.length() * 2);
    }

    /**
     * Puts a character sequence into the buffer at the given
     * position. Does not increase the write position.
     * @param pos the position where to put the character sequence
     * @param s the character sequence to put
     */
    public void putString(int pos, CharSequence s) {
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            byte b0 = (byte)c;
            byte b1 = (byte)(c >> 8);
            if (_order == ByteOrder.BIG_ENDIAN) {
                putBytes(pos, b1, b0);
            } else {
                putBytes(pos, b0, b1);
            }
            pos += 2;
        }
    }

    /**
     * Encodes the given string as UTF-8, puts it into the buffer
     * and increases the write position accordingly.
     * @param s the string to put
     * @return the number of UTF-8 bytes put
     */
    public int putUTF8(String s) {
        int written = putUTF8(_position, s);
        _position += written;
        return written;
    }

    /**
     * Puts the given string as UTF-8 into the buffer at the
     * given position. This method does not increase the write position.
     * @param pos the position where to put the string
     * @param s the string to put
     * @return the number of UTF-8 bytes put
     */
    public int putUTF8(int pos, String s) {
        ByteBuffer minibb = null;

        CharsetEncoder enc = getUTF8Encoder();
        CharBuffer in = CharBuffer.wrap(s);

        int pos2 = pos;
        ByteBuffer bb = getBuffer(pos2);
        int index = pos2 % _bufferSize;
        bb.position(index);

        while (in.remaining() > 0) {
            CoderResult res = enc.encode(in, bb, true);

            // flush minibb first
            if (bb == minibb) {
                bb.flip();
                while (bb.remaining() > 0) {
                    putByte(pos2, bb.get());
                    ++pos2;
                }
            } else {
                pos2 += bb.position() - index;
            }

            if (res.isOverflow()) {
                if (bb.remaining() > 0) {
                    // exceeded buffer boundaries; write to a small temporary buffer
                    if (minibb == null) {
                        minibb = ByteBuffer.allocate(4);
                    }
                    minibb.clear();
                    bb = minibb;
                    index = 0;
                } else {
                    bb = getBuffer(pos2);
                    index = pos2 % _bufferSize;
                    bb.position(index);
                }
            } else if (res.isError()) {
                try {
                    res.throwException();
                } catch (CharacterCodingException e) {
                    throw new RuntimeException("Could not encode string", e);
                }
            }
        }

        adaptSize(pos2);
        return pos2 - pos;
    }

    /**
     * Tries to copy as much bytes as possible from this buffer to
     * the given channel. See {@link #flushTo(WritableByteChannel)}
     * for further information.
     * @param out the output stream to write to
     * @throws IOException if the buffer could not be flushed
     */
    public void flushTo(OutputStream out) throws IOException {
        int n1 = _flushPosition / _bufferSize;
        int n2 = _position / _bufferSize;
        if (n1 < n2) {
            flushTo(Channels.newChannel(out));
        }
    }

    /**
     * Tries to copy as much bytes as possible from this buffer to
     * the given channel. This method always copies whole internal
     * buffers and deallocates them afterwards. It does not deallocate
     * the buffer the write position is currently pointing to nor does
     * it deallocate buffers following the write position. The method
     * increases an internal pointer so consecutive calls also copy
     * consecutive bytes.
     * @param out the channel to write to
     * @throws IOException if the buffer could not be flushed
     */
    public void flushTo(WritableByteChannel out) throws IOException {
        int n1 = _flushPosition / _bufferSize;
        int n2 = _position / _bufferSize;
        while (n1 < n2) {
            ByteBuffer bb = _buffers.get(n1);
            bb.rewind();
            out.write(bb);
            deallocateBuffer(n1);
            _flushPosition += _bufferSize;
            ++n1;
        }
    }

    /**
     * Writes all non-flushed internal buffers to the given output
     * stream. If {@link #flushTo(OutputStream)} has not been called
     * before, this method writes the whole buffer to the output stream.
     * @param out the output stream to write to
     * @throws IOException if the buffer could not be written
     */
    public void writeTo(OutputStream out) throws IOException {
        writeTo(Channels.newChannel(out));
    }

    /**
     * Writes all non-flushed internal buffers to the given channel.
     * If {@link #flushTo(WritableByteChannel)} has not been called
     * before, this method writes the whole buffer to the channel.
     * @param out the channel to write to
     * @throws IOException if the buffer could not be written
     */
    public void writeTo(WritableByteChannel out) throws IOException {
        int n1 = _flushPosition / _bufferSize;
        int n2 = _buffers.size();
        int toWrite = _size - _flushPosition;
        while (n1 < n2) {
            int curWrite = Math.min(toWrite, _bufferSize);
            ByteBuffer bb = _buffers.get(n1);
            bb.position(curWrite);
            bb.flip();
            out.write(bb);
            ++n1;
            toWrite -= curWrite;
        }
    }
}
