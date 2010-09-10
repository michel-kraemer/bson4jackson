package de.undercouch.bson4jackson;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bson.BSONDecoder;
import org.bson.BSONObject;
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
}
