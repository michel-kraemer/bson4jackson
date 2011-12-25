// Copyright 2010-2011 James Roper
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

import com.fasterxml.jackson.databind.SerializerProvider;
import de.undercouch.bson4jackson.BsonGenerator;
import de.undercouch.bson4jackson.types.ObjectId;

import java.io.IOException;

/**
 * Serializer for ObjectIds
 *
 * @author James Roper
 * @since 1.3
 */
public class BsonObjectIdSerializer extends BsonSerializer<ObjectId> {
	@Override
	public void serialize(ObjectId objectId, BsonGenerator bsonGenerator, SerializerProvider serializerProvider) throws IOException {
		if (objectId == null) {
			serializerProvider.defaultSerializeNull(bsonGenerator);
		} else {
			bsonGenerator.writeObjectId(objectId);
		}
	}
}
