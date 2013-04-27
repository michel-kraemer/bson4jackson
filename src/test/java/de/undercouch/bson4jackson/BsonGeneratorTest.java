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

package de.undercouch.bson4jackson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bson.BSONDecoder;
import org.bson.BSONObject;
import org.bson.BasicBSONDecoder;
import org.bson.types.BSONTimestamp;
import org.bson.types.Code;
import org.bson.types.CodeWScope;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.undercouch.bson4jackson.io.DynamicOutputBuffer;
import de.undercouch.bson4jackson.types.JavaScript;
import de.undercouch.bson4jackson.types.ObjectId;
import de.undercouch.bson4jackson.types.Timestamp;

/**
 * Tests {@link BsonGenerator}
 * @author Michel Kraemer
 */
public class BsonGeneratorTest {
	@Test
	public void generatePrimitives() throws Exception {
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		data.put("Int32", 5);
		data.put("Boolean1", true);
		data.put("Boolean2", false);
		data.put("String", "Hello");
		data.put("Long", 1234L);
		data.put("Null", null);
		data.put("Float", 1234.1234f);
		data.put("Double", 5678.5678);
		
		//BigInteger that can be serialized as an Integer
		data.put("BigInt1", BigInteger.valueOf(Integer.MAX_VALUE));
		
		//BigInteger that can be serialized as a Long
		BigInteger bi2 = BigInteger.valueOf(Integer.MAX_VALUE)
			.multiply(BigInteger.valueOf(2));
		data.put("BigInt2", bi2);
		
		//BigInteger that will be serialized as a String
		BigInteger bi3 = BigInteger.valueOf(Long.MAX_VALUE)
			.multiply(BigInteger.valueOf(Long.MAX_VALUE));
		data.put("BigInt3", bi3);
		
		BSONObject obj = generateAndParse(data);

		assertEquals(5, obj.get("Int32"));
		assertEquals(true, obj.get("Boolean1"));
		assertEquals(false, obj.get("Boolean2"));
		assertEquals("Hello", obj.get("String"));
		assertEquals(1234L, obj.get("Long"));
		assertEquals(null, obj.get("Null"));
		assertEquals(1234.1234f, (Double)obj.get("Float"), 0.00001);
		assertEquals(5678.5678, (Double)obj.get("Double"), 0.00001);
		assertEquals(Integer.MAX_VALUE, obj.get("BigInt1"));
		assertEquals(Long.valueOf(Integer.MAX_VALUE) * 2L, obj.get("BigInt2"));
		assertEquals(bi3.toString(), obj.get("BigInt3"));
	}
	
	@Test
	public void stream() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BsonFactory fac = new BsonFactory();
		fac.enable(BsonGenerator.Feature.ENABLE_STREAMING);
		BsonGenerator gen = fac.createGenerator(baos);
		byte[] dummy = new byte[DynamicOutputBuffer.DEFAULT_BUFFER_SIZE * 3 / 2];
		gen.writeStartObject();
		gen.writeFieldName("Test");
		gen.writeBinary(dummy);
		
		assertEquals(DynamicOutputBuffer.DEFAULT_BUFFER_SIZE, baos.size());
		
		gen.writeEndObject();
		gen.writeStartObject();
		gen.writeFieldName("Test");
		gen.writeBinary(dummy);
		
		assertEquals(DynamicOutputBuffer.DEFAULT_BUFFER_SIZE * 3, baos.size());
		
		gen.writeEndObject();
		gen.close();
		
