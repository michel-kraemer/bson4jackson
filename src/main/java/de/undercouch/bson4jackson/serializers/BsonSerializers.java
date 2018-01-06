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

import org.bson.BsonTimestamp;
import org.bson.types.Code;
import org.bson.types.ObjectId;
import org.bson.types.Symbol;

import com.fasterxml.jackson.databind.module.SimpleSerializers;

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
		addSerializer(Date.class, new BsonDateSerializer());
		addSerializer(Calendar.class, new BsonCalendarSerializer());
		addSerializer(Code.class, new BsonCodeSerializer());
		addSerializer(ObjectId.class, new BsonObjectIdSerializer());
		addSerializer(Pattern.class, new BsonRegexSerializer());
		addSerializer(Symbol.class, new BsonSymbolSerializer());
		addSerializer(BsonTimestamp.class, new BsonTimestampSerializer());
		addSerializer(UUID.class, new BsonUuidSerializer());
	}
}
