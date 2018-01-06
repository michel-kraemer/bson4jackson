// Copyright 2010-2016 Michel Kraemer
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
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
import java.util.UUID;
import java.util.regex.Pattern;

import org.bson.BSONDecoder;
import org.bson.BSONObject;
import org.bson.BasicBSONDecoder;
import org.bson.BsonTimestamp;
import org.bson.Document;
import org.bson.types.BSONTimestamp;
import org.bson.types.Code;
import org.bson.types.CodeWScope;
import org.bson.types.CodeWithScope;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.undercouch.bson4jackson.BsonGenerator.Feature;
import de.undercouch.bson4jackson.io.DynamicOutputBuffer;

/**
 * Tests {@link BsonGenerator}
 * @author Michel Kraemer
 */
public class BsonGeneratorTest {
	/**
	 * Simple dummy object for testing object (de-)serialization.
	 */
	private static class TestPojo {
		public Integer i;
		public String s;
	}

	/**
	 * Test if primitives can be serialized
	 * @throws Exception if something goes wrong
	 */
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
		
		// BigInteger that can be serialized as an Integer
		data.put("BigInt1", BigInteger.valueOf(Integer.MAX_VALUE));
		
		// BigInteger that can be serialized as a Long
		BigInteger bi2 = BigInteger.valueOf(Integer.MAX_VALUE)
			.multiply(BigInteger.valueOf(2));
		data.put("BigInt2", bi2);
		
		// BigInteger that will be serialized as a String
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

	/**
	 * Test if the streaming feature works as expected
	 * @see BsonGenerator.Feature#ENABLE_STREAMING
	 * @throws Exception if something goes wrong
	 */
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
	
	/**
	 * Test the {@link BsonGenerator#writeRaw(char[], int, int)} method
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void rawChar() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BsonGenerator gen = new BsonGenerator(
				JsonGenerator.Feature.collectDefaults(), 0, baos);
		gen.writeStartObject();
		gen.writeFieldName("Test");
		gen.writeRaw(new char[] { 'a', 'b' }, 0, 2);
		gen.writeEndObject();
		gen.close();
		assertRaw(baos.toByteArray());
	}

	/**
	 * Test the {@link BsonGenerator#writeString(String)} method
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void rawString() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BsonGenerator gen = new BsonGenerator(
				JsonGenerator.Feature.collectDefaults(), 0, baos);
		gen.writeStartObject();
		gen.writeFieldName("Test");
		gen.writeRaw("ab");
		gen.writeEndObject();
		gen.close();
		assertRaw(baos.toByteArray());
	}

	/**
	 * Test the {@link BsonGenerator#writeBinary(byte[], int, int)} method
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void rawBytes() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BsonGenerator gen = new BsonGenerator(
				JsonGenerator.Feature.collectDefaults(), 0, baos);
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

	/**
	 * Test if embedded objects can be serialized correctly
	 * @throws Exception if something goes wrong
	 */
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

	/**
	 * Test if arrays can be serialized correctly
	 * @throws Exception if something goes wrong
	 */
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

	/**
	 * Test if strings containing UTF-8 characters can be serialized
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void utf8Strings() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BsonGenerator gen = new BsonGenerator(
				JsonGenerator.Feature.collectDefaults(), 0, baos);
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

	/**
	 * Test if {@link UUID} objects can be serialized
	 * @throws Exception if something goes wrong
	 */
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

	/**
	 * Test if {@link Date} objects can be serialized
	 * @throws Exception if something goes wrong
	 */
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

