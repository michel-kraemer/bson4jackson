// Copyright 2010-2014 Michel Kraemer
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

package de.undercouch.bson4jackson.serializers;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
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
import org.bson.types.ObjectId;
import org.bson.types.Symbol;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.undercouch.bson4jackson.BsonFactory;
import de.undercouch.bson4jackson.BsonModule;

/**
 * Tests {@link BsonSerializers}
 * @author Michel Kraemer
 */
public class BsonSerializersTest {
	private static Object generateAndParse(Object data) throws Exception {
		Map<String, Object> m = new LinkedHashMap<String, Object>();
		m.put("data", data);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		ObjectMapper om = new ObjectMapper(new BsonFactory());
		om.registerModule(new BsonModule());
		om.writeValue(baos, m);

		byte[] r = baos.toByteArray();
		ByteArrayInputStream bais = new ByteArrayInputStream(r);

		BSONDecoder decoder = new BasicBSONDecoder();
		BSONObject bo = decoder.readObject(bais);
		
		return bo.get("data");
	}
	
	/**
	 * Tests {@link BsonCalendarSerializer}
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void calendar() throws Exception {
		Calendar cal = Calendar.getInstance();
		Object obj = generateAndParse(cal);
		assertEquals(cal.getTime(), obj);
	}
	
	/**
	 * Tests {@link BsonDateSerializer}
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void date() throws Exception {
		Date date = new Date();
		Object obj = generateAndParse(date);
		assertEquals(date, obj);
	}
	
	/**
	 * Tests {@link BsonCodeSerializer}
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void javascript() throws Exception {
		Code js = new Code("code");
		Code code = (Code)generateAndParse(js);
		assertEquals(js.getCode(), code.getCode());
	}
	
	/**
	 * Tests {@link BsonCodeSerializer}
	 * @throws Exception if something goes wrong
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void javascriptWithScope() throws Exception {
		Document scope = new Document();
		scope.put("j", 5);
		CodeWithScope js = new CodeWithScope("code", scope);
		CodeWScope code = (CodeWScope)generateAndParse(js);
		assertEquals(js.getCode(), code.getCode());
		assertEquals(js.getScope(), new Document(code.getScope().toMap()));
	}
	
	/**
	 * Tests {@link BsonObjectIdSerializer}
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void objectId() throws Exception {
		ObjectId id = new ObjectId(1, 2, (short)3, 4);
		org.bson.types.ObjectId roid = (org.bson.types.ObjectId)generateAndParse(id);
		assertEquals(id, roid);
	}
	
	/**
	 * Tests {@link BsonRegexSerializer}
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void regex() throws Exception {
		Pattern pat = Pattern.compile("[a-zA-Z0-9]+");
		Pattern obj = (Pattern)generateAndParse(pat);
		assertEquals(pat.pattern(), obj.pattern());
	}
	
	/**
	 * Tests {@link BsonSymbolSerializer}
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void symbol() throws Exception {
		Symbol sym = new Symbol("symbol");
		String obj = (String)generateAndParse(sym);
		assertEquals(sym.getSymbol(), obj);
	}
	
	/**
	 * Tests {@link BsonTimestampSerializer}
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void timestamp() throws Exception {
		BsonTimestamp ts = new BsonTimestamp(1, 2);
		BSONTimestamp rts = (BSONTimestamp)generateAndParse(ts);
		assertEquals(ts.getTime(), rts.getTime());
		assertEquals(ts.getInc(), rts.getInc());
	}
	
	/**
	 * Tests {@link BsonUuidSerializer}
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void uuid() throws Exception {
		UUID uuid = UUID.randomUUID();
		Object obj = generateAndParse(uuid);
		assertEquals(uuid, obj);
	}
}
