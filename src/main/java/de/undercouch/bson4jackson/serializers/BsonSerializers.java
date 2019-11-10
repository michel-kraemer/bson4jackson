package de.undercouch.bson4jackson.serializers;

import com.fasterxml.jackson.databind.module.SimpleSerializers;
import de.undercouch.bson4jackson.types.JavaScript;
import de.undercouch.bson4jackson.types.ObjectId;
import de.undercouch.bson4jackson.types.Symbol;
import de.undercouch.bson4jackson.types.Timestamp;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * BSON serializers
 * @author James Roper
 * @since 2.0
 */
public class BsonSerializers extends SimpleSerializers {
    private static final long serialVersionUID = -1327629614239143170L;

    /**
     * Default constructor
     */
    public BsonSerializers() {
        addSerializer(Date.class, new BsonDateSerializer());
        addSerializer(Calendar.class, new BsonCalendarSerializer());
        addSerializer(JavaScript.class, new BsonJavaScriptSerializer());
        addSerializer(ObjectId.class, new BsonObjectIdSerializer());
        addSerializer(Pattern.class, new BsonRegexSerializer());
        addSerializer(Symbol.class, new BsonSymbolSerializer());
        addSerializer(Timestamp.class, new BsonTimestampSerializer());
        addSerializer(UUID.class, new BsonUuidSerializer());
    }
}
