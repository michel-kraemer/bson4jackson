// Copyright 2010-2014 Michel Kraemer
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link StaticBufferedInputStream}
 * @author Michel Kraemer
 */
public class StaticBufferedInputStreamTest {
	private static final byte[] BUF = "abcdefghijklmnopqrstuvwxyz\0\u00ff".getBytes();
	private StaticBufferedInputStream _in;
	
	@Before
	public void setUp() {
		_in = new StaticBufferedInputStream(new ByteArrayInputStream(BUF), 2);
	}
	
	@After
	public void tearDown() throws IOException {
		_in.close();
	}
	
	@Test
	public void read() throws IOException {
		assertEquals('a', _in.read());
		assertEquals('b', _in.read());
		assertEquals('c', _in.read());
		assertEquals('d', _in.read());
	}
	
	@Test
	public void mark() throws IOException {
		assertEquals('a', _in.read());
		_in.mark(2);
		assertEquals('b', _in.read());
		_in.reset();
		assertEquals('b', _in.read());
	}
	
	@Test
	public void readBuf() throws IOException {
		byte[] buf = new byte[10];
		assertEquals(5, _in.read(buf, 0, 5));
		assertArrayEquals(Arrays.copyOf("abcde".getBytes(), 10), buf);
		assertEquals(5, _in.read(buf, 5, 5));
		assertArrayEquals("abcdefghij".getBytes(), buf);
		assertEquals(10, _in.read(buf, 0, 10));
		assertEquals('u', _in.read());
		assertEquals(5, _in.read(buf, 0, 5));
		assertEquals(0, _in.read());
		assertEquals((byte)0xff, _in.read());
		assertEquals(-1, _in.read());
		assertEquals(-1, _in.read(buf, 0, 10));
	}
	
	@Test
	public void skip() throws IOException {
		assertEquals('a', _in.read());
		assertEquals(1, _in.skip(1));
		assertEquals('c', _in.read());
		assertEquals(1, _in.skip(1));
		assertEquals('e', _in.read());
		assertEquals(20, _in.skip(20));
		assertEquals('z', _in.read());
		assertEquals(0, _in.read());
		assertEquals(1, _in.skip(20));
		assertEquals(-1, _in.read());
		assertEquals(0, _in.skip(20));
	}
	
	private int makeLargeIn() throws IOException {
		_in.close();
		
		int n = StaticBuffers.GLOBAL_MIN_SIZE * 3 / 2;
		StringBuilder bigStr = new StringBuilder();
		for (int i = 0; i < n; ++i) {
			bigStr.append("abc");
		}
		
		_in = new StaticBufferedInputStream(new ByteArrayInputStream(
				bigStr.toString().getBytes()), 2);
		return n;
	}
	
	@Test
	public void readLarge1() throws IOException {
		int n = makeLargeIn();
		for (int i = 0; i < n; ++i) {
			assertEquals('a', _in.read());
			assertEquals('b', _in.read());
			assertEquals('c', _in.read());
		}
		assertEquals(-1, _in.read());
	}

	@Test
	public void readLarge2() throws IOException {
		int n = makeLargeIn();
		byte[] b = new byte[3];
		for (int i = 0; i < n; ++i) {
			assertEquals(3, _in.read(b));
			assertArrayEquals("abc".getBytes(), b);
		}
		assertEquals(-1, _in.read());
	}
	
	@Test
	public void markLarge() throws IOException {
		int n = makeLargeIn();
		byte[] b = new byte[30];
		_in.read(b);
		assertEquals('a', _in.read());
		_in.mark(n);
		assertEquals('b', _in.read());
		_in.reset();
		assertEquals('b', _in.read());
		
		for (int i = 0; i < StaticBuffers.GLOBAL_MIN_SIZE / 3; ++i) {
			assertEquals('c', _in.read());
			assertEquals('a', _in.read());
			assertEquals('b', _in.read());
		}
		
		_in.reset();
		assertEquals('b', _in.read());
	}
	
	@Test
	public void markLargeValid() throws IOException {
		int n = makeLargeIn();
		_in.mark(n);
		for (int i = 0; i < StaticBuffers.GLOBAL_MIN_SIZE / 3; ++i) {
			assertEquals('a', _in.read());
			assertEquals('b', _in.read());
			assertEquals('c', _in.read());
		}
		_in.reset();
		for (int i = 0; i < n; ++i) {
			assertEquals('a', _in.read());
			assertEquals('b', _in.read());
			assertEquals('c', _in.read());
		}
		assertEquals(-1, _in.read());
	}
	
	@Test(expected = IOException.class)
	public void markLargeInvalid() throws IOException {
		int n = makeLargeIn();
		_in.mark(n);
		for (int i = 0; i < n; ++i) {
			assertEquals('a', _in.read());
			assertEquals('b', _in.read());
			assertEquals('c', _in.read());
		}
		//will throw because we've read beyond the buffer size
		_in.reset();
	}
}
