package de.undercouch.bson4jackson.serializers;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.undercouch.bson4jackson.BsonFactory;
import de.undercouch.bson4jackson.BsonModule;
import de.undercouch.bson4jackson.types.JavaScript;
import de.undercouch.bson4jackson.types.ObjectId;
import de.undercouch.bson4jackson.types.Symbol;
import de.undercouch.bson4jackson.types.Timestamp;
import org.bson.BSONDecoder;
import org.bson.BSONObject;
import org.bson.BasicBSONDecoder;
import org.bson.types.Code;
import org.bson.types.CodeWScope;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

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
     * Tests {@link BsonJavaScriptSerializer}
     * @throws Exception if something goes wrong
     */
    @Test
    public void javascript() throws Exception {
        JavaScript js = new JavaScript("code");
        Code code = (Code)generateAndParse(js);
        assertEquals(js.getCode(), code.getCode());
    }

    /**
     * Tests {@link BsonJavaScriptSerializer}
     * @throws Exception if something goes wrong
     */
    @Test
    public void javascriptWithScope() throws Exception {
        Map<String, Object> scope = new HashMap<>();
        scope.put("j", 5);
        JavaScript js = new JavaScript("code", scope);
        CodeWScope code = (CodeWScope)generateAndParse(js);
        assertEquals(js.getCode(), code.getCode());
        assertEquals(js.getScope(), code.getScope());
    }

    /**
     * Tests {@link BsonObjectIdSerializer}
     * @throws Exception if something goes wrong
     */
    @Test
    public void objectId() throws Exception {
        ObjectId id = new ObjectId(1, 2, 3, (short)4);
        org.bson.types.ObjectId roid = (org.bson.types.ObjectId)generateAndParse(id);
        assertEquals(1, roid.getTimestamp());
        assertEquals(2, roid.getCounter());
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
        assertEquals(sym, obj);
    }

    /**
     * Tests {@link BsonTimestampSerializer}
     * @throws Exception if something goes wrong
     */
    @Test
    public void timestamp() throws Exception {
        Timestamp ts = new Timestamp(1, 2);
        org.bson.types.BSONTimestamp rts = (org.bson.types.BSONTimestamp)generateAndParse(ts);
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
