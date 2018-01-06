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

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import org.bson.BsonTimestamp;
import org.bson.types.Code;
import org.bson.types.ObjectId;

import com.fasterxml.jackson.databind.module.SimpleDeserializers;

/**
 * BSON deserializers
 * @author Michel Kraemer
 * @since 2.3.2
 */
public class BsonDeserializers extends SimpleDeserializers {
	private static final long serialVersionUID = 261492073508673840L;
	
	/**
	 * Default constructor
	 */
	public BsonDeserializers() {
		addDeserializer(Date.class, new BsonDateDeserializer());
		addDeserializer(Calendar.class, new BsonCalendarDeserializer());
		addDeserializer(Code.class, new BsonCodeDeserializer());
		addDeserializer(ObjectId.class, new BsonObjectIdDeserializer());
		addDeserializer(Pattern.class, new BsonRegexDeserializer());
		addDeserializer(BsonTimestamp.class, new BsonTimestampDeserializer());
	}
}
