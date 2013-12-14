// Copyright 2010-2011 Michel Kraemer
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package de.undercouch.bson4jackson.io;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

/**
 * Works like {@link DataInputStream} but reads values using
 * little-endian encoding. Apart from that, it provides a method
 * that reads an UTF-8 encoded string without reading the number of
 * bytes from the input stream.
 * @author Michel Kraemer
 */
public class LittleEndianInputStream extends FilterInputStream implements DataInput {
	/**
	 * A unique key for a buffer used in {@link #readUTF(DataInput, int)}
	 */
	private static final StaticBuffers.Key UTF8_BUFFER = StaticBuffers.Key.BUFFER0;
	
	/**
	 * A small buffer to speed up reading slightly
	 */
	private byte[] _rawBuf;
	
	/**
	 * Wraps around {@link #_rawBuf}
	 */
	private ByteBuffer _buf;
	
	/**
	 * A buffer that will lazily be initialized by {@link #readLine()}
	 */
	private CharBuffer _lineBuffer;
	
	/**
	 * @see FilterInputStream#FilterInputStream(InputStream)
	 */
	public LittleEndianInputStream(InputStream in) {
		super(in);
		_rawBuf = new byte[8];
		_buf = ByteBuffer.wrap(_rawBuf).order(ByteOrder.LITTLE_ENDIAN);
	}
	
	@Override
	public void readFully(byte[] b) throws IOException {
		readFully(b, 0, b.length);
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		while (len > 0) {
			int r = read(b, off, len);
			if (r < 0) {
				throw new EOFException();
			}
			len -= r;
			off += r;
		}
	}

	@Override
	public int skipBytes(int n) throws IOException {
		int r = 0;
		while (n > 0) {
			long s = skip(n);
			if (s <= 0) {
				break;
			}
			r += s;
			n -= s;
		}
		return r;
	}

	@Override
	public boolean readBoolean() throws IOException {
		return (readByte() != 0);
	}

	@Override
	public byte readByte() throws IOException {
		return (byte)readUnsignedByte();
	}

	@Override
	public int readUnsignedByte() throws IOException {
		int r = read();
		if (r < 0) {
			throw new EOFException();
		}
		return r;
	}

	@Override
	public short readShort() throws IOException {
		return (short)readUnsignedShort();
	}

	@Override
	public int readUnsignedShort() throws IOException {
		int r1 = readUnsignedByte();
		int r2 = readUnsignedByte();
		return (r1 + (r2 << 8));
	}

	@Override
	public char readChar() throws IOException {
		return (char)readUnsignedShort();
	}

	@Override
	public int readInt() throws IOException {
		readFully(_rawBuf, 0, 4);
		return _buf.getInt(0);
	}

	@Override
	public long readLong() throws IOException {
		readFully(_rawBuf, 0, 8);
		return _buf.getLong(0);
	}

	@Override
	public float readFloat() throws IOException {
		readFully(_rawBuf, 0, 4);
		return _buf.getFloat(0);
	}

	@Override
	public double readDouble() throws IOException {
		readFully(_rawBuf, 0, 8);
		return _buf.getDouble(0);
	}

	@Override
	public String readLine() throws IOException {
		int bufSize = 0;
		if (_lineBuffer != null) {
			_lineBuffer.rewind();
			_lineBuffer.limit(_lineBuffer.capacity());
			bufSize = _lineBuffer.capacity();
		}
		
		int c;
		while (true) {
			c = read();
			if (c < 0 || c == '\n') {
				break;
			} else if (c == '\r') {
				int c2 = read();
				if (c2 != -1 && c2 != '\n') {
					if (!(in instanceof PushbackInputStream)) {
						in = new PushbackInputStream(in);
					}
					((PushbackInputStream)in).unread(c2);
				}
				break;
			} else {
				if (_lineBuffer == null || _lineBuffer.remaining() == 0) {
					int newBufSize = bufSize + 128;
					CharBuffer newBuf = CharBuffer.allocate(newBufSize);
					if (_lineBuffer != null) {
						_lineBuffer.flip();
						newBuf.put(_lineBuffer);
					}
					_lineBuffer = newBuf;
					bufSize = newBufSize;
				}
				_lineBuffer.put((char)c);
			}
		}
		if (c < 0 && (_lineBuffer == null || _lineBuffer.position() == 0)) {
			return null;
		}
		return String.valueOf(_lineBuffer.array(), 0, _lineBuffer.position());
	}

