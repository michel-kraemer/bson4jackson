package de.undercouch.bson4jackson.deserializers;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import de.undercouch.bson4jackson.BsonConstants;
import de.undercouch.bson4jackson.BsonParser;

import java.util.Date;

/**
 * Deserializes BSON date type objects to dates
 * @since 2.3.2
 */
public class BsonDateDeserializer extends ValueDeserializer<Date> {
    @Override
    public Date deserialize(JsonParser jp, DeserializationContext ctxt) {
        if (jp instanceof BsonParser bsonParser) {
            if (bsonParser.currentToken() != JsonToken.VALUE_EMBEDDED_OBJECT ||
                    bsonParser.getCurrentBsonType() != BsonConstants.TYPE_DATETIME) {
                ctxt.reportBadDefinition(Date.class,
                        "Current token isn't embedded object or date time");
            }
            return (Date)bsonParser.getEmbeddedObject();
        } else if (jp.currentToken() == JsonToken.VALUE_EMBEDDED_OBJECT &&
                jp.getEmbeddedObject() instanceof Date) {
            return (Date)jp.getEmbeddedObject();
        } else {
            return new Date(jp.getLongValue());
        }
    }
}
