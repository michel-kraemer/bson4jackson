package de.undercouch.bson4jackson.serializers;

import de.undercouch.bson4jackson.BsonGenerator;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.util.Calendar;

/**
 * Serializes calendars as BSON date type objects
 * @since 1.3
 */
public class BsonCalendarSerializer extends ValueSerializer<Calendar> {
    @Override
    public void serialize(Calendar value, JsonGenerator gen,
            SerializationContext ctxt) {
        if (value == null) {
            ctxt.defaultSerializeNullValue(gen);
        } else if (gen instanceof BsonGenerator bgen) {
            bgen.writeDateTime(value.getTime());
        } else {
            gen.writeNumber(value.getTime().getTime());
        }
    }
}
