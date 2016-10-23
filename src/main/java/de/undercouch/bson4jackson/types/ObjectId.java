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

import de.undercouch.bson4jackson.io.ByteOrderUtil;

/**
 * A unique identifier for MongoDB documents. Such identifiers
 * consist of a timestamp, a machine ID and a counter.
 *
 * @author Michel Kraemer
 */
public class ObjectId {
	private static final char[] HEX_CHARS = new char[]{
			'0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

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

	@Override
	public String toString() {
		char[] chars = new char[24];
		int i = 0;
		for (byte b : toByteArray()) {
			chars[i++] = HEX_CHARS[b >> 4 & 0xF];
			chars[i++] = HEX_CHARS[b & 0xF];
		}
		return new String(chars);
	}

	public byte[] toByteArray() {
		byte[] bytes = new byte[12];
		byte[] timeBytes = ByteOrderUtil.reverseByteArray(_time);
		byte[] machineBytes = ByteOrderUtil.reverseByteArray(_machine);
		byte[] incBytes = ByteOrderUtil.reverseByteArray(_inc);
		bytes[0] = timeBytes[0];
		bytes[1] = timeBytes[1];
		bytes[2] = timeBytes[2];
		bytes[3] = timeBytes[3];
		bytes[4] = machineBytes[0];
		bytes[5] = machineBytes[1];
		bytes[6] = machineBytes[2];
		bytes[7] = machineBytes[3];
		bytes[8] = incBytes[0];
		bytes[9] = incBytes[1];
		bytes[10] = incBytes[2];
		bytes[11] = incBytes[3];
		return bytes;
	}
}
