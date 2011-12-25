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

package de.undercouch.bson4jackson;

import de.undercouch.bson4jackson.serializers.*;
import de.undercouch.bson4jackson.types.JavaScript;
import de.undercouch.bson4jackson.types.ObjectId;
import de.undercouch.bson4jackson.types.Symbol;
import de.undercouch.bson4jackson.types.Timestamp;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.module.SimpleModule;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Module that configures Jackson to be able to correctly handle all BSON types
 *
 * @author James Roper
 * @since 1.3
 */
public class BsonModule extends SimpleModule {
	public BsonModule() {
		super("BsonModule", new Version(1, 3, 0, ""));
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
