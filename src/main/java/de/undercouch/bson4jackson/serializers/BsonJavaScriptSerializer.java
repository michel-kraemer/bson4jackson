package de.undercouch.bson4jackson.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.undercouch.bson4jackson.BsonGenerator;
import de.undercouch.bson4jackson.types.JavaScript;

import java.io.IOException;

/**
 * Serializer for JavaScript
 * @since 1.3
 * @author James Roper
 * @author Michel Kraemer
 */
public class BsonJavaScriptSerializer extends JsonSerializer<JavaScript> {
    @Override
    public void serialize(JavaScript value, JsonGenerator gen,
            SerializerProvider provider) throws IOException {
        if (value == null) {
            provider.defaultSerializeNull(gen);
        } else if (gen instanceof BsonGenerator) {
            BsonGenerator bgen = (BsonGenerator)gen;
            bgen.writeJavaScript(value, provider);
        } else {
            gen.writeStartObject();
            gen.writeStringField("$code", value.getCode());
            gen.writeObjectField("$scope", value.getScope());
            gen.writeEndObject();
        }
    }
}
