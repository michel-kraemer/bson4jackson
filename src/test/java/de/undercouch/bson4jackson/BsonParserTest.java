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
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bson.BSONEncoder;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.bson.types.BSONTimestamp;
import org.bson.types.Binary;
import org.bson.types.Code;
import org.bson.types.CodeWScope;
import org.bson.types.Symbol;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import de.undercouch.bson4jackson.types.JavaScript;
import de.undercouch.bson4jackson.types.ObjectId;
import de.undercouch.bson4jackson.types.Timestamp;

/**
 * Tests {@link BsonParser}
 * @author Michel Kraemer
 */
public class BsonParserTest {
	private Map<?, ?> parseBsonObject(BSONObject o) throws IOException {
		BSONEncoder enc = new BSONEncoder();
		byte[] b = enc.encode(o);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		BsonFactory fac = new BsonFactory();
		ObjectMapper mapper = new ObjectMapper(fac);
		fac.setCodec(mapper);
		return mapper.readValue(bais, Map.class);
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
	
	@Test
	public void parseBig() throws Exception {
		BSONObject o = new BasicBSONObject();
		o.put("Double", 5.0);
		o.put("Int32", 1234);
		BSONEncoder enc = new BSONEncoder();
		byte[] b = enc.encode(o);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		ObjectMapper mapper = new ObjectMapper(new BsonFactory());
		mapper.configure(DeserializationConfig.Feature.USE_BIG_DECIMAL_FOR_FLOATS, true);
		mapper.configure(DeserializationConfig.Feature.USE_BIG_INTEGER_FOR_INTS, true);
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
		BSONEncoder enc = new BSONEncoder() {
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
}
