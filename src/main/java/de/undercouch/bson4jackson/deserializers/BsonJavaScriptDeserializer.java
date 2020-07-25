package de.undercouch.bson4jackson.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import de.undercouch.bson4jackson.BsonConstants;
import de.undercouch.bson4jackson.BsonParser;
import de.undercouch.bson4jackson.types.JavaScript;

import java.io.IOException;
import java.util.Map;

/**
 * Deserializes BSON JavaScript objects
 * @author Michel Kraemer
 * @since 2.8.0
 */
public class BsonJavaScriptDeserializer extends JsonDeserializer<JavaScript> {
    @Override
    @SuppressWarnings("deprecation")
    public JavaScript deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        if (jp instanceof BsonParser) {
            BsonParser bsonParser = (BsonParser)jp;
            if (bsonParser.getCurrentToken() != JsonToken.VALUE_EMBEDDED_OBJECT ||
                    (bsonParser.getCurrentBsonType() != BsonConstants.TYPE_JAVASCRIPT &&
                            bsonParser.getCurrentBsonType() != BsonConstants.TYPE_JAVASCRIPT_WITH_SCOPE)) {
                throw ctxt.mappingException(JavaScript.class);
            }
            return (JavaScript)bsonParser.getEmbeddedObject();
        } else if (jp.getCurrentToken() == JsonToken.VALUE_EMBEDDED_OBJECT &&
                jp.getEmbeddedObject() instanceof JavaScript) {
            return (JavaScript)jp.getEmbeddedObject();
        } else {
            TreeNode tree = jp.getCodec().readTree(jp);

            String code = null;
            TreeNode codeNode = tree.get("$code");
            if (codeNode instanceof ValueNode) {
                code = ((ValueNode)codeNode).asText();
            }

            Map<String, Object> scope = null;
            TreeNode scopeNode = tree.get("$scope");
            if (scopeNode instanceof ObjectNode) {
                @SuppressWarnings("unchecked")
                Map<String, Object> scope2 =
                        jp.getCodec().treeToValue(scopeNode, Map.class);
                scope = scope2;
            }

            return new JavaScript(code, scope);
        }
    }
}
