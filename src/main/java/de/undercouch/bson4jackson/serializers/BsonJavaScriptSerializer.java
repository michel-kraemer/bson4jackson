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
import de.undercouch.bson4jackson.types.JavaScript;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Serializer for JavaScript
 *
 * @since 1.3
 * @author James Roper
 */
public class BsonJavaScriptSerializer extends BsonSerializer<JavaScript> {
	@Override
	public void serialize(JavaScript javaScript, BsonGenerator bsonGenerator, SerializerProvider serializerProvider) throws IOException {
		if (javaScript == null) {
			serializerProvider.defaultSerializeNull(bsonGenerator);
		} else {
			bsonGenerator.writeJavaScript(javaScript, serializerProvider);
		}
	}
}
