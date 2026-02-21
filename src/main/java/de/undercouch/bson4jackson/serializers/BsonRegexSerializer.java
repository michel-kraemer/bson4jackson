package de.undercouch.bson4jackson.serializers;

import de.undercouch.bson4jackson.BsonGenerator;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.util.regex.Pattern;

/**
 * Serializer for regular expression patterns
 * @since 1.3
 * @author James Roper
 * @author Michel Kraemer
 */
public class BsonRegexSerializer extends ValueSerializer<Pattern> {
    @Override
    public void serialize(Pattern value, JsonGenerator gen,
            SerializationContext ctxt) {
        if (value == null) {
            ctxt.defaultSerializeNullValue(gen);
        } else if (gen instanceof BsonGenerator) {
            BsonGenerator bgen = (BsonGenerator)gen;
            bgen.writeRegex(value);
        } else {
            gen.writeStartObject();
            gen.writeStringProperty("$pattern", value.pattern());
            gen.writeNumberProperty("$flags", value.flags());
            gen.writeEndObject();
        }
    }
}
