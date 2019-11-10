package de.undercouch.bson4jackson.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.undercouch.bson4jackson.BsonGenerator;

import java.io.IOException;
import java.util.Calendar;

/**
 * Serializes calendars as BSON date type objects
 * @author James Roper
 * @author Michel Kraemer
 * @since 1.3
 */
public class BsonCalendarSerializer extends JsonSerializer<Calendar> {
    @Override
    public void serialize(Calendar value, JsonGenerator gen,
            SerializerProvider provider) throws IOException {
        if (value == null) {
            provider.defaultSerializeNull(gen);
        } else if (gen instanceof BsonGenerator) {
            BsonGenerator bgen = (BsonGenerator)gen;
            bgen.writeDateTime(value.getTime());
        } else {
            gen.writeNumber(value.getTime().getTime());
        }
    }
}
