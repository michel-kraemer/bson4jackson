package de.undercouch.bson4jackson;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.undercouch.bson4jackson.types.JavaScript;
import de.undercouch.bson4jackson.types.ObjectId;
import de.undercouch.bson4jackson.types.Symbol;
import de.undercouch.bson4jackson.types.Timestamp;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

/**
 * Similar to {@link TypePropertyLastTest}. The {@code type} property is here
 * at the second position (after the first normal property but before all
 * properties that will be handled by one of bson4jackson's deserializers). This
 * basically tests if normal parsers and token buffers can be mixed in a
 * {@link com.fasterxml.jackson.core.util.JsonParserSequence} and if our
 * deserializers are able to handle that.
 * @see <a href="https://github.com/michel-kraemer/bson4jackson/issues/72">issue #72</a>.
 */
public class TypePropertySecondTest {
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.EXISTING_PROPERTY,
            property = "type"
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(name = "a", value = TypeAsPropertyA.class),
    })
    // 'type' must be the second property to really test this feature!
    @JsonPropertyOrder({"str", "type", "date", "calendar", "javaScript",
            "objectId", "pattern", "symbol", "timestamp"})
    public static abstract class TypeAsProperty {
        String str;
        // 'type' must be the second property to really test this feature!
        String type;
        Date date;
        Calendar calendar;
        JavaScript javaScript;
        ObjectId objectId;
        Pattern pattern;
        Symbol symbol;
        Timestamp timestamp;

        public TypeAsProperty() {
            str = "Hello world";
            date = new Date();
            calendar = Calendar.getInstance();
            Map<String, Object> scope = new HashMap<>();
            scope.put("j", 5);
            javaScript = new JavaScript("var i;", scope);
            objectId = new ObjectId(1, 2, 3);
            pattern = Pattern.compile("[a-zA-Z0-9]+",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            symbol = new Symbol("foobar");
            timestamp = new Timestamp(100, 200);
        }

        public void setStr(String str) {
            this.str = str;
        }

        public String getStr() {
            return str;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public Date getDate() {
            return date;
        }

        public void setCalendar(Calendar calendar) {
            this.calendar = calendar;
        }

        public Calendar getCalendar() {
            return calendar;
        }

        public void setJavaScript(JavaScript javascript) {
            this.javaScript = javascript;
        }

        public JavaScript getJavaScript() {
            return javaScript;
        }

        public void setObjectId(ObjectId objectId) {
            this.objectId = objectId;
        }

        public ObjectId getObjectId() {
            return objectId;
        }

        public void setPattern(Pattern pattern) {
            this.pattern = pattern;
        }

        public Pattern getPattern() {
            return pattern;
        }

        public void setSymbol(Symbol symbol) {
            this.symbol = symbol;
        }

        public Symbol getSymbol() {
            return symbol;
        }

        public void setTimestamp(Timestamp timestamp) {
            this.timestamp = timestamp;
        }

        public Timestamp getTimestamp() {
            return timestamp;
        }
    }

    public static class TypeAsPropertyA extends TypeAsProperty {
        TypeAsPropertyA() {
            super();
            type = "a";
        }
    }

    /**
     * Serialize and deserialize an object of type {@link TypeAsPropertyA} and
     * check if all properties are OK.
     * @throws Exception if something goes wrong
     */
    @Test
    @Category(value = RequiresJackson_v2_7.class)
    public void parse() throws Exception {
        ObjectMapper mapper = new ObjectMapper(new BsonFactory())
                .registerModule(new BsonModule());
        TypeAsPropertyA a = new TypeAsPropertyA();
        byte[] bytes = mapper.writeValueAsBytes(a);
        TypeAsProperty v = mapper.readValue(bytes, TypeAsProperty.class);
        assertEquals("a", v.getType());
        assertEquals(TypeAsPropertyA.class, v.getClass());
        assertEquals(a.getStr(), v.getStr());
        assertEquals(a.getDate(), v.getDate());
        assertEquals(a.getCalendar().getTimeInMillis(),
                v.getCalendar().getTimeInMillis());
        assertEquals(a.getJavaScript().getCode(), v.getJavaScript().getCode());
        assertEquals(a.getJavaScript().getScope(), v.getJavaScript().getScope());
        assertEquals(a.getObjectId().getTime(), v.getObjectId().getTime());
        assertEquals(a.getObjectId().getMachine(), v.getObjectId().getMachine());
        assertEquals(a.getObjectId().getInc(), v.getObjectId().getInc());
        assertEquals(a.getPattern().pattern(), v.getPattern().pattern());
        assertEquals(a.getPattern().flags(), v.getPattern().flags());
        assertEquals(a.getSymbol(), v.getSymbol());
        assertEquals(a.getTimestamp(), v.getTimestamp());
    }
}
