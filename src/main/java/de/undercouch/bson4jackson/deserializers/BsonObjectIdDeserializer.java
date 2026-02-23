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
    private final boolean useLegacyFormat;

    /**
     * Default constructor
     */
    public BsonObjectIdDeserializer() {
        this(false);
    }

    /**
     * Constructor that allows the legacy format to be enabled
     * @param useLegacyFormat {@code true} if the legacy format should be enabled
     * @deprecated Legacy ObjectId format is deprecated. Please use the default
     * constructor to create ObjectIds in the new format.
     */
    @Deprecated
    public BsonObjectIdDeserializer(boolean useLegacyFormat) {
        this.useLegacyFormat = useLegacyFormat;
    }

    @Override
    @SuppressWarnings("deprecation")
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
            if (useLegacyFormat) {
                int time = ((ValueNode)tree.get("$time")).asInt();
                int machine = ((ValueNode)tree.get("$machine")).asInt();
                int inc = ((ValueNode)tree.get("$inc")).asInt();
                return new ObjectId(time, machine, inc);
            } else {
                int timestamp = ((ValueNode)tree.get("$timestamp")).asInt();
                int randomValue1 = ((ValueNode)tree.get("$randomValue1")).asInt();
                short randomValue2 = (short)((ValueNode)tree.get("$randomValue2")).asInt();
                int counter = ((ValueNode)tree.get("$counter")).asInt();
                return new ObjectId(timestamp, counter, randomValue1, randomValue2);
            }
        }
    }
}
