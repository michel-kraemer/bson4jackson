package de.undercouch.bson4jackson.serializers;

import de.undercouch.bson4jackson.BsonGenerator;
import de.undercouch.bson4jackson.types.Timestamp;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * Serializer for MongoDB Timestamps
 * @since 1.3
 */
public class BsonTimestampSerializer extends ValueSerializer<Timestamp> {
    @Override
    public void serialize(Timestamp value, JsonGenerator gen,
            SerializationContext ctxt) {
        if (value == null) {
            ctxt.defaultSerializeNullValue(gen);
        } else if (gen instanceof BsonGenerator bgen) {
            bgen.writeTimestamp(value);
        } else {
            gen.writeStartObject();
            gen.writeNumberProperty("$time", value.getTime());
            gen.writeNumberProperty("$inc", value.getInc());
            gen.writeEndObject();
        }
    }
}
