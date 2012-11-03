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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.CharacterCodingException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link LittleEndianInputStream}
 * @author Michel Kraemer
 */
public class LittleEndianInputStreamTest {
	/**
	 * An input stream that contains some test data
	 */
	private ByteArrayInputStream _bais;
	
	/**
	 * The input stream to test
	 */
	private LittleEndianInputStream _leis;
	
	@Before
	public void setUp() {
		byte[] b = new byte[] { (byte)0x66, (byte)0x77, (byte)0x88, (byte)0x99,
				(byte)0xAA, (byte)0xBB, (byte)0xCC, (byte)0xDD, (byte)0x00, (byte)0x01 };
		_bais = new ByteArrayInputStream(b);
		_leis = new LittleEndianInputStream(_bais);
	}
	
	@Test
	public void readByte() throws Exception {
		assertEquals((byte)0x66, _leis.readByte());
	}
	
	@Test
	public void readFully() throws Exception {
		byte[] r1 = new byte[4];
		byte[] r2 = new byte[12];
		_leis.readFully(r1);
		_leis.readFully(r2, 4, 4);
		assertEquals((byte)0x66, r1[0]);
		assertEquals((byte)0x77, r1[1]);
		assertEquals((byte)0x88, r1[2]);
		assertEquals((byte)0x99, r1[3]);
		assertEquals((byte)0xAA, r2[4]);
		assertEquals((byte)0xBB, r2[5]);
		assertEquals((byte)0xCC, r2[6]);
		assertEquals((byte)0xDD, r2[7]);
	}
	
	@Test
	public void skipBytes() throws Exception {
		assertEquals(4, _leis.skip(4));
		assertEquals((byte)0xAA, _leis.readByte());
	}
	
	@Test
	public void readBoolean() throws Exception {
		assertEquals(8, _leis.skipBytes(8));
		assertEquals(false, _leis.readBoolean());
		assertEquals(true, _leis.readBoolean());
	}
	
	@Test
	public void readUnsignedByte() throws Exception {
		assertEquals(4, _leis.skipBytes(4));
		assertEquals(0xAA, _leis.readUnsignedByte());
	}
	
	@Test
	public void readShort() throws Exception {
		assertEquals(4, _leis.skipBytes(4));
		assertEquals((short)0xBBAA, _leis.readShort());
	}
	
	@Test
	public void readUnsignedShort() throws Exception {
		assertEquals(4, _leis.skipBytes(4));
		assertEquals(0xBBAA, _leis.readUnsignedShort());
	}
	
	@Test
	public void readChar() throws Exception {
		assertEquals(4, _leis.skipBytes(4));
		assertEquals((char)0xBBAA, _leis.readChar());
	}
	
	@Test
	public void readInt() throws Exception {
		assertEquals(0x99887766, _leis.readInt());
		assertEquals(0xDDCCBBAA, _leis.readInt());
	}
	
	@Test
	public void readLong() throws Exception {
		assertEquals(0xDDCCBBAA99887766L, _leis.readLong());
	}
	
	@Test
	public void readFloat() throws Exception {
		byte[] b = new byte[4];
		ByteBuffer bb = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
		bb.putFloat(1234.1234f);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		LittleEndianInputStream leis = new LittleEndianInputStream(bais);
		try {
			assertEquals(1234.1234f, leis.readFloat(), 0.00001);
		} finally {
			leis.close();
		}
	}
	
	@Test
	public void readDouble() throws Exception {
		byte[] b = new byte[8];
		ByteBuffer bb = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
		bb.putDouble(1234.1234);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		LittleEndianInputStream leis = new LittleEndianInputStream(bais);
		try {
			assertEquals(1234.1234, leis.readDouble(), 0.00001);
		} finally {
			leis.close();
		}
	}
	
	@Test
	public void readLine() throws Exception {
		byte[] b = new byte[] { 'H', 'e', 'l', 'l', 'o', '\r',
				'W', 'o', 'r', 'l', 'd', '\r', '\n' };
		byte[] b2;
		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		LittleEndianInputStream leis = new LittleEndianInputStream(bais);
		try {
			assertEquals("Hello", leis.readLine());
			assertEquals("World", leis.readLine());
			assertNull(leis.readLine());
			
			b2 = new byte[1024 * 8];
			Arrays.fill(b2, (byte)'a');
		} finally {
			leis.close();
		}

		bais = new ByteArrayInputStream(b2);
		leis = new LittleEndianInputStream(bais);
		try {
			String s = leis.readLine();
			assertNotNull(s);
			byte[] line = s.getBytes();
			assertEquals(b2.length, line.length);
			for (int i = 0; i < b2.length; ++i) {
				assertEquals(b2[i], line[i]);
			}
		} finally {
			leis.close();
		}
	}
	
	private LittleEndianInputStream outputBufferToInputStream(DynamicOutputBuffer db) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		db.writeTo(baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		return new LittleEndianInputStream(bais);
	}
	
	@Test
	public void readUTF() throws Exception {
		DynamicOutputBuffer db = new DynamicOutputBuffer(20);
		db.putUTF8("Helloa\u20AC\u00A2\u00A2bb");
		LittleEndianInputStream leis = outputBufferToInputStream(db);
		assertEquals("Hello", leis.readUTF(5));
		assertEquals("a\u20AC\u00A2\u00A2bb", leis.readUTF(db.size() - 5));
	}
	
	@Test(expected = CharacterCodingException.class)
	public void readUTFError() throws Exception {
		DynamicOutputBuffer db = new DynamicOutputBuffer(20);
		db.putUTF8("a\u00A2");
		LittleEndianInputStream leis = outputBufferToInputStream(db);
		leis.readUTF(2);
	}
	
	@Test
	public void readZeroTerminatedUTF() throws Exception {
		DynamicOutputBuffer db = new DynamicOutputBuffer(20);
		db.putUTF8("Hello");
		db.putByte((byte)0);
		LittleEndianInputStream leis = outputBufferToInputStream(db);
		assertEquals("Hello", leis.readUTF(-1));
	}
}
