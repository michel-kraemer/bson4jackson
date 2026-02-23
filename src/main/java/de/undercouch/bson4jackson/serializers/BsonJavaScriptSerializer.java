package de.undercouch.bson4jackson.serializers;

import de.undercouch.bson4jackson.BsonGenerator;
import de.undercouch.bson4jackson.types.JavaScript;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * Serializer for JavaScript
 * @since 1.3
 */
public class BsonJavaScriptSerializer extends ValueSerializer<JavaScript> {
    @Override
    public void serialize(JavaScript value, JsonGenerator gen,
            SerializationContext ctxt) {
        if (value == null) {
            ctxt.defaultSerializeNullValue(gen);
        } else if (gen instanceof BsonGenerator bgen) {
            bgen.writeJavaScript(value, ctxt);
        } else {
            gen.writeStartObject();
            gen.writeStringProperty("$code", value.getCode());
            gen.writePOJOProperty("$scope", value.getScope());
            gen.writeEndObject();
        }
    }
}
