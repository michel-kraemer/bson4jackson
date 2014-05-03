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

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;

import de.undercouch.bson4jackson.deserializers.BsonDeserializers;
import de.undercouch.bson4jackson.serializers.BsonSerializers;

/**
 * Module that configures Jackson to be able to correctly handle all BSON types
 *
 * @author James Roper
 * @since 1.3
 */
public class BsonModule extends Module {
	@Override
	public String getModuleName() {
		return "BsonModule";
	}

	@Override
	public Version version() {
		return new Version(2, 0, 0, "", "de.undercouch", "bson4jackson");
	}

	@Override
	public void setupModule(SetupContext context) {
		context.addSerializers(new BsonSerializers());
		context.addDeserializers(new BsonDeserializers());
	}
}
