package de.undercouch.bson4jackson.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ValueNode;
import de.undercouch.bson4jackson.BsonConstants;
import de.undercouch.bson4jackson.BsonParser;
import de.undercouch.bson4jackson.types.ObjectId;

import java.io.IOException;

/**
 * Deserializes BSON ObjectId objects
 * @author Michel Kraemer
 * @since 2.8.0
 */
public class BsonObjectIdDeserializer extends JsonDeserializer<ObjectId> {
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
    public ObjectId deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        if (jp instanceof BsonParser) {
            BsonParser bsonParser = (BsonParser)jp;
            if (bsonParser.getCurrentToken() != JsonToken.VALUE_EMBEDDED_OBJECT ||
                    bsonParser.getCurrentBsonType() != BsonConstants.TYPE_OBJECTID) {
                ctxt.reportBadDefinition(ObjectId.class, "Current token isn't a embedded object or isn't objectId");
            }
            return (ObjectId)bsonParser.getEmbeddedObject();
        } else if (jp.getCurrentToken() == JsonToken.VALUE_EMBEDDED_OBJECT &&
                jp.getEmbeddedObject() instanceof ObjectId) {
            return (ObjectId)jp.getEmbeddedObject();
        } else {
            TreeNode tree = jp.getCodec().readTree(jp);
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
