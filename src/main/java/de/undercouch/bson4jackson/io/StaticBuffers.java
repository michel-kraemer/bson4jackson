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

import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

/**
 * Keeps thread-local re-usable buffers. Each buffer is identified by a key.
 * This class is a singleton, whereas the reference to the instance is held
 * in a {@link SoftReference} so buffers can be freed when they are not needed
 * anymore.
 * @see com.fasterxml.jackson.core.util.BufferRecycler
 * @author Michel Kraemer
 */
public class StaticBuffers {
	/**
	 * All buffers have a minimum size of 64 kb
	 */
	public static final int GLOBAL_MIN_SIZE = 1024 * 64;
	
	/**
	 * Possible buffer keys
	 */
	@SuppressWarnings("javadoc")
	public static enum Key {
		BUFFER0,
		BUFFER1,
		BUFFER2,
		BUFFER3,
		BUFFER4,
		BUFFER5,
		BUFFER6,
		BUFFER7,
		BUFFER8,
		BUFFER9
	}
	
	/**
	 * A thread-local soft reference to the singleton instance of this class
	 */
	protected static final ThreadLocal<SoftReference<StaticBuffers>> _instance =
		new ThreadLocal<SoftReference<StaticBuffers>>();
	
	/**
	 * Maps of already allocated re-usable buffers
	 */
	protected ByteBuffer[] _byteBuffers = new ByteBuffer[Key.values().length];
	protected CharBuffer[] _charBuffers = new CharBuffer[Key.values().length];
	
	/**
	 * Hidden constructor
	 */
	protected StaticBuffers() {
		//nothing to do here
	}
	
	/**
	 * @return a thread-local singleton instance of this class
	 */
	public static StaticBuffers getInstance() {
		SoftReference<StaticBuffers> ref = _instance.get();
		StaticBuffers buf = (ref == null ? null : ref.get());
		if (buf == null) {
			buf = new StaticBuffers();
			_instance.set(new SoftReference<StaticBuffers>(buf));
		}
		return buf;
	}
	
	/**
	 * Creates or re-uses a {@link CharBuffer} that has a minimum size. Calling
	 * this method multiple times with the same key will always return the
	 * same buffer, as long as it has the minimum size and is marked to be
	 * re-used. Buffers that are allowed to be re-used should be released using
	 * {@link #releaseCharBuffer(Key, CharBuffer)}.
	 * @param key the buffer's identifier
	 * @param minSize the minimum size
	 * @return the {@link CharBuffer} instance
	 * @see #byteBuffer(Key, int)
	 */
	public CharBuffer charBuffer(Key key, int minSize) {
		minSize = Math.max(minSize, GLOBAL_MIN_SIZE);
		
		CharBuffer r = _charBuffers[key.ordinal()];
		if (r == null || r.capacity() < minSize) {
			r = CharBuffer.allocate(minSize);
		} else {
			_charBuffers[key.ordinal()] = null;
			r.clear();
		}
		return r;
	}
	
	/**
	 * Marks a buffer a being re-usable.
	 * @param key the buffer's key
	 * @param buf the buffer
	 * @see #releaseByteBuffer(Key, ByteBuffer)
	 */
	public void releaseCharBuffer(Key key, CharBuffer buf) {
		_charBuffers[key.ordinal()] = buf;
	}
	
	/**
	 * Creates or re-uses a {@link ByteBuffer} that has a minimum size. Calling
	 * this method multiple times with the same key will always return the
	 * same buffer, as long as it has the minimum size and is marked to be
	 * re-used. Buffers that are allowed to be re-used should be released using
	 * {@link #releaseByteBuffer(Key, ByteBuffer)}.
	 * @param key the buffer's identifier
	 * @param minSize the minimum size
	 * @return the {@link ByteBuffer} instance
	 * @see #charBuffer(Key, int)
	 */
	public ByteBuffer byteBuffer(Key key, int minSize) {
		minSize = Math.max(minSize, GLOBAL_MIN_SIZE);
		
		ByteBuffer r = _byteBuffers[key.ordinal()];
		if (r == null || r.capacity() < minSize) {
			r = ByteBuffer.allocate(minSize);
		} else {
			_byteBuffers[key.ordinal()] = null;
			r.clear();
		}
		return r;
	}
	
	/**
	 * Marks a buffer a being re-usable.
	 * @param key the buffer's key
	 * @param buf the buffer
	 * @see #releaseCharBuffer(Key, CharBuffer)
	 */
	public void releaseByteBuffer(Key key, ByteBuffer buf) {
		_byteBuffers[key.ordinal()] = buf;
	}
}