	/**
	 * Test if {@link ObjectId}s can be serialized
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void objectIds() throws Exception {
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		ObjectId objectId = new ObjectId((int)(System.currentTimeMillis() / 1000),
				16777215, (short)65534, 100);
		data.put("_id", objectId);

		BSONObject obj = generateAndParse(data);

		ObjectId result = (ObjectId)obj.get("_id");
		assertNotNull(result);
		assertEquals(objectId, result);
	}

	/**
	 * Test if {@link Pattern}s can be serialized
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void patterns() throws Exception {
		Pattern pattern = Pattern.compile("a.*a",
				Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		data.put("pattern", pattern);

		BSONObject obj = generateAndParse(data);

		Pattern result = (Pattern) obj.get("pattern");
		assertNotNull(result);
		assertEquals(pattern.pattern(), result.pattern());
		assertEquals(pattern.flags(), result.flags());
	}

	/**
	 * Test if {@link BsonTimestamp} objects can be serialized
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void timestamps() throws Exception {
		BsonTimestamp timestamp = new BsonTimestamp(100, 200);
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		data.put("timestamp", timestamp);

		BSONObject obj = generateAndParse(data);

		BSONTimestamp result = (BSONTimestamp)obj.get("timestamp");
		assertNotNull(result);
		assertEquals(timestamp.getInc(), result.getInc());
		assertEquals(timestamp.getTime(), result.getTime());
	}

	/**
	 * Test if {@link Code} objects can be serialized
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void javascript() throws Exception {
		Code javaScript = new Code("a < 100");
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		data.put("javaScript", javaScript);

		BSONObject obj = generateAndParse(data);

		Code result = (Code) obj.get("javaScript");
		assertNotNull(result);
		assertEquals(javaScript.getCode(), result.getCode());
	}

	/**
	 * Test if {@link Code} objects with a scope can be serialized
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void javascriptWithScope() throws Exception {
		Document scope = new Document();
		scope.put("a", 99);
		scope.put("b", 80);
		Code javaScript = new CodeWithScope("a < 100", scope);
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		data.put("javaScript", javaScript);

		BSONObject obj = generateAndParse(data);

		CodeWScope result = (CodeWScope) obj.get("javaScript");
		assertNotNull(result);
		assertEquals(javaScript.getCode(), result.getCode());
		Map<?, ?> returnedScope = result.getScope().toMap();
		assertEquals(returnedScope, scope);
	}

	private BSONObject generateAndParse(Map<String, Object> data,
			BsonGenerator.Feature... featuresToEnable) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BsonFactory bsonFactory = new BsonFactory();
		if (featuresToEnable != null) {
			for (BsonGenerator.Feature fe : featuresToEnable) {
				bsonFactory.enable(fe);
			}
		}
		ObjectMapper om = new ObjectMapper(bsonFactory);
		om.registerModule(new BsonModule());
		om.writeValue(baos, data);

		byte[] r = baos.toByteArray();
		ByteArrayInputStream bais = new ByteArrayInputStream(r);

		BSONDecoder decoder = new BasicBSONDecoder();
		return decoder.readObject(bais);
	}

	/**
	 * Test if {@link BigDecimal} objects can be serialized as {@link String}s
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void writeBigDecimalsAsStrings() throws Exception {
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		data.put("big", new BigDecimal("0.3"));

		BSONObject obj = generateAndParse(data);

		Double result = (Double)obj.get("big");
		
		//BigDecimal("0.3") does not equal 0.3!
		assertEquals(Double.valueOf(0.3), result, 0.000001);
		assertFalse(Double.valueOf(0.3).equals(result));
		
		data = new LinkedHashMap<String, Object>();
		data.put("big", new BigDecimal("0.3"));

		obj = generateAndParse(data,
				BsonGenerator.Feature.WRITE_BIGDECIMALS_AS_STRINGS);

		String strResult = (String)obj.get("big");
		assertEquals("0.3", strResult);
	}

	/**
	 * Test if {@link BigDecimal} objects can be serialized as {@link Decimal128}s
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void writeBigDecimalsAsDecimal128s() throws Exception {
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		data.put("big", new BigDecimal("0.3"));

		BSONObject obj = generateAndParse(data);

		Double result = (Double)obj.get("big");

		//BigDecimal("0.3") does not equal 0.3!
		assertEquals(0.3, result, 0.000001);
		assertFalse(Double.valueOf(0.3).equals(result));

		data = new LinkedHashMap<String, Object>();
		data.put("big", new BigDecimal("0.3"));

		obj = generateAndParse(data,
				Feature.WRITE_BIGDECIMALS_AS_DECIMAL128);

		org.bson.types.Decimal128 strResult = (org.bson.types.Decimal128)obj.get("big");
		assertEquals(new BigDecimal("0.3"), strResult.bigDecimalValue());
	}

	/**
	 * Test if  binary data can be serialized
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void writeBinaryData() throws Exception {
		byte[] binary = new byte[] { (byte)0x05, (byte)0xff, (byte)0xaf,
				(byte)0x30, 'A', 'B', 'C', (byte)0x13, (byte)0x80,
				(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff};
		
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		data.put("binary", binary);
		
		// binary data has to be converted to base64 with normal JSON
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = mapper.writeValueAsString(data);
		assertEquals("{\"binary\":\"Bf+vMEFCQxOA/////w==\"}", jsonString);
		
		// with BSON we don't have to convert to base64
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BsonFactory bsonFactory = new BsonFactory();
		ObjectMapper om = new ObjectMapper(bsonFactory);
		om.writeValue(baos, data);
		
		// document header (4 bytes) + type (1 byte) + field_name ("binary", 6 bytes) +
		// end_of_string (1 byte) + binary_size (4 bytes) + subtype (1 byte) +
		// binary_data (13 bytes) + end_of_document (1 byte)
		int expectedLen = 4 + 1 + 6 + 1 + 4 + 1 + 13 + 1;
		
		assertEquals(expectedLen, baos.size());
		
		// BSON is smaller than JSON (at least in this case)
		assertTrue(baos.size() < jsonString.length());
		
		// test if binary data can be parsed
		BSONObject obj = generateAndParse(data);
		byte[] objbin = (byte[])obj.get("binary");
		assertArrayEquals(binary, objbin);
	}

	/**
	 * Test if multiple objects can be written in sequence using
	 * {@link SequenceWriter}
	 * @throws Exception if something goes wrong
	 */
	@Test
	@Category(value = RequiresJackson_v2_5.class)
	public void writeMultipleObjects() throws Exception {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		BsonFactory bsonFactory = new BsonFactory();

		// test input 1
		TestPojo testObjectIn1 = new TestPojo();
		testObjectIn1.i = 42;
		testObjectIn1.s = "43";

		// test input 2
		TestPojo testObjectIn2 = new TestPojo();
		testObjectIn2.i = 44;
		testObjectIn2.s = "45";

		// write in non-streaming mode using explicit flush()
		// we explicitly create a sequence writer for writing out several
		// TestPojo objects in a row (useful for storage/online communications)
		bsonFactory.disable(BsonGenerator.Feature.ENABLE_STREAMING);
		ObjectMapper mapperExplicit = new ObjectMapper(bsonFactory);
		mapperExplicit.disable(SerializationFeature.FLUSH_AFTER_WRITE_VALUE);
		ObjectWriter objectWriterExplicit = mapperExplicit.writer();
		SequenceWriter sequenceWriterExplicit =
				objectWriterExplicit.writeValues(outputStream);
		sequenceWriterExplicit.write(testObjectIn1);
		sequenceWriterExplicit.flush();

		// were all bytes associated with our top-level object testObjectIn1
		// successfully written to the output stream?
		TestPojo testObjectOut1 = mapperExplicit.readValue(outputStream.toByteArray(),
				TestPojo.class);
		assertEquals(testObjectIn1.i, testObjectOut1.i);
		assertEquals(testObjectIn1.s, testObjectOut1.s);

		sequenceWriterExplicit.close();

		// clear buffer
		outputStream.reset();

		// write() with implicit flush() (streaming mode)
		bsonFactory.enable(BsonGenerator.Feature.ENABLE_STREAMING);
		ObjectMapper mapperImplicit = new ObjectMapper(bsonFactory);
		mapperImplicit.enable(SerializationFeature.FLUSH_AFTER_WRITE_VALUE);
		ObjectWriter objectWriterImplicit = mapperImplicit.writer();
		SequenceWriter sequenceWriterImplicit =
				objectWriterImplicit.writeValues(outputStream);
		sequenceWriterImplicit.write(testObjectIn2);

		// second object also passed through?
		TestPojo testObjectOut2 = mapperImplicit.readValue(outputStream.toByteArray(),
				TestPojo.class);
		assertEquals(testObjectIn2.i, testObjectOut2.i);
		assertEquals(testObjectIn2.s, testObjectOut2.s);

		sequenceWriterImplicit.close();
	}

