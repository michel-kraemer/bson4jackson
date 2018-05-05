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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;

import org.junit.Test;

/**
 * Tests {@link DynamicOutputBuffer}
 * @author Michel Kraemer
 */
public class DynamicOutputBufferTest {
	@Test
	public void putByteBig() throws Exception {
		DynamicOutputBuffer db = new DynamicOutputBuffer(2);
		assertEquals(0, db.size());
		db.putByte((byte)0xA0);
		assertEquals(1, db.size());
		db.putByte((byte)0xB0);
		assertEquals(2, db.size());
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		db.writeTo(baos);
		byte[] r = baos.toByteArray();
		assertEquals((byte)0xA0, r[0]);
		assertEquals((byte)0xB0, r[1]);
	}
	
	@Test
	public void putByteLittle() throws Exception {
		DynamicOutputBuffer db = new DynamicOutputBuffer(ByteOrder.LITTLE_ENDIAN, 2);
		assertEquals(0, db.size());
		db.putByte((byte)0xA0);
		assertEquals(1, db.size());
		db.putByte((byte)0xB0);
		assertEquals(2, db.size());
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		db.writeTo(baos);
		byte[] r = baos.toByteArray();
		assertEquals((byte)0xA0, r[0]);
		assertEquals((byte)0xB0, r[1]);
	}
	
	@Test
	public void putInt32Big() throws Exception {
		//test the case of a too small initial size
		DynamicOutputBuffer db = new DynamicOutputBuffer(2);
		assertEquals(0, db.size());
		db.putInt(0xAABBCCDD);
		assertEquals(4, db.size());
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		db.writeTo(baos);
		byte[] r = baos.toByteArray();
		assertEquals((byte)0xAA, r[0]);
		assertEquals((byte)0xBB, r[1]);
		assertEquals((byte)0xCC, r[2]);
		assertEquals((byte)0xDD, r[3]);
		
		//test the case of a large initial size
		db = new DynamicOutputBuffer(40);
		assertEquals(0, db.size());
		db.putInt(0xAABBCCDD);
		assertEquals(4, db.size());
		
		baos = new ByteArrayOutputStream();
		db.writeTo(baos);
		r = baos.toByteArray();
		assertEquals((byte)0xAA, r[0]);
		assertEquals((byte)0xBB, r[1]);
		assertEquals((byte)0xCC, r[2]);
		assertEquals((byte)0xDD, r[3]);
	}
	
	@Test
	public void putInt32Little() throws Exception {
		//test the case of a too small initial size
		DynamicOutputBuffer db = new DynamicOutputBuffer(ByteOrder.LITTLE_ENDIAN, 2);
		assertEquals(0, db.size());
		db.putInt(0xAABBCCDD);
		assertEquals(4, db.size());
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		db.writeTo(baos);
		byte[] r = baos.toByteArray();
		assertEquals((byte)0xAA, r[3]);
		assertEquals((byte)0xBB, r[2]);
		assertEquals((byte)0xCC, r[1]);
		assertEquals((byte)0xDD, r[0]);
		
		//test the case of a large initial size
		db = new DynamicOutputBuffer(ByteOrder.LITTLE_ENDIAN, 40);
		assertEquals(0, db.size());
		db.putInt(0xAABBCCDD);
		assertEquals(4, db.size());
		
		baos = new ByteArrayOutputStream();
		db.writeTo(baos);
		r = baos.toByteArray();
		assertEquals((byte)0xAA, r[3]);
		assertEquals((byte)0xBB, r[2]);
		assertEquals((byte)0xCC, r[1]);
		assertEquals((byte)0xDD, r[0]);
	}
	
