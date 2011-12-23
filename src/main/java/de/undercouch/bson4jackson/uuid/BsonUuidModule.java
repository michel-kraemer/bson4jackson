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

package de.undercouch.bson4jackson.uuid;

import java.util.UUID;

import de.undercouch.bson4jackson.serializers.BsonUuidSerializer;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.module.SimpleModule;

/**
 * Module which registers a serializer that writes UUIDs as BSON binary fields
 * with the UUID sub-type. Register with an ObjectMapper instance to enable this
 * functionality.
 * @author Ed Anuff
 * @deprecated Use {@link de.undercouch.bson4jackson.BsonModule} instead to get all the custom serializers BSON requires
 */
@Deprecated
public class BsonUuidModule extends SimpleModule {
	/**
	 * Default constructor
	 */
	public BsonUuidModule() {
		super("BsonUuidModule", new Version(0, 1, 0, "alpha"));
		addSerializer(UUID.class, new BsonUuidSerializer());
	}
}