		assertTrue(baos.size() > DynamicOutputBuffer.DEFAULT_BUFFER_SIZE * 3);
	}
	
	private void assertRaw(byte[] r) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(r);
		BSONDecoder decoder = new BasicBSONDecoder();
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
		BsonGenerator gen = new BsonGenerator(JsonGenerator.Feature.collectDefaults(), 0, baos);
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
		BsonGenerator gen = new BsonGenerator(JsonGenerator.Feature.collectDefaults(), 0, baos);
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
		BsonGenerator gen = new BsonGenerator(JsonGenerator.Feature.collectDefaults(), 0, baos);
		gen.writeStartObject();
		gen.writeFieldName("Test");
		gen.writeBinary(new byte[] { (byte)1, (byte)2 });
		gen.writeEndObject();
		gen.close();
		
		byte[] r = baos.toByteArray();
		ByteArrayInputStream bais = new ByteArrayInputStream(r);
		BSONDecoder decoder = new BasicBSONDecoder();
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

		BSONObject obj2 = generateAndParse(data2);

		assertEquals(10L, obj2.get("Int64"));
		BSONObject obj1 = (BSONObject)obj2.get("data1");
		assertEquals(5, obj1.get("Int32"));
		BSONObject obj3 = (BSONObject)obj2.get("data3");
		assertEquals("Hello", obj3.get("String"));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void arrays() throws Exception {
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		data.put("Int32", 5);
		data.put("Arr", Arrays.asList("a", "b", "c"));
		data.put("Int64", 10L);
		List<String> a3 = Arrays.asList("d", "e", "f");
		List<String> a4 = Arrays.asList("g", "h", "j");
		List<List<String>> a5 = new ArrayList<List<String>>();
		a5.add(a3);
		a5.add(a4);
		data.put("Arr2", a5);
		
		Map<String, Object> data2 = new LinkedHashMap<String, Object>();
		data2.put("Str", "Hello");
		List<Map<String, Object>> a6 = new ArrayList<Map<String, Object>>();
		a6.add(data2);
		data.put("Arr3", a6);
		
		BSONObject obj = generateAndParse(data);

		assertEquals(5, obj.get("Int32"));
		List<String> o = (List<String>)obj.get("Arr");
		assertEquals(3, o.size());
		assertEquals("a", o.get(0));
		assertEquals("b", o.get(1));
		assertEquals("c", o.get(2));
		assertEquals(10L, obj.get("Int64"));
		List<List<String>> o5 = (List<List<String>>)obj.get("Arr2");
		assertEquals(a5, o5);
		List<BSONObject> o6 = (List<BSONObject>)obj.get("Arr3");
		assertEquals(1, o6.size());
		BSONObject b6 = o6.get(0);
		assertEquals("Hello", b6.get("Str"));
	}
	
	@Test
	public void utf8Strings() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BsonGenerator gen = new BsonGenerator(JsonGenerator.Feature.collectDefaults(), 0, baos);
		gen.writeStartObject();
		gen.writeFieldName("a\u20AC\u00A2\u00A2bb");
		gen.writeString("a\u20AC\u00A2\u00A2bb");
		gen.writeEndObject();
		gen.close();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		BSONDecoder decoder = new BasicBSONDecoder();
		BSONObject obj = decoder.readObject(bais);
		String s = (String)obj.get("a\u20AC\u00A2\u00A2bb");
		assertEquals("a\u20AC\u00A2\u00A2bb", s);
	}
	
	@Test
	public void uuids() throws Exception {
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		data.put("Int32", 5);
		data.put("Arr", Arrays.asList("a", "b", "c"));
		UUID uuid = UUID.randomUUID();
		data.put("Uuid", uuid);

		BSONObject obj = generateAndParse(data);

		assertEquals(5, obj.get("Int32"));
		assertNotNull(obj.get("Uuid"));
		assertEquals(UUID.class, obj.get("Uuid").getClass());
		assertEquals(uuid, obj.get("Uuid"));
	}

	@Test
	public void dates() throws Exception {
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		data.put("date", date);
		data.put("calendar", calendar);

		BSONObject obj = generateAndParse(data);

		assertEquals(date, obj.get("date"));
		assertEquals(date, obj.get("calendar"));
	}

	@Test
	public void objectIds() throws Exception {
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		ObjectId objectId = new ObjectId((int) (System.currentTimeMillis() / 1000), new Random().nextInt(), 100);
		data.put("_id", objectId);

		BSONObject obj = generateAndParse(data);

		org.bson.types.ObjectId result = (org.bson.types.ObjectId) obj.get("_id");
		assertNotNull(result);
		assertEquals(objectId.getTime(), result.getTimeSecond());
		assertEquals(objectId.getMachine(), result.getMachine());
		assertEquals(objectId.getInc(), result.getInc());
	}

	@Test
	public void patterns() throws Exception {
		Pattern pattern = Pattern.compile("a.*a", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		data.put("pattern", pattern);

		BSONObject obj = generateAndParse(data);

		Pattern result = (Pattern) obj.get("pattern");
		assertNotNull(result);
		assertEquals(pattern.pattern(), result.pattern());
		assertEquals(pattern.flags(), result.flags());
	}

	@Test
	public void timestamps() throws Exception {
		Timestamp timestamp = new Timestamp(100, 200);
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		data.put("timestamp", timestamp);

		BSONObject obj = generateAndParse(data);

		BSONTimestamp result = (BSONTimestamp) obj.get("timestamp");
		assertNotNull(result);
		assertEquals(timestamp.getInc(), result.getInc());
		assertEquals(timestamp.getTime(), result.getTime());
	}

	@Test
	public void javascript() throws Exception {
		JavaScript javaScript = new JavaScript("a < 100");
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		data.put("javaScript", javaScript);

		BSONObject obj = generateAndParse(data);

		Code result = (Code) obj.get("javaScript");
		assertNotNull(result);
		assertEquals(javaScript.getCode(), result.getCode());
	}

	@Test
	public void javascriptWithScope() throws Exception {
		Map<String, Object> scope = new LinkedHashMap<String, Object>();
		scope.put("a", 99);
		scope.put("b", 80);
		JavaScript javaScript = new JavaScript("a < 100", scope);
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		data.put("javaScript", javaScript);

		BSONObject obj = generateAndParse(data);

		CodeWScope result = (CodeWScope) obj.get("javaScript");
		assertNotNull(result);
		assertEquals(javaScript.getCode(), result.getCode());
		Map<?, ?> returnedScope = result.getScope().toMap();
		assertEquals(returnedScope, scope);
	}

	private BSONObject generateAndParse(Map<String, Object> data) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectMapper om = new ObjectMapper(new BsonFactory());
		om.registerModule(new BsonModule());
		om.writeValue(baos, data);

		byte[] r = baos.toByteArray();
		ByteArrayInputStream bais = new ByteArrayInputStream(r);

		BSONDecoder decoder = new BasicBSONDecoder();
		return decoder.readObject(bais);
	}
}