	@Test
	public void putInt64Big() throws Exception {
		//test the case of a too small initial size
		DynamicOutputBuffer db = new DynamicOutputBuffer(2);
		assertEquals(0, db.size());
		db.putLong(0x66778899AABBCCDDL);
		assertEquals(8, db.size());
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		db.writeTo(baos);
		byte[] r = baos.toByteArray();
		assertEquals((byte)0x66, r[0]);
		assertEquals((byte)0x77, r[1]);
		assertEquals((byte)0x88, r[2]);
		assertEquals((byte)0x99, r[3]);
		assertEquals((byte)0xAA, r[4]);
		assertEquals((byte)0xBB, r[5]);
		assertEquals((byte)0xCC, r[6]);
		assertEquals((byte)0xDD, r[7]);
		
		//test the case of a large initial size
		db = new DynamicOutputBuffer(40);
		assertEquals(0, db.size());
		db.putLong(0x66778899AABBCCDDL);
		assertEquals(8, db.size());
		
		baos = new ByteArrayOutputStream();
		db.writeTo(baos);
		r = baos.toByteArray();
		assertEquals((byte)0x66, r[0]);
		assertEquals((byte)0x77, r[1]);
		assertEquals((byte)0x88, r[2]);
		assertEquals((byte)0x99, r[3]);
		assertEquals((byte)0xAA, r[4]);
		assertEquals((byte)0xBB, r[5]);
		assertEquals((byte)0xCC, r[6]);
		assertEquals((byte)0xDD, r[7]);
	}
	
	@Test
	public void putInt64Little() throws Exception {
		//test the case of a too small initial size
		DynamicOutputBuffer db = new DynamicOutputBuffer(ByteOrder.LITTLE_ENDIAN, 2);
		assertEquals(0, db.size());
		db.putLong(0x66778899AABBCCDDL);
		assertEquals(8, db.size());
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		db.writeTo(baos);
		byte[] r = baos.toByteArray();
		assertEquals((byte)0x66, r[7]);
		assertEquals((byte)0x77, r[6]);
		assertEquals((byte)0x88, r[5]);
		assertEquals((byte)0x99, r[4]);
		assertEquals((byte)0xAA, r[3]);
		assertEquals((byte)0xBB, r[2]);
		assertEquals((byte)0xCC, r[1]);
		assertEquals((byte)0xDD, r[0]);
		
		//test the case of a large initial size
		db = new DynamicOutputBuffer(ByteOrder.LITTLE_ENDIAN, 40);
		assertEquals(0, db.size());
		db.putLong(0x66778899AABBCCDDL);
		assertEquals(8, db.size());
		
		baos = new ByteArrayOutputStream();
		db.writeTo(baos);
		r = baos.toByteArray();
		assertEquals((byte)0x66, r[7]);
		assertEquals((byte)0x77, r[6]);
		assertEquals((byte)0x88, r[5]);
		assertEquals((byte)0x99, r[4]);
		assertEquals((byte)0xAA, r[3]);
		assertEquals((byte)0xBB, r[2]);
		assertEquals((byte)0xCC, r[1]);
		assertEquals((byte)0xDD, r[0]);
	}
	
	@Test
	public void putUTF8() throws Exception {
		DynamicOutputBuffer db = new DynamicOutputBuffer(2);
		int w = db.putUTF8("Hello");
		assertEquals(5, w);
		assertEquals(5, db.size());
		
		db = new DynamicOutputBuffer(10);
		w = db.putUTF8("Hello");
		assertEquals(5, w);
		assertEquals(5, db.size());
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		db.writeTo(baos);
		String s = new String(baos.toByteArray());
		assertEquals("Hello", s);
		
		db = new DynamicOutputBuffer(2);
		w = db.putUTF8("a\u20AC\u00A2\u00A2bb");
		assertEquals(10, w);
		assertEquals(10, db.size());
		
		baos = new ByteArrayOutputStream();
		db.writeTo(baos);
		byte[] r = baos.toByteArray();
		assertEquals('a', r[0]);
		
		assertEquals((byte)0xE2, r[1]);
		assertEquals((byte)0x82, r[2]);
		assertEquals((byte)0xAC, r[3]);
		
		assertEquals((byte)0xC2, r[4]);
		assertEquals((byte)0xA2, r[5]);
		
		assertEquals((byte)0xC2, r[6]);
		assertEquals((byte)0xA2, r[7]);
		
		assertEquals('b', r[8]);
		assertEquals('b', r[9]);
	}
	
