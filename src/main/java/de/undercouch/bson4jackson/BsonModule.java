package de.undercouch.bson4jackson;

import java.io.IOException;
import java.util.UUID;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.module.SimpleModule;

public class BsonModule extends SimpleModule {

	public BsonModule() {
		super("BsonModule", new Version(0, 1, 0, "alpha"));
		this.addSerializer(UUID.class, new BsonUuidSerializer());
	}

	static public class BsonUuidSerializer extends JsonSerializer<UUID> {
		@Override
		public void serialize(UUID value, JsonGenerator jgen,
				SerializerProvider provider) throws IOException,
				JsonProcessingException {
			if (!(jgen instanceof BsonGenerator)) {
				throw new JsonGenerationException(
						"BsonUuidSerializer can only be used with BsonGenerator");
			}
			((BsonGenerator) jgen).writeBinary(null,
					BsonConstants.SUBTYPE_UUID, uuidToLittleEndianBytes(value),
					0, 16);
		}
	}

	public static byte[] uuidToLittleEndianBytes(UUID uuid) {
		long msb = uuid.getMostSignificantBits();
		long lsb = uuid.getLeastSignificantBits();
		byte[] buffer = new byte[16];

		for (int i = 0; i < 8; i++) {
			buffer[7 - i] = (byte) (msb >>> 8 * (7 - i));
		}
		for (int i = 8; i < 16; i++) {
			buffer[23 - i] = (byte) (lsb >>> 8 * (7 - i));
		}

		return buffer;
	}

}
