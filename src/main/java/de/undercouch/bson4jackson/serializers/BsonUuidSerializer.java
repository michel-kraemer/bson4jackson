// Copyright 2010-2011 Ed Anuff
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package de.undercouch.bson4jackson.serializers;

import java.io.IOException;
import java.util.UUID;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.SerializerProvider;

import de.undercouch.bson4jackson.BsonConstants;
import de.undercouch.bson4jackson.BsonGenerator;

/**
 * Serializer for writing UUIDs as BSON binary fields with UUID subtype. Can
 * only be used in conjunction with the BsonGenerator.
 * @author Ed Anuff
 */
public class BsonUuidSerializer extends BsonSerializer<UUID> {
	@Override
	public void serialize(UUID value, BsonGenerator bgen,
			SerializerProvider provider) throws IOException {
		bgen.writeBinary(null, BsonConstants.SUBTYPE_UUID,
				uuidToLittleEndianBytes(value), 0, 16);
	}

	/**
	 * Utility routine for converting UUIDs to bytes in little endian format.
	 * @param uuid The UUID to convert
	 * @return a byte array representing the UUID in little endian format
	 */
	private static byte[] uuidToLittleEndianBytes(UUID uuid) {
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
