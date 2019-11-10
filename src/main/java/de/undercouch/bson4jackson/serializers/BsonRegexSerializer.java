package de.undercouch.bson4jackson.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.undercouch.bson4jackson.BsonGenerator;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Serializer for regular expression patterns
 * @since 1.3
 * @author James Roper
 * @author Michel Kraemer
 */
public class BsonRegexSerializer extends JsonSerializer<Pattern> {
    @Override
    public void serialize(Pattern value, JsonGenerator gen,
            SerializerProvider serializerProvider) throws IOException {
        if (value == null) {
            serializerProvider.defaultSerializeNull(gen);
        } else if (gen instanceof BsonGenerator) {
            BsonGenerator bgen = (BsonGenerator)gen;
            bgen.writeRegex(value);
        } else {
            gen.writeStartObject();
            gen.writeStringField("$pattern", value.pattern());
            gen.writeNumberField("$flags", value.flags());
            gen.writeEndObject();
        }
    }
}
