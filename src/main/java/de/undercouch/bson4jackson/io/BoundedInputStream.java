// Copyright 2010-2011 James Roper
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
 * Input stream that bounds an underlying input stream to a particular size.
 * This class is not threadsafe.
 * @author James Roper
 */
public class BoundedInputStream extends FilterInputStream {
	private final int size;
	private int count = 0;
	private boolean eof = false;
	private int mark;

	/**
	 * Wraps another stream by this one
	 * @param in the stream to wrap
	 * @param size the stream's size
	 */
	public BoundedInputStream(InputStream in, int size) {
		super(in);
		this.size = size;
	}

	@Override
	public synchronized int read() throws IOException {
		if (!eof && count < size) {
			int read = super.read();
			if (read == -1) {
				eof = true;
			} else {
				count++;
			}
			return read;
		}
		return -1;
	}

	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (eof || count >= size) {
			return -1;
		} else {
			// Bound length by what's remaining
			len = Math.min(len, size - count);
			int read = super.read(b, off, len);
			if (read == -1) {
				eof = true;
			} else {
				count += read;
			}
			return read;
		}
	}

	@Override
	public long skip(long n) throws IOException {
		n = Math.min(n, size - count);
		long skipped = super.skip(n);
		count += skipped;
		return skipped;
	}

	@Override
	public int available() throws IOException {
		return Math.min(super.available(), size - count);
	}

	@Override
	public void mark(int readlimit) {
		mark = count;
		super.mark(readlimit);
	}

	@Override
	public void reset() throws IOException {
		if (super.markSupported()) {
			count = mark;
		}
		super.reset();
	}
}
