// Copyright 2010-2011 Michel Kraemer
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

package de.undercouch.bson4jackson.types;

/**
 * A unique identifier for MongoDB documents. Such identifiers
 * consist of a timestamp, a machine ID and a counter.
 * @author Michel Kraemer
 */
public class ObjectId {
	/**
	 * The timestamp
	 */
	protected final int _time;
	
	/**
	 * The machine ID
	 */
	protected final int _machine;
	
	/**
	 * The counter
	 */
	protected final int _inc;
	
	/**
	 * Constructs a new identifier
	 * @param time the timestamp
	 * @param machine the machine ID
	 * @param inc the counter
	 */
	public ObjectId(int time, int machine, int inc) {
		_time = time;
		_machine = machine;
		_inc = inc;
	}
	
	/**
	 * @return the timestamp
	 */
	public int getTime() {
		return _time;
	}
	
	/**
	 * @return the machine ID
	 */
	public int getMachine() {
		return _machine;
	}
	
	/**
	 * @return the counter
	 */
	public int getInc() {
		return _inc;
	}
}
