package de.undercouch.bson4jackson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.BSONEncoder;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.bson.types.BSONTimestamp;
import org.bson.types.Symbol;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

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
		ObjectMapper mapper = new ObjectMapper(new BsonFactory());
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
		
		Map<?, ?> data = parseBsonObject(o);
		assertEquals(new Timestamp(0xAABB, 0xCCDD), data.get("Timestamp"));
		assertEquals(new de.undercouch.bson4jackson.types.Symbol("Test"), data.get("Symbol"));
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
}
