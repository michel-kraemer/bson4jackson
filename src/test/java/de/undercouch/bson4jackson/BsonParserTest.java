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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.bson.BSONEncoder;
import org.bson.BSONObject;
import org.bson.BasicBSONEncoder;
import org.bson.BasicBSONObject;
import org.bson.types.BSONTimestamp;
import org.bson.types.Binary;
import org.bson.types.Code;
import org.bson.types.CodeWScope;
import org.bson.types.Symbol;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import de.undercouch.bson4jackson.types.JavaScript;
import de.undercouch.bson4jackson.types.ObjectId;
import de.undercouch.bson4jackson.types.Timestamp;

/**
 * Tests {@link BsonParser}
 * @author Michel Kraemer
 */
public class BsonParserTest {
	/**
	 * Simple test class for {@link BsonParserTest#parseRootObjectArray()}
	 */
	public static class SimpleClass {
		public String name;
	}
	
	/**
	 * Simple test class for {@link BsonParserTest#parseBinaryObject()}
	 */
	public static class BinaryClass {
		public byte[] barr;
	}
	
	/**
	 * Simple test class for {@link BsonParserTest#parseObjectId()}
	 */
	public static class ObjectIdClass {
		public org.bson.types.ObjectId oid;
	}
	
	private <T> T parseBsonObject(BSONObject o, Class<T> cls,
			Module... modules) throws IOException {
		BSONEncoder enc = new BasicBSONEncoder();
		byte[] b = enc.encode(o);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		BsonFactory fac = new BsonFactory();
		ObjectMapper mapper = new ObjectMapper(fac);
		if (modules != null) {
			for (Module mod : modules) {
				mapper.registerModule(mod);
			}
		}
		fac.setCodec(mapper);
		return mapper.readValue(bais, cls);
	}

	private Map<?, ?> parseBsonObject(BSONObject o) throws IOException {
		return parseBsonObject(o, Map.class);
	}
	
	@Test
	public void parsePrimitives() throws Exception {
		BSONObject o = new BasicBSONObject();
		o.put("Double", 5.0);
		o.put("Float", 10.0f);
		o.put("String", "Hello World");
		o.put("Null", null);
		o.put("Bool1", true);
		o.put("Bool2", false);
		o.put("Int32", 1234);
		o.put("Int64", 1234L);

		Map<?, ?> data = parseBsonObject(o);
		assertEquals(5.0, data.get("Double"));
		assertEquals(10.0, data.get("Float"));
		assertEquals("Hello World", data.get("String"));
		assertNull(data.get("Null"));
		assertEquals(true, data.get("Bool1"));
		assertEquals(false, data.get("Bool2"));
		assertEquals(1234, data.get("Int32"));
		assertEquals(1234L, data.get("Int64"));
	}
	
	/**
	 * Tests reading a very large string. Refers issue #18
	 * @throws Exception if something went wrong
	 * @author endasb
	 */
	@Test
	public void parseBigString() throws Exception {
		BSONObject o = new BasicBSONObject();
		StringBuilder bigStr = new StringBuilder();
		for (int i = 0; i < 80000; i++) {
			bigStr.append("abc");
		}
		o.put("String", bigStr.toString());
		Map<?, ?> data = parseBsonObject(o);
		assertEquals(240000, data.get("String").toString().length());
	}
	
