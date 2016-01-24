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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reads from another input stream, but counts the number of
 * bytes read or skipped (i.e. saves the current buffer position).
 * @author Michel Kraemer
 */
public class CountingInputStream extends FilterInputStream {
	/**
	 * The current buffer position
	 */
	protected int _pos;
	
	/**
	 * The position in the buffer the last time {@link #mark(int)} was called
	 */
	protected int _markpos = -1;
	
	/**
	 * @see FilterInputStream#FilterInputStream(InputStream)
	 */
	public CountingInputStream(InputStream in) {
		super(in);
	}
	
	/**
	 * @return the number of bytes read or skipped
	 */
	public int getPosition() {
		return _pos;
	}

	@Override
	public int read() throws IOException {
		int r = super.read();
		if (r >= 0) {
			++_pos;
		}
		return r;
	}
	
	@Override
	public int read(byte b[]) throws IOException {
		int r = super.read(b);
		if (r > 0) {
			_pos += r;
		}
		return r;
	}
	
	@Override
	public int read(byte b[], int off, int len) throws IOException {
		int r = super.read(b, off, len);
		if (r > 0) {
			_pos += r;
		}
		return r;
	}
	
	@Override
	public long skip(long n) throws IOException {
		long r = super.skip(n);
		if (r > 0) {
			_pos += r;
		}
		return r;
	}
	
	@Override
	public synchronized void mark(int readlimit) {
		_markpos = _pos;
		super.mark(readlimit);
	}
	
	@Override
	public synchronized void reset() throws IOException {
		super.reset();
		_pos = _markpos;
	}
}
