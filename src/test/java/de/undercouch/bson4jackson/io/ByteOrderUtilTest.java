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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests {@link ByteOrderUtil}
 * @author Michel Kraemer
 */
public class ByteOrderUtilTest {
	@Test
	public void flipInt() {
		assertEquals(0xDDCCBBAA, ByteOrderUtil.flip(0xAABBCCDD));
		assertEquals(-129, ByteOrderUtil.flip(Integer.MAX_VALUE));
		assertEquals(128, ByteOrderUtil.flip(Integer.MIN_VALUE));
	}
}
