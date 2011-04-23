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

package de.undercouch.bson4jackson.io;

/**
 * Provides static methods to change the byte order of single values
 * @author Michel Kraemer
 */
public class ByteOrderUtil {
	/**
	 * Flips the byte order of an integer
	 * @param i the integer
	 * @return the flipped integer
	 */
	public static int flip(int i) {
		int result = 0;
		result |= (i & 0xFF) << 24;
		result |= (i & 0xFF00) << 8;
		result |= ((i & 0xFF0000) >> 8) & 0xFF00;
		result |= ((i & 0xFF000000) >> 24) & 0xFF;
		return result;
	}
}
