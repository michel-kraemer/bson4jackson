package de.undercouch.bson4jackson.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ValueNode;
import de.undercouch.bson4jackson.BsonConstants;
import de.undercouch.bson4jackson.BsonParser;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Deserializes BSON Regex objects (Patterns)
 * @author Michel Kraemer
 * @since 2.8.0
 */
public class BsonRegexDeserializer extends JsonDeserializer<Pattern> {
    @Override
    @SuppressWarnings("deprecation")
    public Pattern deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        if (jp instanceof BsonParser) {
            BsonParser bsonParser = (BsonParser)jp;
            if (bsonParser.getCurrentToken() != JsonToken.VALUE_EMBEDDED_OBJECT ||
                    bsonParser.getCurrentBsonType() != BsonConstants.TYPE_REGEX) {
                throw ctxt.mappingException(Pattern.class);
            }
            return (Pattern)bsonParser.getEmbeddedObject();
        } else if (jp.getCurrentToken() == JsonToken.VALUE_EMBEDDED_OBJECT &&
                jp.getEmbeddedObject() instanceof Pattern) {
            return (Pattern)jp.getEmbeddedObject();
        } else {
            TreeNode tree = jp.getCodec().readTree(jp);

            TreeNode patternNode = tree.get("$pattern");
            String pattern = ((ValueNode)patternNode).asText();

            TreeNode flagsNode = tree.get("$flags");
            int flags = ((ValueNode)flagsNode).asInt();

            return Pattern.compile(pattern, flags);
        }
    }
}
