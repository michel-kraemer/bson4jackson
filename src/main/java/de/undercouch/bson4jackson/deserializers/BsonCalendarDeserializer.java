package de.undercouch.bson4jackson.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import de.undercouch.bson4jackson.BsonConstants;
import de.undercouch.bson4jackson.BsonParser;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 * Deserializes BSON date type objects to calendars
 * @author Michel Kraemer
 * @since 2.3.2
 */
public class BsonCalendarDeserializer extends JsonDeserializer<Calendar> {
    @Override
    @SuppressWarnings("deprecation")
    public Calendar deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        if (jp instanceof BsonParser) {
            BsonParser bsonParser = (BsonParser)jp;
            if (bsonParser.getCurrentToken() != JsonToken.VALUE_EMBEDDED_OBJECT ||
                    bsonParser.getCurrentBsonType() != BsonConstants.TYPE_DATETIME) {
                throw ctxt.mappingException(Date.class);
            }

            Object obj = bsonParser.getEmbeddedObject();
            if (obj == null) {
                return null;
            }

            Calendar cal = Calendar.getInstance();
            cal.setTime((Date)obj);
            return cal;
        } else if (jp.getCurrentToken() == JsonToken.VALUE_EMBEDDED_OBJECT &&
                jp.getEmbeddedObject() instanceof Date) {
            Calendar cal = Calendar.getInstance();
            cal.setTime((Date)jp.getEmbeddedObject());
            return cal;
        } else {
            Date date = new Date(jp.getLongValue());
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return cal;
        }
    }
}