	/**
	 * <p>Forwards to {@link DataInputStream#readUTF(DataInput)} which expects
	 * a short value at the beginning of the UTF-8 string that specifies
	 * the number of bytes to read.</p>
	 * <p>If the output stream does no include such a short value, use
	 * {@link #readUTF(int)} to explicitly specify the number of bytes.</p>
	 */
	@Override
	public String readUTF() throws IOException {
		return DataInputStream.readUTF(this);
	}
	
	/**
	 * Reads a modified UTF-8 string
	 * @param len the number of bytes to read (please do not mix that up
	 * with the number of characters!). If this is -1 then the method
	 * will read bytes until the first one is zero (0x00). The zero
	 * byte will not be included in the result string.
	 * @return the UTF-8 string
	 * @throws IOException if an I/O error occurs
	 * @throws CharacterCodingException if an invalid UTF-8 character
	 * has been read
	 */
	public String readUTF(int len) throws IOException {
		return readUTF(this, len);
	}
	
	/**
	 * Reads a modified UTF-8 string from a DataInput object
	 * @param input the DataInput object to read from
	 * @param len the number of bytes to read (please do not mix that up
	 * with the number of characters!). If this is -1 then the method
	 * will read bytes until the first one is zero (0x00). The zero
	 * byte will not be included in the result string.
	 * @return the UTF-8 string
	 * @throws IOException if an I/O error occurs
	 * @throws CharacterCodingException if an invalid UTF-8 character
	 * has been read
	 */
	public String readUTF(DataInput input, int len) throws IOException {
		StaticBuffers staticBuffers = StaticBuffers.getInstance();
		
		ByteBuffer utf8buf = staticBuffers.byteBuffer(UTF8_BUFFER, 1024 * 8);
		byte[] rawUtf8Buf = utf8buf.array();

		CharsetDecoder dec = Charset.forName("UTF-8").newDecoder();
		int expectedLen = (len > 0 ? (int)(dec.averageCharsPerByte() * len) + 1 : 1024);
		CharBuffer cb = staticBuffers.charBuffer(UTF8_BUFFER, expectedLen);
		try {
			while (len != 0 || utf8buf.position() > 0) {
				//read as much as possible
				if (len < 0) {
					//read until the first zero byte
					while (utf8buf.remaining() > 0) {
						byte b = input.readByte();
						if (b == 0) {
							len = 0;
							break;
						}
						utf8buf.put(b);
					}
					utf8buf.flip();
				} else if (len > 0) {
					int r = Math.min(len, utf8buf.remaining());
					input.readFully(rawUtf8Buf, utf8buf.position(), r);
					len -= r;
					utf8buf.limit(utf8buf.position() + r);
					utf8buf.rewind();
				} else {
					utf8buf.flip();
				}
	
				//decode byte buffer
				CoderResult cr = dec.decode(utf8buf, cb, len == 0);
				if (cr.isUnderflow()) {
					//too few input bytes. move rest of the buffer
					//to the beginning and then try again
					utf8buf.compact();
				} else if (cr.isOverflow()) {
					//output buffer to small. enlarge buffer and try again
					utf8buf.compact();
					
					//create a new char buffer with the same key
					CharBuffer newBuf = staticBuffers.charBuffer(UTF8_BUFFER,
							cb.capacity() + 1024);
					
					cb.flip();
					newBuf.put(cb);
					cb = newBuf;
				} else if (cr.isError()) {
					cr.throwException();
				}
			}
		} finally {
			staticBuffers.releaseCharBuffer(UTF8_BUFFER, cb);
			staticBuffers.releaseByteBuffer(UTF8_BUFFER, utf8buf);
		}
		
		cb.flip();
		return cb.toString();
	}
}
