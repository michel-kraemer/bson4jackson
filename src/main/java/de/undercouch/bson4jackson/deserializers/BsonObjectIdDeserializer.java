package de.undercouch.bson4jackson.deserializers;

import de.undercouch.bson4jackson.BsonConstants;
import de.undercouch.bson4jackson.BsonParser;
import de.undercouch.bson4jackson.types.ObjectId;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.TreeNode;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.node.ValueNode;

/**
 * Deserializes BSON ObjectId objects
 * @since 2.8.0
 */
public class BsonObjectIdDeserializer extends ValueDeserializer<ObjectId> {
    @Override
    public ObjectId deserialize(JsonParser jp, DeserializationContext ctxt) {
        if (jp instanceof BsonParser bsonParser) {
            if (bsonParser.currentToken() != JsonToken.VALUE_EMBEDDED_OBJECT ||
                    bsonParser.getCurrentBsonType() != BsonConstants.TYPE_OBJECTID) {
                ctxt.reportBadDefinition(ObjectId.class,
                        "Current token isn't a embedded object or isn't objectId");
            }
            return (ObjectId)bsonParser.getEmbeddedObject();
        } else if (jp.currentToken() == JsonToken.VALUE_EMBEDDED_OBJECT &&
                jp.getEmbeddedObject() instanceof ObjectId) {
            return (ObjectId)jp.getEmbeddedObject();
        } else {
            TreeNode tree = ctxt.readTree(jp);
            int timestamp = ((ValueNode)tree.get("$timestamp")).asInt();
            int randomValue1 = ((ValueNode)tree.get("$randomValue1")).asInt();
            short randomValue2 = (short)((ValueNode)tree.get("$randomValue2")).asInt();
            int counter = ((ValueNode)tree.get("$counter")).asInt();
            return new ObjectId(timestamp, counter, randomValue1, randomValue2);
        }
    }
}
