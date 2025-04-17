package de.undercouch.bson4jackson.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import de.undercouch.bson4jackson.BsonConstants;
import de.undercouch.bson4jackson.BsonParser;

import java.io.IOException;
import java.util.Date;

/**
 * Deserializes BSON date type objects to dates
 * @author Michel Kraemer
 * @since 2.3.2
 */
public class BsonDateDeserializer extends JsonDeserializer<Date> {
    @Override
    public Date deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        if (jp instanceof BsonParser) {
            BsonParser bsonParser = (BsonParser)jp;
            if (bsonParser.getCurrentToken() != JsonToken.VALUE_EMBEDDED_OBJECT ||
                    bsonParser.getCurrentBsonType() != BsonConstants.TYPE_DATETIME) {
                ctxt.reportBadDefinition(Date.class,
                        "Current token isn't embedded object or date time");
            }
            return (Date)bsonParser.getEmbeddedObject();
        } else if (jp.getCurrentToken() == JsonToken.VALUE_EMBEDDED_OBJECT &&
                jp.getEmbeddedObject() instanceof Date) {
            return (Date)jp.getEmbeddedObject();
        } else {
            return new Date(jp.getLongValue());
        }
    }
}
