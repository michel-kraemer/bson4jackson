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

package de.undercouch.bson4jackson;

/**
 * Constants used within the BSON format
 * @author Michel Kraemer
 */
@SuppressWarnings("all")
public final class BsonConstants {
	/**
	 * End of document
	 */
	public static final byte TYPE_END = 0x00;
	
	/**
	 * End of string
	 */
	public static final byte END_OF_STRING = 0x00;
	
	/**
	 * Type markers
	 */
	public static final byte TYPE_DOUBLE = 0x01;
	public static final byte TYPE_STRING = 0x02;
	public static final byte TYPE_DOCUMENT = 0x03;
	public static final byte TYPE_ARRAY = 0x04;
	public static final byte TYPE_BINARY = 0x05;
	public static final byte TYPE_UNDEFINED = 0x06;
	public static final byte TYPE_OBJECTID = 0x07;
	public static final byte TYPE_BOOLEAN = 0x08;
	public static final byte TYPE_DATETIME = 0x09;
	public static final byte TYPE_NULL = 0x0A;
	public static final byte TYPE_REGEX = 0x0B;
	public static final byte TYPE_DBPOINTER = 0x0C;
	public static final byte TYPE_JAVASCRIPT = 0x0D;
	public static final byte TYPE_SYMBOL = 0x0E;
	public static final byte TYPE_JAVASCRIPT_WITH_SCOPE = 0x0F;
	public static final byte TYPE_INT32 = 0x10;
	public static final byte TYPE_TIMESTAMP = 0x11;
	public static final byte TYPE_INT64 = 0x12;
	public static final byte TYPE_MINKEY = (byte)0xFF;
	public static final byte TYPE_MAXKEY = 0x7f;
	
	/**
	 * Binary subtypes
	 */
	public static final byte SUBTYPE_BINARY = 0x00;
	public static final byte SUBTYPE_FUNCTION = 0x01;
	public static final byte SUBTYPE_BINARY_OLD = 0x02;
	public static final byte SUBTYPE_UUID = 0x03;
	public static final byte SUBTYPE_MD5 = 0x05;
	public static final byte SUBTYPE_USER_DEFINED = (byte)0x80;
}
