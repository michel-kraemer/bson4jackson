package de.undercouch.bson4jackson.deserializers;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.TreeNode;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.node.ValueNode;
import de.undercouch.bson4jackson.BsonConstants;
import de.undercouch.bson4jackson.BsonParser;

import java.util.regex.Pattern;

/**
 * Deserializes BSON Regex objects (Patterns)
 * @since 2.8.0
 */
public class BsonRegexDeserializer extends ValueDeserializer<Pattern> {
    @Override
    public Pattern deserialize(JsonParser jp, DeserializationContext ctxt) {
        if (jp instanceof BsonParser) {
            BsonParser bsonParser = (BsonParser)jp;
            if (bsonParser.currentToken() != JsonToken.VALUE_EMBEDDED_OBJECT ||
                    bsonParser.getCurrentBsonType() != BsonConstants.TYPE_REGEX) {
                ctxt.reportBadDefinition(Pattern.class,
                        "Current token isn't embedded object or regular expression");
            }
            return (Pattern)bsonParser.getEmbeddedObject();
        } else if (jp.currentToken() == JsonToken.VALUE_EMBEDDED_OBJECT &&
                jp.getEmbeddedObject() instanceof Pattern) {
            return (Pattern)jp.getEmbeddedObject();
        } else {
            TreeNode tree = ctxt.readTree(jp);

            TreeNode patternNode = tree.get("$pattern");
            String pattern = ((ValueNode)patternNode).asString();

            TreeNode flagsNode = tree.get("$flags");
            int flags = ((ValueNode)flagsNode).asInt();

            return Pattern.compile(pattern, flags);
        }
    }
}
