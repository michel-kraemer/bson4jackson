// Copyright 2010-2016 Michel Kraemer
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

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.module.SimpleSerializers;

import de.undercouch.bson4jackson.types.JavaScript;
import de.undercouch.bson4jackson.types.ObjectId;
import de.undercouch.bson4jackson.types.Symbol;
import de.undercouch.bson4jackson.types.Timestamp;

/**
 * Bson Serializers
 *
 * @author James Roper
 * @since 2.0
 */
public class BsonSerializers extends SimpleSerializers {
	private static final long serialVersionUID = -1327629614239143170L;

	/**
	 * Default constructor
	 */
	public BsonSerializers() {
		addSerializer(UUID.class, new BsonUuidSerializer());
		addSerializer(Date.class, new BsonDateSerializer());
		addSerializer(Calendar.class, new BsonCalendarSerializer());
		addSerializer(ObjectId.class, new BsonObjectIdSerializer());
		addSerializer(Pattern.class, new BsonRegexSerializer());
		addSerializer(JavaScript.class, new BsonJavaScriptSerializer());
		addSerializer(Timestamp.class, new BsonTimestampSerializer());
		addSerializer(Symbol.class, new BsonSymbolSerializer());
	}
}
