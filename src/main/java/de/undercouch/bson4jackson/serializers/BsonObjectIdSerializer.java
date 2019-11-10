package de.undercouch.bson4jackson.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.undercouch.bson4jackson.BsonGenerator;
import de.undercouch.bson4jackson.types.ObjectId;

import java.io.IOException;

/**
 * Serializer for ObjectIds
 * @author James Roper
 * @author Michel Kraemer
 * @since 1.3
 */
public class BsonObjectIdSerializer extends JsonSerializer<ObjectId> {
    @Override
    public void serialize(ObjectId value, JsonGenerator gen,
            SerializerProvider serializerProvider) throws IOException {
        if (value == null) {
            serializerProvider.defaultSerializeNull(gen);
        } else if (gen instanceof BsonGenerator) {
            BsonGenerator bgen = (BsonGenerator)gen;
            bgen.writeObjectId(value);
        } else {
            gen.writeStartObject();
            gen.writeNumberField("$time", value.getTime());
            gen.writeNumberField("$machine", value.getMachine());
            gen.writeNumberField("$inc", value.getInc());
            gen.writeEndObject();
        }
    }
}
