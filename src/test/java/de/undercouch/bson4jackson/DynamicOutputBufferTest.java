package de.undercouch.bson4jackson;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.nio.ByteOrder;

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
		db.putInt32(0xAABBCCDD);
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
		db.putInt32(0xAABBCCDD);
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
		db.putInt32(0xAABBCCDD);
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
		db.putInt32(0xAABBCCDD);
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
		db.putInt64(0x66778899AABBCCDDL);
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
		db.putInt64(0x66778899AABBCCDDL);
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
		db.putInt64(0x66778899AABBCCDDL);
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
		db.putInt64(0x66778899AABBCCDDL);
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
}
