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

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link UnsafeByteArrayInputStream}
 * @author Michel Kraemer
 */
public class UnsafeByteArrayInputStreamTest {
	private static final byte[] BUF = "abcdefghijklmnopqrstuvwxyz\0\0".getBytes();
	static {
		BUF[BUF.length - 1] = (byte)0xff;
	}
	private UnsafeByteArrayInputStream _in;
	
	@Before
	public void setUp() {
		_in = new UnsafeByteArrayInputStream(BUF);
	}
	
	@Test
	public void available() {
		assertEquals(BUF.length, _in.available());
		_in.read();
		assertEquals(BUF.length - 1, _in.available());
		_in.read(new byte[5], 0, 5);
		assertEquals(BUF.length - 6, _in.available());
	}
	
	@Test
	public void read() {
		assertEquals('a', _in.read());
		assertEquals('b', _in.read());
		assertEquals('c', _in.read());
		
		byte[] b = new byte[10];
		assertEquals(5, _in.read(b, 0, 5));
		assertArrayEquals(Arrays.copyOf("defgh".getBytes(), 10), b);
		assertEquals(5, _in.read(b, 5, 5));
		assertArrayEquals("defghijklm".getBytes(), b);
		
		assertEquals(10, _in.read(b, 0, 10));
		assertArrayEquals("nopqrstuvw".getBytes(), b);
		
		byte[] b2 = new byte[10];
		assertEquals(5, _in.read(b2, 0, 10));
		byte[] expected = Arrays.copyOf("xyz\0".getBytes(), 10);
		expected[4] = (byte)0xff;
		assertArrayEquals(expected, b2);
		
		assertEquals(-1, _in.read());
		assertEquals(-1, _in.read(b2, 0, 10));
	}
	
	@Test
	public void readFF() {
		assertEquals(26, _in.skip(26));
		assertEquals(0, _in.read());
		assertEquals((byte)0xff, (byte)_in.read());
	}
	
	@Test
	public void skip() {
		assertEquals('a', _in.read());
		assertEquals(1, _in.skip(1));
		assertEquals('c', _in.read());
		assertEquals(20, _in.skip(20));
		assertEquals('x', _in.read());
		assertEquals(4, _in.skip(20));
		assertEquals(-1, _in.read());
	}
	
	@Test
	public void offset() {
		_in = new UnsafeByteArrayInputStream(BUF, 3, 3);
		assertEquals('d', _in.read());
		assertEquals('e', _in.read());
		assertEquals('f', _in.read());
		assertEquals(-1, _in.read());
	}
	
	@Test
	public void mark() {
		assertEquals('a', _in.read());
		assertEquals('b', _in.read());
		assertEquals('c', _in.read());
		_in.mark(3);
		assertEquals('d', _in.read());
		assertEquals('e', _in.read());
		assertEquals('f', _in.read());
		_in.reset();
		assertEquals('d', _in.read());
		assertEquals('e', _in.read());
		assertEquals('f', _in.read());
	}
}
