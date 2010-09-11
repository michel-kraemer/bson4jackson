package de.undercouch.bson4jackson;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bson.BSONDecoder;
import org.bson.BSONObject;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

/**
 * Tests {@link BsonGenerator}
 * @author Michel Kraemer
 */
public class BsonGeneratorTest {
	@Test
	public void generatePrimitives() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		data.put("Int32", 5);
		data.put("Boolean1", true);
		data.put("Boolean2", false);
		data.put("String", "Hello");
		data.put("Long", 1234L);
		data.put("Null", null);
		data.put("Float", 1234.1234f);
		data.put("Double", 5678.5678);
		
		//BigIntegers will be serialized as Strings, since the standard
		//serializer (StdSerializers#NumberSerializer) does not handle
		//them correctly
		data.put("BigInt", BigInteger.valueOf(Integer.MAX_VALUE));
		
		ObjectMapper om = new ObjectMapper(new BsonFactory());
		om.writeValue(baos, data);
		
		assertEquals(130, baos.size());
		
		byte[] r = baos.toByteArray();
		ByteArrayInputStream bais = new ByteArrayInputStream(r);
		
		BSONDecoder decoder = new BSONDecoder();
		BSONObject obj = decoder.readObject(bais);
		assertEquals(5, obj.get("Int32"));
		assertEquals(true, obj.get("Boolean1"));
		assertEquals(false, obj.get("Boolean2"));
		assertEquals("Hello", obj.get("String"));
		assertEquals(1234L, obj.get("Long"));
		assertEquals(null, obj.get("Null"));
		assertEquals(1234.1234f, (Double)obj.get("Float"), 0.00001);
		assertEquals(5678.5678, (Double)obj.get("Double"), 0.00001);
		assertEquals(String.valueOf(Integer.MAX_VALUE), obj.get("BigInt"));
	}
	
	@Test
	public void stream() throws Exception {
		//TODO test streaming
	}
	
	private void assertRaw(byte[] r) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(r);
		BSONDecoder decoder = new BSONDecoder();
		BSONObject obj = decoder.readObject(bais);
		byte[] o = (byte[])obj.get("Test");
		CharBuffer buf = ByteBuffer.wrap(o).order(ByteOrder.LITTLE_ENDIAN).asCharBuffer();
		assertEquals(2, buf.remaining());
		char a = buf.get();
		char b = buf.get();
		assertEquals('a', a);
		assertEquals('b', b);
	}
	
	@Test
	public void rawChar() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BsonGenerator gen = new BsonGenerator(JsonGenerator.Feature.collectDefaults(), 0, null, baos);
		gen.writeStartObject();
		gen.writeFieldName("Test");
		gen.writeRaw(new char[] { 'a', 'b' }, 0, 2);
		gen.writeEndObject();
		gen.close();
		assertRaw(baos.toByteArray());
	}
	
	@Test
	public void rawString() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BsonGenerator gen = new BsonGenerator(JsonGenerator.Feature.collectDefaults(), 0, null, baos);
		gen.writeStartObject();
		gen.writeFieldName("Test");
		gen.writeRaw("ab");
		gen.writeEndObject();
		gen.close();
		assertRaw(baos.toByteArray());
	}
	
	@Test
	public void rawBytes() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BsonGenerator gen = new BsonGenerator(JsonGenerator.Feature.collectDefaults(), 0, null, baos);
		gen.writeStartObject();
		gen.writeFieldName("Test");
		gen.writeBinary(new byte[] { (byte)1, (byte)2 });
		gen.writeEndObject();
		gen.close();
		
		byte[] r = baos.toByteArray();
		ByteArrayInputStream bais = new ByteArrayInputStream(r);
		BSONDecoder decoder = new BSONDecoder();
		BSONObject obj = decoder.readObject(bais);
		byte[] o = (byte[])obj.get("Test");
		assertEquals(2, o.length);
		assertEquals((byte)1, o[0]);
		assertEquals((byte)2, o[1]);
	}
	
	@Test
	public void stackedObjects() throws Exception {
		Map<String, Object> data1 = new LinkedHashMap<String, Object>();
		data1.put("Int32", 5);
		Map<String, Object> data3 = new LinkedHashMap<String, Object>();
		data3.put("String", "Hello");
		
		Map<String, Object> data2 = new LinkedHashMap<String, Object>();
		data2.put("Int64", 10L);
		data2.put("data1", data1);
		data2.put("data3", data3);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectMapper om = new ObjectMapper(new BsonFactory());
		om.writeValue(baos, data2);
		
		byte[] r = baos.toByteArray();
		ByteArrayInputStream bais = new ByteArrayInputStream(r);
		
		BSONDecoder decoder = new BSONDecoder();
		BSONObject obj2 = decoder.readObject(bais);
		assertEquals(10L, obj2.get("Int64"));
		BSONObject obj1 = (BSONObject)obj2.get("data1");
		assertEquals(5, obj1.get("Int32"));
		BSONObject obj3 = (BSONObject)obj2.get("data3");
		assertEquals("Hello", obj3.get("String"));
	}
}
