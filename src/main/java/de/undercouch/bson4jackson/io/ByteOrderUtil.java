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
 *
 * @author Michel Kraemer
 */
public class ByteOrderUtil {
    /**
     * Flips the byte order of an integer
     *
     * @param i the integer
     * @return the flipped integer
     */
    public static int flip(int i) {
        return makeInt(int0(i), int1(i), int2(i), int3(i));
    }

    public static byte[] reverseByteArray(int i) {
        return new byte[]{int3(i), int2(i), int1(i), int0(i)};
    }

    private static int makeInt(final byte b3, final byte b2, final byte b1, final byte b0) {
        return (((b3) << 24)
                | ((b2 & 0xff) << 16)
                | ((b1 & 0xff) << 8)
                | ((b0 & 0xff)));
    }

    private static byte int3(final int x) {
        return (byte) (x >> 24);
    }

    private static byte int2(final int x) {
        return (byte) (x >> 16);
    }

    private static byte int1(final int x) {
        return (byte) (x >> 8);
    }

    private static byte int0(final int x) {
        return (byte) (x);
    }
}
