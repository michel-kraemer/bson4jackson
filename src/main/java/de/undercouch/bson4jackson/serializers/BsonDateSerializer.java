package de.undercouch.bson4jackson.serializers;

import de.undercouch.bson4jackson.BsonGenerator;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.util.Date;

/**
 * Serializes dates as BSON date type objects
 * @author James Roper
 * @author Michel Kraemer
 * @since 1.3
 */
public class BsonDateSerializer extends ValueSerializer<Date> {
    @Override
    public void serialize(Date value, JsonGenerator gen,
            SerializationContext ctxt) {
        if (value == null) {
            ctxt.defaultSerializeNullValue(gen);
        } else if (gen instanceof BsonGenerator) {
            BsonGenerator bgen = (BsonGenerator)gen;
            bgen.writeDateTime(value);
        } else {
            gen.writeNumber(value.getTime());
        }
    }
}
