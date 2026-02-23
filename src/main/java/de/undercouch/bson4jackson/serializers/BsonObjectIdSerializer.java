package de.undercouch.bson4jackson.serializers;

import de.undercouch.bson4jackson.BsonGenerator;
import de.undercouch.bson4jackson.types.ObjectId;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * Serializer for ObjectIds
 * @since 1.3
 */
public class BsonObjectIdSerializer extends ValueSerializer<ObjectId> {
    @Override
    public void serialize(ObjectId value, JsonGenerator gen,
            SerializationContext ctxt) {
        if (value == null) {
            ctxt.defaultSerializeNullValue(gen);
        } else if (gen instanceof BsonGenerator bgen) {
            bgen.writeObjectId(value);
        } else {
            gen.writeStartObject();
            gen.writeNumberProperty("$timestamp", value.getTimestamp());
            gen.writeNumberProperty("$randomValue1", value.getRandomValue1());
            gen.writeNumberProperty("$randomValue2", (int)value.getRandomValue2());
            gen.writeNumberProperty("$counter", value.getCounter());
            gen.writeEndObject();
        }
    }
}
