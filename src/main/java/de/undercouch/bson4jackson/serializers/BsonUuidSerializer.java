package de.undercouch.bson4jackson.serializers;

import de.undercouch.bson4jackson.BsonConstants;
import de.undercouch.bson4jackson.BsonGenerator;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.ser.jdk.UUIDSerializer;

import java.util.UUID;

/**
 * Serializer for writing UUIDs as BSON binary fields with UUID subtype
 * @author Ed Anuff
 * @author Michel Kraemer
 */
public class BsonUuidSerializer extends ValueSerializer<UUID> {
    @Override
    public void serialize(UUID value, JsonGenerator gen,
            SerializationContext ctxt) {
        if (gen instanceof BsonGenerator) {
            BsonGenerator bgen = (BsonGenerator)gen;
            bgen.writeBinary(null, BsonConstants.SUBTYPE_UUID,
                    uuidToLittleEndianBytes(value), 0, 16);
        } else {
            new UUIDSerializer().serialize(value, gen, ctxt);
        }
    }

    /**
     * Utility routine for converting UUIDs to bytes in little endian format.
     * @param uuid The UUID to convert
     * @return a byte array representing the UUID in little endian format
     */
    protected static byte[] uuidToLittleEndianBytes(UUID uuid) {
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        byte[] buffer = new byte[16];

        for (int i = 0; i < 8; i++) {
            buffer[i] = (byte) (msb >>> 8 * i);
        }
        for (int i = 8; i < 16; i++) {
            buffer[i] = (byte) (lsb >>> 8 * (i - 16));
        }

        return buffer;
    }
}
