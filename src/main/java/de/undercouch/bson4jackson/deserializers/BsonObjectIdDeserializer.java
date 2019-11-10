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
    @Override
    @SuppressWarnings("deprecation")
    public ObjectId deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        if (jp instanceof BsonParser) {
            BsonParser bsonParser = (BsonParser)jp;
            if (bsonParser.getCurrentToken() != JsonToken.VALUE_EMBEDDED_OBJECT ||
                    bsonParser.getCurrentBsonType() != BsonConstants.TYPE_OBJECTID) {
                throw ctxt.mappingException(ObjectId.class);
            }
            return (ObjectId)bsonParser.getEmbeddedObject();
        } else {
            TreeNode tree = jp.getCodec().readTree(jp);
            int time = ((ValueNode)tree.get("$time")).asInt();
            int machine = ((ValueNode)tree.get("$machine")).asInt();
            int inc = ((ValueNode)tree.get("$inc")).asInt();
            return new ObjectId(time, machine, inc);
        }
    }
}
