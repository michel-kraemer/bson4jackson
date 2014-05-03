// Copyright 2010-2014 Michel Kraemer
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

package de.undercouch.bson4jackson.deserializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import de.undercouch.bson4jackson.BsonParser;

/**
 * Base class for BSON deserializers
 * @author Michel Kraemer
 * @since 2.3.2
 */
public abstract class BsonDeserializer<T> extends JsonDeserializer<T> {
	@Override
	public T deserialize(JsonParser jsonParser, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		if (!(jsonParser instanceof BsonParser)) {
			throw new JsonGenerationException("BsonDeserializer can " +
					"only be used with BsonParser");
		}
		return deserialize((BsonParser)jsonParser, ctxt);
	}
	
	/**
     * Deserialize an object using the given BsonParser
     * @param bp the BsonParser read from
     * @param ctxt context that can be used to access information about
     * this deserialization activity
     * @return the deserialized object
     */
    public abstract T deserialize(BsonParser bp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException;
}
