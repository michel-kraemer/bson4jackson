package de.undercouch.bson4jackson;

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
import java.util.LinkedList;

/**
 * A random-access buffer that resizes itself. This buffer differentiates
 * from {@link java.io.ByteArrayOutputStream} in the following points:
 * <ul>
 * <li>It allows specifying the byte order</li>
 * <li>It assigns several internal buffers instead of one and therefore
 * saves garbage collection cycles.</li>
 * </ul>
 * The buffer has an initial size. This is also the size of each internal
 * buffer, so if a new buffer has to be allocated it will take exactly
 * that many bytes.
 * @author Michel Kraemer
 */
public class DynamicOutputBuffer {
	/**
	 * The default byte order if nothing is specified
	 */
	private final static ByteOrder DEFAULT_BYTE_ORDER = ByteOrder.BIG_ENDIAN;
	
	/**
	 * The default initial buffer size if nothing is specified
	 */
	private final static int DEFAULT_BUFFER_SIZE = 1024 * 8;
	
	/**
	 * The byte order of this buffer
	 */
	private final ByteOrder _order;
	
	/**
	 * The size of each internal buffer (also the initial buffer size)
	 */
	private final int _bufferSize;
	
	/**
	 * The current write position
	 */
	private int _position;
	
	/**
	 * The current buffer size (changes dynamically)
	 */
	private int _size;
	
	/**
	 * A linked list of internal buffers
	 */
	private LinkedList<ByteBuffer> _buffers = new LinkedList<ByteBuffer>();
	
	/**
	 * The character set used in {@link #putUTF8(String)}. Will be
	 * created lazily in {@link #getUTF8Charset()}
	 */
	private Charset _utf8;
	
	/**
	 * The encoder used in {@link #putUTF8(String)}. Will be created
	 * lazily in {@link #getUTF8Encoder()}
	 */
	private CharsetEncoder _utf8Encoder;
	
	/**
	 * Creates a dynamic buffer with BIG_ENDIAN byte order and
	 * a default initial buffer size of 8192 bytes.
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
	 * a default initial buffer size of 8192 bytes.
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
	 * @return a new buffer with the current buffer size and the current byte order
	 */
	private ByteBuffer allocateNewBuffer() {
		return ByteBuffer.allocate(_bufferSize).order(_order);
	}
	
