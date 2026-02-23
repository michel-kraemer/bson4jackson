package de.undercouch.bson4jackson.deserializers;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import de.undercouch.bson4jackson.BsonConstants;
import de.undercouch.bson4jackson.BsonParser;

import java.util.Calendar;
import java.util.Date;

/**
 * Deserializes BSON date type objects to calendars
 * @since 2.3.2
 */
public class BsonCalendarDeserializer extends ValueDeserializer<Calendar> {
    @Override
    public Calendar deserialize(JsonParser jp, DeserializationContext ctxt) {
        if (jp instanceof BsonParser) {
            BsonParser bsonParser = (BsonParser)jp;
            if (bsonParser.currentToken() != JsonToken.VALUE_EMBEDDED_OBJECT ||
                    bsonParser.getCurrentBsonType() != BsonConstants.TYPE_DATETIME) {
                ctxt.reportBadDefinition(Date.class,
                        "Current token isn't embedded object or date time");
            }

            Object obj = bsonParser.getEmbeddedObject();
            if (obj == null) {
                return null;
            }

            Calendar cal = Calendar.getInstance();
            cal.setTime((Date)obj);
            return cal;
        } else if (jp.currentToken() == JsonToken.VALUE_EMBEDDED_OBJECT &&
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
