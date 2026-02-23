package de.undercouch.bson4jackson.deserializers;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.TreeNode;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.node.ValueNode;
import de.undercouch.bson4jackson.BsonConstants;
import de.undercouch.bson4jackson.BsonParser;
import de.undercouch.bson4jackson.types.Timestamp;

/**
 * Deserializes BSON Timestamp objects
 * @since 2.8.0
 */
public class BsonTimestampDeserializer extends ValueDeserializer<Timestamp> {
    @Override
    public Timestamp deserialize(JsonParser jp, DeserializationContext ctxt) {
        if (jp instanceof BsonParser bsonParser) {
            if (bsonParser.currentToken() != JsonToken.VALUE_EMBEDDED_OBJECT ||
                    bsonParser.getCurrentBsonType() != BsonConstants.TYPE_TIMESTAMP) {
                ctxt.reportBadDefinition(Timestamp.class,
                        "Current token isn't a embedded object or a timestamp");
            }
            return (Timestamp)bsonParser.getEmbeddedObject();
        } else if (jp.currentToken() == JsonToken.VALUE_EMBEDDED_OBJECT &&
                jp.getEmbeddedObject() instanceof Timestamp) {
            return (Timestamp)jp.getEmbeddedObject();
        } else {
            TreeNode tree = ctxt.readTree(jp);
            int time = ((ValueNode)tree.get("$time")).asInt();
            int inc = ((ValueNode)tree.get("$inc")).asInt();
            return new Timestamp(time, inc);
        }
    }
}