	/**
	 * Adds a new buffer to the list of internal buffers
	 * @return the new buffer
	 */
	private ByteBuffer addNewBuffer() {
		ByteBuffer bb = allocateNewBuffer();
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
	private ByteBuffer getBuffer(int position) {
		int n = position / _bufferSize;
		while (n >= _buffers.size()) {
			addNewBuffer();
			--n;
		}
		return _buffers.getLast();
	}
	
	/**
	 * Increments the current write position by 1
	 */
	private void incPosition() {
		incPosition(1);
	}
	
	/**
	 * Increments the current write position by the given
	 * number of bytes
	 * @param n the increment
	 */
	private void incPosition(int n) {
		_position += n;
		if (_position > _size) {
			_size = _position;
		}
	}
	
	/**
	 * @return the lazily created UTF-8 character set
	 */
	private Charset getUTF8Charset() {
		if (_utf8 == null) {
			_utf8 = Charset.forName("UTF-8");;
		}
		return _utf8;
	}
	
	/**
	 * @return the lazily created UTF-8 encoder
	 */
	private CharsetEncoder getUTF8Encoder() {
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
		_buffers.clear();
		_buffers.add(allocateNewBuffer());
		_position = 0;
		_size = 0;
	}
	
	/**
	 * Puts a byte into the buffer at the current write position
	 * and increases write position accordingly.
	 * @param b the byte to put
	 */
	public void putByte(byte b) {
		putByte(_position, b);
		incPosition();
	}
	
	/**
	 * Puts a byte into the buffer at the given position. Does
	 * not increase the write position.
	 * @param pos the position where to put the byte
	 * @param b the byte to put
	 */
	public void putByte(int pos, byte b) {
		ByteBuffer bb = getBuffer(pos);
		int i = pos % _bufferSize;
		bb.put(i, b);
	}
	
	/**
	 * Puts a 32-bit integer into the buffer at the current write position
	 * and increases write position accordingly.
	 * @param i the integer to put
	 */
	public void putInt32(int i) {
		putInt32(_position, i);
		incPosition(4);
	}
	
	/**
	 * Puts a 32-bit integer into the buffer at the given position. Does
	 * not increase the write position.
	 * @param pos the position where to put the integer
	 * @param i the integer to put
	 */
	public void putInt32(int pos, int i) {
		ByteBuffer bb = getBuffer(pos);
		if (bb.remaining() >= 4) {
			int index = pos % _bufferSize;
			bb.putInt(index, i);
		} else {
			byte b0 = (byte)i;
			byte b1 = (byte)(i >> 8);
			byte b2 = (byte)(i >> 16);
			byte b3 = (byte)(i >> 24);
			
			if (_order == ByteOrder.BIG_ENDIAN) {
				putByte(pos, b3);
				putByte(pos + 1, b2);
				putByte(pos + 2, b1);
				putByte(pos + 3, b0);
			} else {
				putByte(pos, b0);
				putByte(pos + 1, b1);
				putByte(pos + 2, b2);
				putByte(pos + 3, b3);
			}
		}
	}
	
	/**
	 * Puts a 64-bit integer into the buffer at the current write position
	 * and increases write position accordingly.
	 * @param b the byte to put
	 */
	public void putInt64(long l) {
		putInt64(_position, l);
		incPosition(8);
	}
	
	/**
	 * Puts a 64-bit integer into the buffer at the given position. Does
	 * not increase the write position.
	 * @param pos the position where to put the integer
	 * @param i the integer to put
	 */
	public void putInt64(int pos, long l) {
		ByteBuffer bb = getBuffer(pos);
		if (bb.remaining() >= 8) {
			int index = pos % _bufferSize;
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
				putByte(pos, b7);
				putByte(pos + 1, b6);
				putByte(pos + 2, b5);
				putByte(pos + 3, b4);
				putByte(pos + 4, b3);
				putByte(pos + 5, b2);
				putByte(pos + 6, b1);
				putByte(pos + 7, b0);
			} else {
				putByte(pos, b0);
				putByte(pos + 1, b1);
				putByte(pos + 2, b2);
				putByte(pos + 3, b3);
				putByte(pos + 4, b4);
				putByte(pos + 5, b5);
				putByte(pos + 6, b6);
				putByte(pos + 7, b7);
			}
		}
	}
	
	/**
	 * Encodes the given string as UTF-8, puts it into the buffer
	 * and increases write position accordingly.
	 * @param s the string to put
	 * @return the number of UTF-8 bytes put
	 */
	public int putUTF8(String s) {
		int written = putUTF8(_position, s);
		incPosition(written);
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
			
			//flush minibb first
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
					//exceeded buffer boundaries; write to a small temporary buffer
					if (minibb == null) {
						minibb = ByteBuffer.allocate(4);
					}
					minibb.rewind();
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
		
		return pos2 - pos;
	}
	
	/**
	 * Writes the whole buffer to the given output stream
	 * @param out the output stream to write to
	 * @throws IOException if the buffer could not be written
	 */
	public void writeTo(OutputStream out) throws IOException {
		writeTo(Channels.newChannel(out));
	}
	
	/**
	 * Writes the whole buffer to the given channel
	 * @param out the channel to write to
	 * @throws IOException if the buffer could not be written
	 */
	public void writeTo(WritableByteChannel out) throws IOException {
		int toWrite = _size;
		int n = 0;
		while (toWrite > 0) {
			int curWrite = Math.min(toWrite, _bufferSize);
			ByteBuffer bb = _buffers.get(n);
			bb.position(curWrite);
			bb.flip();
			out.write(bb);
			++n;
			toWrite -= curWrite;
		}
	}
}