	/**
	 * Tests reading a very large string using multiple threads. Refers
	 * issue #19. Does not fail reproducibly, but with very high probability.
	 * You may have to run unit tests several times though to really rule out
	 * multi-threading issues.
	 * @throws Exception if something went wrong
	 * @author endasb
	 */
	@Test
	public void parseBigStringInThreads() throws Exception {
		final BSONObject o = new BasicBSONObject();
		final AtomicInteger fails = new AtomicInteger(0);
		StringBuilder bigStr = new StringBuilder();
		for (int i = 0; i < 80000; i++) {
			bigStr.append("abc");
		}
		o.put("String", bigStr.toString());

		ArrayList<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < 50; i++) {
			threads.add(new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Map<?, ?> data = parseBsonObject(o);
						data = parseBsonObject(o);
						assertNotNull(data);
					} catch (Exception e) {
						fail("Threading issue " + fails.incrementAndGet());
					}
				}
			}));
		}
		for (Thread thread:threads) {
			thread.start();
		}

		for (Thread thread:threads) {
			thread.join();
		}

		assertEquals(0, fails.get());
	}

	@Test
	public void parseBig() throws Exception {
		BSONObject o = new BasicBSONObject();
		o.put("Double", 5.0);
		o.put("Int32", 1234);
		BSONEncoder enc = new BasicBSONEncoder();
		byte[] b = enc.encode(o);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		ObjectMapper mapper = new ObjectMapper(new BsonFactory());
		mapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
		mapper.configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, true);
		Map<?, ?> data = mapper.readValue(bais, Map.class);
		assertEquals(BigDecimal.class, data.get("Double").getClass());
		assertEquals(BigInteger.class, data.get("Int32").getClass());
	}
	
	@Test
	public void parseComplex() throws Exception {
		BSONObject o = new BasicBSONObject();
		o.put("Timestamp", new BSONTimestamp(0xAABB, 0xCCDD));
		o.put("Symbol", new Symbol("Test"));
		o.put("ObjectId", new org.bson.types.ObjectId(Integer.MAX_VALUE, -2, Integer.MIN_VALUE));
		Pattern p = Pattern.compile(".*", Pattern.CASE_INSENSITIVE |
				Pattern.DOTALL | Pattern.MULTILINE | Pattern.UNICODE_CASE);
		o.put("Regex", p);
		
		Map<?, ?> data = parseBsonObject(o);
		assertEquals(new Timestamp(0xAABB, 0xCCDD), data.get("Timestamp"));
		assertEquals(new de.undercouch.bson4jackson.types.Symbol("Test"), data.get("Symbol"));
		ObjectId oid = (ObjectId)data.get("ObjectId");
		assertEquals(Integer.MAX_VALUE, oid.getTime());
		assertEquals(-2, oid.getMachine());
		assertEquals(Integer.MIN_VALUE, oid.getInc());
		Pattern p2 = (Pattern)data.get("Regex");
		assertEquals(p.flags(), p2.flags());
		assertEquals(p.pattern(), p2.pattern());
	}
	
	@Test
	public void parseUndefined() throws Exception {
		BSONObject o = new BasicBSONObject();
		o.put("Undefined", new Object());
		o.put("Int32", 5);
		BSONEncoder enc = new BasicBSONEncoder() {
			@Override
			protected boolean putSpecial(String name, Object o) {
				putUndefined(name);
				return true;
			}
		};
		byte[] b = enc.encode(o);
		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		ObjectMapper mapper = new ObjectMapper(new BsonFactory());
		Map<?, ?> data = mapper.readValue(bais, Map.class);
		assertEquals(1, data.size());
		assertEquals(5, data.get("Int32"));
	}
	
	@Test
	public void parseEmbeddedDocument() throws Exception {
		BSONObject o1 = new BasicBSONObject();
		o1.put("Int32", 5);
		BSONObject o2 = new BasicBSONObject();
		o2.put("Int64", 10L);
		o1.put("Obj", o2);
		o1.put("String", "Hello");
		
		Map<?, ?> data = parseBsonObject(o1);
		assertEquals(3, data.size());
		assertEquals(5, data.get("Int32"));
		Map<?, ?> data2 = (Map<?, ?>)data.get("Obj");
		assertEquals(1, data2.size());
		assertEquals(10L, data2.get("Int64"));
		assertEquals("Hello", data.get("String"));
	}
	
	@Test
	public void parseEmbeddedArray() throws Exception {
		List<Integer> i = new ArrayList<Integer>();
		i.add(5);
		i.add(6);
		BSONObject o = new BasicBSONObject();
		o.put("Int32", 5);
		o.put("Arr", i);
		o.put("String", "Hello");
		
		Map<?, ?> data = parseBsonObject(o);
		assertEquals(3, data.size());
		assertEquals(5, data.get("Int32"));
	}
	
	/**
	 * Tests reading an embedded document through
	 * {@link BsonParser#readValueAsTree()}. Refers issue #9
	 * @throws Exception if something went wrong
	 * @author audistard
	 */
	@Test
	public void parseEmbeddedDocumentAsTree() throws Exception {
		BSONObject o2 = new BasicBSONObject();
		o2.put("Int64", 10L);
		
		BSONObject o3 = new BasicBSONObject();
		o3.put("Int64", 11L);
		
		BSONObject o1 = new BasicBSONObject();
		o1.put("Obj2", o2);
		o1.put("Obj3", o3);
		
		BSONEncoder enc = new BasicBSONEncoder();
		byte[] b = enc.encode(o1);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		BsonFactory fac = new BsonFactory();
		ObjectMapper mapper = new ObjectMapper(fac);
		fac.setCodec(mapper);
		
		BsonParser dec = fac.createParser(bais);
		
		assertEquals(JsonToken.START_OBJECT, dec.nextToken());
		
		assertEquals(JsonToken.FIELD_NAME, dec.nextToken());
		assertEquals("Obj2", dec.getCurrentName());
		assertEquals(JsonToken.START_OBJECT, dec.nextToken());
		JsonNode obj2 = dec.readValueAsTree();
		assertEquals(1, obj2.size());
		assertNotNull(obj2.get("Int64"));
		assertEquals(10L, obj2.get("Int64").longValue());
		
		assertEquals(JsonToken.FIELD_NAME, dec.nextToken());
		assertEquals("Obj3", dec.getCurrentName());
		assertEquals(JsonToken.START_OBJECT, dec.nextToken());
		
		assertEquals(JsonToken.FIELD_NAME, dec.nextToken());
		assertEquals("Int64", dec.getCurrentName());
		assertEquals(JsonToken.VALUE_NUMBER_INT, dec.nextToken());
		assertEquals(11L, dec.getLongValue());
		
		assertEquals(JsonToken.END_OBJECT, dec.nextToken());
		
		assertEquals(JsonToken.END_OBJECT, dec.nextToken());
	}
	
	@Test
	public void parseCode() throws Exception {
		BSONObject scope = new BasicBSONObject();
		scope.put("Int32", 5);
		
		BSONObject o = new BasicBSONObject();
		o.put("Code1", new CodeWScope("alert('test');", scope));
		o.put("Code2", new Code("alert('Hello');"));
		
		Map<?, ?> data = parseBsonObject(o);
		assertEquals(2, data.size());
		JavaScript c1 = (JavaScript)data.get("Code1");
		JavaScript c2 = (JavaScript)data.get("Code2");
		assertEquals("alert('test');", c1.getCode());
		assertEquals("alert('Hello');", c2.getCode());
		Map<String, Object> c1scope = c1.getScope();
		assertEquals(5, c1scope.get("Int32"));
	}
	
	@Test
	public void parseBinary() throws Exception {
		byte[] b = new byte[] { 1, 2, 3, 4, 5 };
		BSONObject o = new BasicBSONObject();
		o.put("b1", b);
		o.put("b2", new Binary(BsonConstants.SUBTYPE_BINARY, b));
		o.put("uuid", new UUID(1L, 2L));
		
		Map<?, ?> data = parseBsonObject(o);
		assertEquals(3, data.size());
		assertArrayEquals(b, (byte[])data.get("b1"));
		assertArrayEquals(b, (byte[])data.get("b2"));
		assertEquals(new UUID(1L, 2L), data.get("uuid"));
	}
	
	@Test
	public void parseBinaryObject() throws Exception {
		byte[] b = new byte[] { 1, 2, 3, 4, 5 };
		BSONObject o = new BasicBSONObject();
		o.put("barr", b);
		
		BinaryClass data = parseBsonObject(o, BinaryClass.class);
		assertArrayEquals(b, data.barr);
	}
	
	/**
	 * Test if {@link BsonParser#nextToken()} returns null if there
	 * is no more input. Refers issue #10.
	 * @throws Exception if something went wrong
	 * @author hertzsprung
	 * @author Michel Kraemer
	 */
	@Test
	public void parseBeyondEnd() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BsonFactory bsonFactory = new BsonFactory();
		BsonGenerator generator = bsonFactory.createGenerator(out);
		generator.writeStartObject();
		generator.writeStringField("myField", "myValue");
		generator.writeEndObject();
		generator.close();

		BsonParser parser = bsonFactory.createJsonParser(out.toByteArray());
		//the following loop shall throw no exception and end after 4 iterations
		int i = 0;
		while (parser.nextToken() != null) {
			++i;
			assertTrue(i <= 4);
		}
		assertEquals(4, i);
	}

	/**
	 * Make sure we honor the length of the document if requested
	 * @throws Exception if something went wrong
	 */
	@Test
	public void honorLength() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BsonFactory bsonFactory = new BsonFactory();
		bsonFactory.enable(BsonParser.Feature.HONOR_DOCUMENT_LENGTH);
		BsonGenerator generator = bsonFactory.createGenerator(out);
		generator.writeStartObject();
		generator.writeStringField("myField", "myValue");
		generator.writeEndObject();
		generator.close();

		out.write(new String("Hello world!\n").getBytes());

		InputStream is = new ByteArrayInputStream(out.toByteArray());
		ObjectMapper mapper = new ObjectMapper(bsonFactory);
		bsonFactory.setCodec(mapper);
		Map<?, ?> result = mapper.readValue(is, Map.class);
		assertEquals("myValue", result.get("myField"));

		// Now check that we can read the extra string we put at the end
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		assertEquals("Hello world!", reader.readLine());
	}
	
	/**
	 * Checks if the parser returns a textual representation of arbitrary
	 * tokens. See issue #23.
	 * @throws Exception if something went wrong
	 */
	@Test
	public void parseAsText() throws Exception {
		BSONObject o = new BasicBSONObject();
		o.put("Float", 5.0f);
		o.put("Int32", 1234);
		BSONEncoder enc = new BasicBSONEncoder();
		byte[] b = enc.encode(o);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		BsonFactory fac = new BsonFactory();
		BsonParser dec = fac.createParser(bais);
		
		assertEquals(JsonToken.START_OBJECT, dec.nextToken());
		
		assertEquals(JsonToken.FIELD_NAME, dec.nextToken());
		assertEquals("Float", dec.getCurrentName());
		assertEquals(JsonToken.VALUE_NUMBER_FLOAT, dec.nextToken());
		assertEquals(5.0f, dec.getFloatValue(), 0.00001);
		assertEquals("5.0", dec.getText());
		
		assertEquals(JsonToken.FIELD_NAME, dec.nextToken());
		assertEquals("Int32", dec.getCurrentName());
		assertEquals(JsonToken.VALUE_NUMBER_INT, dec.nextToken());
		assertEquals(1234, dec.getIntValue());
		assertEquals("1234", dec.getText());
		
		assertEquals(JsonToken.END_OBJECT, dec.nextToken());
	}
	
	/**
	 * Tests if a simple BSON file can be read successfully
	 * @throws Exception if something went wrong
	 */
	@Test
	public void readBSONFile() throws Exception {
		InputStream is = getClass().getResourceAsStream("test.bson");
		try {
			ObjectMapper mapper = new ObjectMapper(new BsonFactory());
			@SuppressWarnings("deprecation")
			MappingIterator<BSONObject> iterator =
					mapper.reader(BasicBSONObject.class).readValues(is);

			BSONObject o = null;
			while (iterator.hasNext()) {
				assertNull(o);
				BSONObject object = iterator.next();
				assertNotNull(object);
				o = object;
			}
			
			assertEquals("Hello world", o.get("message"));
			assertEquals(10.0, o.get("size"));
			assertTrue(o.keySet().contains("_id"));
			assertEquals(3, o.keySet().size());
		} finally {
			is.close();
		}
	}
	
	/**
	 * Tests if a root-level array can be read correctly. Fixes issue #31
	 * @throws Exception if something went wrong
	 */
	@Test
	public void parseRootArray() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BsonFactory bsonFactory = new BsonFactory();
		BsonGenerator generator = bsonFactory.createGenerator(out);
		generator.writeStartArray();
		generator.writeString("first");
		generator.writeString("second");
		generator.writeString("third");
		generator.writeEndArray();
		generator.close();

		InputStream is = new ByteArrayInputStream(out.toByteArray());
		ObjectMapper mapper = new ObjectMapper(bsonFactory);
		bsonFactory.setCodec(mapper);
		String[] result = mapper.readValue(is, String[].class);
		assertEquals("first", result[0]);
		assertEquals("second", result[1]);
		assertEquals("third", result[2]);
	}
	
	/**
	 * Tests if an empty root array can be parsed correctly
	 * @throws Exception if something went wrong
	 */
	@Test
	public void parseEmptyRootArray() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BsonFactory bsonFactory = new BsonFactory();
		BsonGenerator generator = bsonFactory.createGenerator(out);
		generator.writeStartArray();
		generator.writeEndArray();
		generator.close();

		InputStream is = new ByteArrayInputStream(out.toByteArray());
		ObjectMapper mapper = new ObjectMapper(bsonFactory);
		bsonFactory.setCodec(mapper);
		String[] result = mapper.readValue(is, String[].class);
		assertEquals(0, result.length);
	}
	
	/**
	 * Tests if a root object is not accidentally parsed as an array
	 * @throws Exception if something went wrong
	 */
	@Test(expected = JsonMappingException.class)
	public void parseRootObjectAsArray() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BsonFactory bsonFactory = new BsonFactory();
		BsonGenerator generator = bsonFactory.createGenerator(out);
		generator.writeStartObject();
		generator.writeStringField("myField", "myValue");
		generator.writeEndObject();
		generator.close();

		InputStream is = new ByteArrayInputStream(out.toByteArray());
		ObjectMapper mapper = new ObjectMapper(bsonFactory);
		bsonFactory.setCodec(mapper);
		mapper.readValue(is, String[].class);
	}
	
	/**
	 * Creates a root array consisting of two simple objects and tries to
	 * deserialize them
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void parseRootObjectArray() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BsonFactory bsonFactory = new BsonFactory();
		BsonGenerator generator = bsonFactory.createGenerator(out);
		generator.writeStartArray();
		generator.writeStartObject();
		generator.writeStringField("name", "test");
		generator.writeEndObject();
		generator.writeStartObject();
		generator.writeStringField("name", "test2");
		generator.writeEndObject();
		generator.writeEndArray();
		generator.close();
		
		InputStream is = new ByteArrayInputStream(out.toByteArray());
		ObjectMapper mapper = new ObjectMapper(bsonFactory);
		bsonFactory.setCodec(mapper);
		SimpleClass[] result = mapper.readValue(is, SimpleClass[].class);
		assertEquals("test", result[0].name);
		assertEquals("test2", result[1].name);
	}
	
	/**
	 * Check if org.bson.types.ObjectId can be serialized and deserialized as
	 * a byte array. See issue #38
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void parseObjectId() throws Exception {
		class ObjectIdDeserializer extends StdDeserializer<org.bson.types.ObjectId> {
			private static final long serialVersionUID = 6934309887169924897L;

			protected ObjectIdDeserializer() {
				super(org.bson.types.ObjectId.class);
			}

			@Override
			public org.bson.types.ObjectId deserialize(JsonParser jp,
					DeserializationContext ctxt) throws IOException,
					JsonGenerationException {
				return new org.bson.types.ObjectId(jp.getBinaryValue());
			}
		}
		
		org.bson.types.ObjectId oid = new org.bson.types.ObjectId();
		BSONObject o = new BasicBSONObject();
		o.put("oid", oid.toByteArray());
		
		SimpleModule mod = new SimpleModule();
		mod.addDeserializer(org.bson.types.ObjectId.class, new ObjectIdDeserializer());
		
		ObjectIdClass res = parseBsonObject(o, ObjectIdClass.class, mod);
		assertEquals(oid, res.oid);
	}
}
