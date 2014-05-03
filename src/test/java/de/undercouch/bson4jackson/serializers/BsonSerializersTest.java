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
import org.bson.types.Code;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.undercouch.bson4jackson.BsonFactory;
import de.undercouch.bson4jackson.BsonModule;
import de.undercouch.bson4jackson.types.JavaScript;
import de.undercouch.bson4jackson.types.ObjectId;
import de.undercouch.bson4jackson.types.Symbol;
import de.undercouch.bson4jackson.types.Timestamp;

/**
 * Tests {@link BsonSerializers}
 * @author Michel Kraemer
 */
public class BsonSerializersTest {
	private static BSONObject generateAndParse(Map<String, Object> data) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		ObjectMapper om = new ObjectMapper(new BsonFactory());
		om.registerModule(new BsonModule());
		om.writeValue(baos, data);

		byte[] r = baos.toByteArray();
		ByteArrayInputStream bais = new ByteArrayInputStream(r);

		BSONDecoder decoder = new BasicBSONDecoder();
		return decoder.readObject(bais);
	}
	
	/**
	 * Tests {@link BsonCalendarSerializer}
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void calendar() throws Exception {
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		Calendar cal = Calendar.getInstance();
		data.put("cal", cal);
		BSONObject obj = generateAndParse(data);
		assertEquals(cal.getTime(), obj.get("cal"));
	}
	
	/**
	 * Tests {@link BsonDateSerializer}
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void date() throws Exception {
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		Date date = new Date();
		data.put("date", date);
		BSONObject obj = generateAndParse(data);
		assertEquals(date, obj.get("date"));
	}
	
	/**
	 * Tests {@link BsonJavaScriptSerializer}
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void javascript() throws Exception {
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		JavaScript js = new JavaScript("code");
		data.put("js", js);
		BSONObject obj = generateAndParse(data);
		assertEquals(js.getCode(), ((Code)obj.get("js")).getCode());
	}
	
	/**
	 * Tests {@link BsonObjectIdSerializer}
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void objectId() throws Exception {
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		ObjectId id = new ObjectId(1, 2, 3);
		data.put("id", id);
		BSONObject obj = generateAndParse(data);
		org.bson.types.ObjectId roid = (org.bson.types.ObjectId)obj.get("id");
		assertEquals(id.getTime(), roid.getTimeSecond());
		assertEquals(id.getMachine(), roid.getMachine());
		assertEquals(id.getInc(), roid.getInc());
	}
	
	/**
	 * Tests {@link BsonRegexSerializer}
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void regex() throws Exception {
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		Pattern pat = Pattern.compile("[a-zA-Z0-9]+");
		data.put("pat", pat);
		BSONObject obj = generateAndParse(data);
		assertEquals(pat.pattern(), ((Pattern)obj.get("pat")).pattern());
	}
	
	/**
	 * Tests {@link BsonSymbolSerializer}
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void symbol() throws Exception {
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		Symbol sym = new Symbol("symbol");
		data.put("sym", sym);
		BSONObject obj = generateAndParse(data);
		assertEquals(sym, obj.get("sym"));
	}
	
	/**
	 * Tests {@link BsonTimestampSerializer}
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void timestamp() throws Exception {
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		Timestamp ts = new Timestamp(1, 2);
		data.put("ts", ts);
		BSONObject obj = generateAndParse(data);
		org.bson.types.BSONTimestamp rts = (org.bson.types.BSONTimestamp)obj.get("ts");
		assertEquals(ts.getTime(), rts.getTime());
		assertEquals(ts.getInc(), rts.getInc());
	}
	
	/**
	 * Tests {@link BsonUuidSerializer}
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void uuid() throws Exception {
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		UUID uuid = UUID.randomUUID();
		data.put("uuid", uuid);
		BSONObject obj = generateAndParse(data);
		assertEquals(uuid, obj.get("uuid"));
	}
}
