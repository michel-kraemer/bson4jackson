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
    private final boolean useLegacyFormat;

    /**
     * Default constructor
     */
    public BsonObjectIdSerializer() {
        this(false);
    }

    /**
     * Constructor that allows the legacy format to be enabled
     * @param useLegacyFormat {@code true} if the legacy format should be enabled
     * @deprecated Legacy ObjectId format is deprecated. Please use the default
     * constructor to create ObjectIds in the new format.
     */
    @Deprecated
    public BsonObjectIdSerializer(boolean useLegacyFormat) {
        this.useLegacyFormat = useLegacyFormat;
    }

    @Override
    public void serialize(ObjectId value, JsonGenerator gen,
            SerializerProvider serializerProvider) throws IOException {
        if (value == null) {
            serializerProvider.defaultSerializeNull(gen);
        } else if (gen instanceof BsonGenerator) {
            BsonGenerator bgen = (BsonGenerator)gen;
            if (useLegacyFormat) {
                bgen.writeObjectIdLegacy(value);
            } else {
                bgen.writeObjectId(value);
            }
        } else {
            gen.writeStartObject();
            if (useLegacyFormat) {
                gen.writeNumberField("$time", value.getTime());
                gen.writeNumberField("$machine", value.getMachine());
                gen.writeNumberField("$inc", value.getInc());
            } else {
                gen.writeNumberField("$timestamp", value.getTimestamp());
                gen.writeNumberField("$randomValue1", value.getRandomValue1());
                gen.writeNumberField("$randomValue2", (int)value.getRandomValue2());
                gen.writeNumberField("$counter", value.getCounter());
            }
            gen.writeEndObject();
        }
    }
}
