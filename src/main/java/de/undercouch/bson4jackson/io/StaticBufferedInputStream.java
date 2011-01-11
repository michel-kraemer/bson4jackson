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
