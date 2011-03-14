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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Extends {@link BufferedInputStream}, but uses a a re-usable static buffer
 * provided by {@link StaticBuffers} to achieve better performance.
 * @author Michel Kraemer
 */
public class StaticBufferedInputStream extends BufferedInputStream {
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
	private ByteBuffer _byteBuffer;
	
	/**
	 * @see BufferedInputStream#BufferedInputStream(InputStream)
	 */
	public StaticBufferedInputStream(InputStream in) {
		this(in, 8192);
	}
	
	/**
	 * @see BufferedInputStream#BufferedInputStream(InputStream, int)
	 */
	public StaticBufferedInputStream(InputStream in, int size) {
		//super constructor wants to allocate a buffer. try to allocate
		//as few bytes as possible
		super(in, 1);
		
		//replace buffer allocated by super constructor
		_staticBuffers = StaticBuffers.getInstance();
		_byteBuffer = _staticBuffers.byteBuffer(BUFFER_KEY, size);
		buf = _byteBuffer.array();
	}
	
	@Override
	public void close() throws IOException {
		_staticBuffers.releaseByteBuffer(BUFFER_KEY, _byteBuffer);
		super.close();
	}
}
