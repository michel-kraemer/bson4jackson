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
	private int _pos;
	
	/**
	 * @see FilteredInputStream#FilteredInputStream(InputStream)
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
		if (r > 0) {
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
}