	@Test
	public void putRandom() throws Exception {
		DynamicOutputBuffer db = new DynamicOutputBuffer(2);
		db.putByte(5, (byte)0);
		db.putByte(1, (byte)1);
		db.putByte(4, (byte)2);
		db.putByte(3, (byte)3);
		db.putByte(2, (byte)4);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		db.writeTo(baos);
		byte[] r = baos.toByteArray();
		assertEquals(0, r[0]);
		assertEquals(0, r[5]);
		assertEquals(1, r[1]);
		assertEquals(2, r[4]);
		assertEquals(3, r[3]);
		assertEquals(4, r[2]);
	}
	
	@Test
	public void putFloatingPoint() throws Exception {
		DynamicOutputBuffer db = new DynamicOutputBuffer(2);
		db.putFloat(1234.1234f);
		db.putDouble(5678.5678);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		db.writeTo(baos);
		byte[] r = baos.toByteArray();
		ByteBuffer bb = ByteBuffer.wrap(r);
		assertEquals(1234.1234f, bb.getFloat(), 0.00001);
		assertEquals(5678.5678, bb.getDouble(), 0.00001);
	}
	
	@Test
	public void flush() throws Exception {
		DynamicOutputBuffer db = new DynamicOutputBuffer(2);
		db.putByte((byte)1);
		db.putByte((byte)2);
		db.putByte((byte)3);
		db.putByte((byte)4);
		db.putByte((byte)5);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		db.flushTo(baos);
		byte[] r = baos.toByteArray();
		assertEquals(4, r.length);
		assertEquals(1, r[0]);
		assertEquals(2, r[1]);
		assertEquals(3, r[2]);
		assertEquals(4, r[3]);
		
		db.putByte((byte)6);
		db.flushTo(baos);
		r = baos.toByteArray();
		assertEquals(6, r.length);
		assertEquals(5, r[4]);
		assertEquals(6, r[5]);
		
		db.putByte((byte)7);
		db.writeTo(baos);
		r = baos.toByteArray();
		assertEquals(7, r.length);
		assertEquals(7, r[6]);
	}
	
	@Test(expected = java.lang.IndexOutOfBoundsException.class)
	public void putAfterWrite() throws Exception {
		DynamicOutputBuffer db = new DynamicOutputBuffer(2);
		db.putByte((byte)1);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		db.writeTo(baos);
		db.putByte((byte)8);
	}
	
	@Test
	public void putString() throws Exception {
		DynamicOutputBuffer db = new DynamicOutputBuffer(2);
		db.putString("Hello");
		assertEquals(10, db.size());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		db.writeTo(baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ReadableByteChannel channel = Channels.newChannel(bais);
		ByteBuffer buf = ByteBuffer.allocate(10);
		channel.read(buf);
		buf.flip();
		CharBuffer cbuf = buf.asCharBuffer();
		char[] c = new char[5];
		cbuf.get(c);
		String s = String.valueOf(c);
		assertEquals("Hello", s);
	}

	/**
	 * Test if the internal mini buffer is correctly cleared on overflow. If
	 * not, this test will cause an infinite loop. See issue #79.
	 */
	@Test
	public void bufferAllocOnOverflow() {
		final int SIZE = DynamicOutputBuffer.DEFAULT_BUFFER_SIZE - 1;

		char[] chars = new char[SIZE + SIZE];

		Arrays.fill(chars, 0, SIZE, 'A');
		chars[SIZE] = (char)2047; // represented with 2 bytes in UTF8
		chars[SIZE + 1] = (char)2048; // represented with 3 bytes in UTF8

		Arrays.fill(chars, SIZE + 2, 2 * SIZE - 2, 'B');
		chars[2 * SIZE - 2] = (char)2048;
		chars[2 * SIZE - 1] = (char)2048;

		DynamicOutputBuffer buff = new DynamicOutputBuffer();
		int nbytes = buff.putUTF8(String.valueOf(chars));

		// sizeof(bytes) - sizeof(chars) should be 7 because 2047 adds 1 extra
		// byte and 3 chars of 2048 add 6 extra bytes in total
		assertEquals("Unexpected number of written bytes", chars.length + 7, nbytes);
	}
}
