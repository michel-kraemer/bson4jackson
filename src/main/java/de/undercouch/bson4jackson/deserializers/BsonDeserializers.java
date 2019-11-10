package de.undercouch.bson4jackson.deserializers;

import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import de.undercouch.bson4jackson.types.JavaScript;
import de.undercouch.bson4jackson.types.ObjectId;
import de.undercouch.bson4jackson.types.Timestamp;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * BSON deserializers
 * @author Michel Kraemer
 * @since 2.3.2
 */
public class BsonDeserializers extends SimpleDeserializers {
    private static final long serialVersionUID = 261492073508673840L;

    /**
     * Default constructor
     */
    public BsonDeserializers() {
        addDeserializer(Date.class, new BsonDateDeserializer());
        addDeserializer(Calendar.class, new BsonCalendarDeserializer());
        addDeserializer(JavaScript.class, new BsonJavaScriptDeserializer());
        addDeserializer(ObjectId.class, new BsonObjectIdDeserializer());
        addDeserializer(Pattern.class, new BsonRegexDeserializer());
        addDeserializer(Timestamp.class, new BsonTimestampDeserializer());
    }
}
