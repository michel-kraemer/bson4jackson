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

import de.undercouch.bson4jackson.BsonGenerator;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

/**
 * Base class for BSON serializers
 *
 * @author James Roper
 */
public abstract class BsonSerializer<T> extends JsonSerializer<T> {
	@Override
	public void serialize(T t, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
		if (!(jsonGenerator instanceof BsonGenerator)) {
			throw new JsonGenerationException("BsonDateSerializer can " +
					"only be used with BsonGenerator");
		}
		serialize(t, (BsonGenerator) jsonGenerator, serializerProvider);
	}

	/**
	 * Serialize the given object using the given BsonGenerator
	 *
	 * @param t				  The object to serialize
	 * @param bsonGenerator	  The generator to serialize to
	 * @param serializerProvider The serialization provider
	 * @throws IOException			 If an error occurred writing to the stream
	 * @throws JsonProcessingException If a JSON error occurred
	 */
	public abstract void serialize(T t, BsonGenerator bsonGenerator, SerializerProvider serializerProvider)
			throws IOException, JsonProcessingException;
}
