package de.undercouch.bson4jackson.serializers;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.Serializers;
import de.undercouch.bson4jackson.types.JavaScript;
import de.undercouch.bson4jackson.types.ObjectId;
import de.undercouch.bson4jackson.types.Symbol;
import de.undercouch.bson4jackson.types.Timestamp;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Bson Serializers
 *
 * @author jroper
 * @since 2.0
 */
public class BsonSerializers extends SimpleSerializers {

	public BsonSerializers() {
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
