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
 * A special internal type used by MongoDB replication and sharding.
 * @author Michel Kraemer
 */
public class Timestamp {
	/**
	 * The increment
	 */
	private final int _inc;
	
	/**
	 * The actual timestamp
	 */
	private final int _time;
	
	/**
	 * Constructs a new timestamp object
	 * @param time the actual timestamp
	 * @param inc the increment
	 */
	public Timestamp(int time, int inc) {
		_inc = inc;
		_time = time;
	}
	
	/**
	 * @return the increment
	 */
	public int getInc() {
		return _inc;
	}
	
	/**
	 * @return the actual timestamp
	 */
	public int getTime() {
		return _time;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Timestamp)) {
			return false;
		}
		Timestamp t = (Timestamp)o;
		return (_inc == t._inc && _time == t._time);
	}
}