	/**
	 * Test if {@link CharacterEscapes} are supported
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void characterEscapes() throws Exception {
		JsonNode node = new ObjectMapper().readTree(
				"{ \"some.field\": \"some.val\", "
				+ "\"another\": \"field\", "
				+ "\".some\": \".field\", "
				+ "\"some.\": \"field.\" }");
		ObjectMapper bsonMapper = new ObjectMapper(new BsonFactory());
		bsonMapper.getFactory().setCharacterEscapes(new CharacterEscapes() {
			private static final long serialVersionUID = 283833498358662446L;

			@Override
			public int[] getEscapeCodesForAscii() {
				int[] escapes = CharacterEscapes.standardAsciiEscapesForJSON();
				escapes['.'] = CharacterEscapes.ESCAPE_CUSTOM;
				return escapes;
			}

			@Override
			public SerializableString getEscapeSequence(int ch) {
				switch(ch) {
				case '.':
					return new SerializedString("\uff0e");
				}
				return null;
			}
		});

		byte[] bsonBytes = bsonMapper.writeValueAsBytes(node);
		
		byte[] sBytes = new byte[] {
				//document length
				0x61, 0x00, 0x00, 0x00,
				
				//type = string
				0x02,
				
				//'s'   'o'   'm'   'e'
				0x73, 0x6f, 0x6d, 0x65,
				
				// escape sequence
				(byte)0xef, (byte)0xbc, (byte)0x8e,
				
				//'f'   'i'   'e'   'l'   'd'
				0x66, 0x69, 0x65, 0x6c, 0x64,
				
				// end of string
				0x00,
				
				//string length (0x0b = 10 characters + trailing 0x00)
				0x0b, 0x00, 0x00, 0x00,

				//'s'   'o'   'm'   'e'
				0x73, 0x6f, 0x6d, 0x65,
				
				//escape sequence
				(byte)0xef, (byte)0xbc, (byte)0x8e,
				
				//'v'   'a'   'l'
				0x76, 0x61, 0x6c,
				
				//end of string
				0x00,
				
				//type = string
				0x02,
				
				//'a'   'n'   'o'   't'   'h'   'e'   'r'
				0x61, 0x6e, 0x6f, 0x74, 0x68, 0x65, 0x72,
				
				//end of string
				0x00,
				
				//string length (5 characters + trailing 0x00)
				0x06, 0x00, 0x00, 0x00,
				
				//'f'   'i'   'e'   'l'   'd'
				0x66, 0x69, 0x65, 0x6c, 0x64,
				
				//end of string
				0x00,
				
				//type = string
				0x02,
				
				//escape sequence
				(byte)0xef, (byte)0xbc, (byte)0x8e,
				
				//'s'   'o'   'm'   'e'
				0x73, 0x6f, 0x6d, 0x65,
				
				//end of string
				0x00,
				
				//string length (8 characters + trailing 0x00)
				0x09, 0x00, 0x00, 0x00,
				
				//escape sequence
				(byte)0xef, (byte)0xbc, (byte)0x8e,
				
				//'f'   'i'   'e'   'l'   'd'
				0x66, 0x69, 0x65, 0x6c, 0x64,
				
				//end of string
				0x00,
				
				//type = string
				0x02,
				
				//'s'   'o'   'm'   'e'
				0x73, 0x6f, 0x6d, 0x65,
				
				//escape sequence
				(byte)0xef, (byte)0xbc, (byte)0x8e,
				
				//end of string
				0x00,
				
				//string length (8 characters + trailing 0x00)
				0x09, 0x00, 0x00, 0x00,
				
				//'f'   'i'   'e'   'l'   'd'
				0x66, 0x69, 0x65, 0x6c, 0x64,
				
				//escape sequence
				(byte)0xef, (byte)0xbc, (byte)0x8e,
				
				//end of string
				0x00,
				
				//end of document
				0x00
		};
		
		assertArrayEquals(sBytes, bsonBytes);
	}
}
