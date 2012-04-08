package de.undercouch.bson4jackson.serializers;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.module.SimpleSerializers;

import de.undercouch.bson4jackson.types.JavaScript;
import de.undercouch.bson4jackson.types.ObjectId;
import de.undercouch.bson4jackson.types.Symbol;
import de.undercouch.bson4jackson.types.Timestamp;

/**
 * Bson Serializers
 *
 * @author James Roper
 * @since 2.0
 */
public class BsonSerializers extends SimpleSerializers {
	/**
	 * Default constructor
	 */
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
