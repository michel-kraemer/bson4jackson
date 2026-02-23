package de.undercouch.bson4jackson.deserializers;

import de.undercouch.bson4jackson.BsonConstants;
import de.undercouch.bson4jackson.BsonParser;
import de.undercouch.bson4jackson.types.JavaScript;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.TreeNode;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.ValueNode;

import java.util.Map;

/**
 * Deserializes BSON JavaScript objects
 * @since 2.8.0
 */
public class BsonJavaScriptDeserializer extends ValueDeserializer<JavaScript> {
    @Override
    public JavaScript deserialize(JsonParser jp, DeserializationContext ctxt) {
        if (jp instanceof BsonParser) {
            BsonParser bsonParser = (BsonParser)jp;
            if (bsonParser.currentToken() != JsonToken.VALUE_EMBEDDED_OBJECT ||
                    (bsonParser.getCurrentBsonType() != BsonConstants.TYPE_JAVASCRIPT &&
                            bsonParser.getCurrentBsonType() != BsonConstants.TYPE_JAVASCRIPT_WITH_SCOPE)) {
                ctxt.reportBadDefinition(JavaScript.class,
                        "Current token isn't a JavaScript object");
            }
            return (JavaScript)bsonParser.getEmbeddedObject();
        } else if (jp.currentToken() == JsonToken.VALUE_EMBEDDED_OBJECT &&
                jp.getEmbeddedObject() instanceof JavaScript) {
            return (JavaScript)jp.getEmbeddedObject();
        } else {
            TreeNode tree = ctxt.readTree(jp);

            String code = null;
            TreeNode codeNode = tree.get("$code");
            if (codeNode instanceof ValueNode) {
                code = ((ValueNode)codeNode).asString();
            }

            Map<String, Object> scope = null;
            TreeNode scopeNode = tree.get("$scope");
            if (scopeNode instanceof ObjectNode) {
                @SuppressWarnings("unchecked")
                Map<String, Object> scope2 =
                        ctxt.readTreeAsValue((JsonNode)scopeNode, Map.class);
                scope = scope2;
            }

            return new JavaScript(code, scope);
        }
    }
}
