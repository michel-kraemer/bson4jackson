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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Works like {@link java.io.BufferedInputStream}, but is not thread-safe and
 * also uses a a re-usable static buffer provided by {@link StaticBuffers} to
 * achieve better performance
 * @author Michel Kraemer
 */
public class StaticBufferedInputStream extends InputStream {
	/**
	 * A unique key for the re-usable buffer
	 */
	private static final StaticBuffers.Key BUFFER_KEY = StaticBuffers.Key.BUFFER1;
	
	/**
	 * Provides re-usable buffers
	 */
	private final StaticBuffers _staticBuffers;
	
	/**
	 * A re-usable buffer
	 */
	private final ByteBuffer _byteBuffer;
	
	/**
	 * The raw re-usable buffer
	 */
	private final byte[] _raw;
	
	/**
	 * The original unbuffered input stream
	 */
	private final InputStream _in;
	
	/**
	 * The current read position
	 */
	private int _pos;
	
	/**
	 * The number of bytes in the buffer
	 */
	private int _count;
	
	/**
	 * The current marked position. -1 means no mark.
	 */
	private int _mark = -1;
	
	/**
	 * Creates a new buffered input stream
	 * @param in the original unbuffered input stream
	 */
	public StaticBufferedInputStream(InputStream in) {
		this(in, 8192);
	}
	
	/**
	 * Creates a new buffered input stream
	 * @param in the original unbuffered input stream
	 * @param size the minimum buffer size
	 */
	public StaticBufferedInputStream(InputStream in, int size) {
		_in = in;
		_staticBuffers = StaticBuffers.getInstance();
		_byteBuffer = _staticBuffers.byteBuffer(BUFFER_KEY, size);
		_raw = _byteBuffer.array();
	}
	
	@Override
	public void close() throws IOException {
		_staticBuffers.releaseByteBuffer(BUFFER_KEY, _byteBuffer);
		super.close();
	}
	
	private void fill() throws IOException {
		if (_mark < 0) {
			//there is no mark
			_pos = 0;
		} else if (_pos >= _raw.length) {
			if (_mark > 0) {
				int cnt = _pos - _mark;
				System.arraycopy(_raw, _mark, _raw, 0, cnt);
				_pos = cnt;
				_mark = 0;
			} else {
				//we've read too many data already
				_mark = -1;
				_pos = 0;
			}
		}
		
		_count = _pos;
		int n = _in.read(_raw, _pos, _raw.length - _pos);
		if (n > 0) {
			_count += n;
		}
	}

	@Override
	public int read() throws IOException {
		if (_pos >= _count) {
			fill();
			if (_pos >= _count) {
				return -1;
			}
		}
		return _raw[_pos++];
	}
	
	@Override
	public int read(byte b[], int off, int len) throws IOException {
		if (len == 0) {
			return 0;
		}
		
		int read = 0;
		while (len > 0) {
			int avail = _count - _pos;
			if (avail <= 0) {
				fill();
				avail = _count - _pos;
				if (avail <= 0) {
					return (read == 0 ? -1 : read);
				}
			}
			
			int cnt = (avail < len ? avail : len);
			System.arraycopy(_raw, _pos, b, off, cnt);
			off += cnt;
			_pos += cnt;
			read += cnt;
			len -= cnt;
		}
		
		return read;
	}
	
	@Override
	public long skip(long n) throws IOException {
		if (n <= 0) {
			return 0;
		}
		
		long avail = _count - _pos;
		if (avail <= 0) {
			return _in.skip(n);
		}
		
		long cnt = (avail < n ? avail : n);
		_pos += cnt;
		return cnt;
	}
	
	@Override
	public boolean markSupported() {
		return true;
	}
	
	@Override
	public void mark(int marklimit) {
		_mark = _pos;
	}
	
	@Override
	public void reset() throws IOException {
		if (_mark < 0) {
			throw new IOException("Invalid mark");
		}
		_pos = _mark;
	}
}
