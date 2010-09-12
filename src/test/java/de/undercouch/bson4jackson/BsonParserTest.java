package de.undercouch.bson4jackson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.util.Map;

import org.bson.BSONEncoder;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

/**
 * Tests {@link BsonParser}
 * @author Michel Kraemer
 */
public class BsonParserTest {
	@Test
	public void parsePrimitives() throws Exception {
		BSONObject o = new BasicBSONObject();
		o.put("Double", 5.0);
		o.put("Float", 10.0f);
		o.put("String", "Hello World");
		o.put("Null", null);
		o.put("Bool1", true);
		o.put("Bool2", false);
		BSONEncoder enc = new BSONEncoder();
		byte[] b = enc.encode(o);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		ObjectMapper mapper = new ObjectMapper(new BsonFactory());
		Map<?, ?> data = mapper.readValue(bais, Map.class);
		assertEquals(5.0, data.get("Double"));
		assertEquals(10.0, data.get("Float"));
		assertEquals("Hello World", data.get("String"));
		assertNull(data.get("Null"));
		assertEquals(true, data.get("Bool1"));
		assertEquals(false, data.get("Bool2"));
	}
}
