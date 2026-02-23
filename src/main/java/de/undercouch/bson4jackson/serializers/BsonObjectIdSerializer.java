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
            SerializationContext ctxt) {
        if (value == null) {
            ctxt.defaultSerializeNullValue(gen);
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
                gen.writeNumberProperty("$time", value.getTime());
                gen.writeNumberProperty("$machine", value.getMachine());
                gen.writeNumberProperty("$inc", value.getInc());
            } else {
                gen.writeNumberProperty("$timestamp", value.getTimestamp());
                gen.writeNumberProperty("$randomValue1", value.getRandomValue1());
                gen.writeNumberProperty("$randomValue2", (int)value.getRandomValue2());
                gen.writeNumberProperty("$counter", value.getCounter());
            }
            gen.writeEndObject();
        }
    }
}
