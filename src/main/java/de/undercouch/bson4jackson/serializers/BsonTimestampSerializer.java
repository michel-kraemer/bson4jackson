package de.undercouch.bson4jackson.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.undercouch.bson4jackson.BsonGenerator;
import de.undercouch.bson4jackson.types.Timestamp;

import java.io.IOException;

/**
 * Serializer for MongoDB Timestamps
 * @since 1.3
 * @author James Roper
 * @author Michel Kraemer
 */
public class BsonTimestampSerializer extends JsonSerializer<Timestamp> {
    @Override
    public void serialize(Timestamp value, JsonGenerator gen,
            SerializerProvider serializerProvider) throws IOException {
        if (value == null) {
            serializerProvider.defaultSerializeNull(gen);
        } else if (gen instanceof BsonGenerator) {
            BsonGenerator bgen = (BsonGenerator)gen;
            bgen.writeTimestamp(value);
        } else {
            gen.writeStartObject();
            gen.writeNumberField("$time", value.getTime());
            gen.writeNumberField("$inc", value.getInc());
            gen.writeEndObject();
        }
    }
}
