package de.undercouch.bson4jackson.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ValueNode;
import de.undercouch.bson4jackson.BsonConstants;
import de.undercouch.bson4jackson.BsonParser;
import de.undercouch.bson4jackson.types.Timestamp;

import java.io.IOException;

/**
 * Deserializes BSON Timestamp objects
 * @author Michel Kraemer
 * @since 2.8.0
 */
public class BsonTimestampDeserializer extends JsonDeserializer<Timestamp> {
    @Override
    public Timestamp deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        if (jp instanceof BsonParser) {
            BsonParser bsonParser = (BsonParser)jp;
            if (bsonParser.getCurrentToken() != JsonToken.VALUE_EMBEDDED_OBJECT ||
                    bsonParser.getCurrentBsonType() != BsonConstants.TYPE_TIMESTAMP) {
                ctxt.reportBadDefinition(Timestamp.class,
                        "Current token isn't a embedded object or a timestamp");
            }
            return (Timestamp)bsonParser.getEmbeddedObject();
        } else if (jp.getCurrentToken() == JsonToken.VALUE_EMBEDDED_OBJECT &&
                jp.getEmbeddedObject() instanceof Timestamp) {
            return (Timestamp)jp.getEmbeddedObject();
        } else {
            TreeNode tree = jp.getCodec().readTree(jp);
            int time = ((ValueNode)tree.get("$time")).asInt();
            int inc = ((ValueNode)tree.get("$inc")).asInt();
            return new Timestamp(time, inc);
        }
    }
}
