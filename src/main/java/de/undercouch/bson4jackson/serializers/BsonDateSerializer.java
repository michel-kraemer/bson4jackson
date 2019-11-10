package de.undercouch.bson4jackson.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.undercouch.bson4jackson.BsonGenerator;

import java.io.IOException;
import java.util.Date;

/**
 * Serializes dates as BSON date type objects
 * @author James Roper
 * @author Michel Kraemer
 * @since 1.3
 */
public class BsonDateSerializer extends JsonSerializer<Date> {
    @Override
    public void serialize(Date value, JsonGenerator gen,
            SerializerProvider provider) throws IOException {
        if (value == null) {
            provider.defaultSerializeNull(gen);
        } else if (gen instanceof BsonGenerator) {
            BsonGenerator bgen = (BsonGenerator)gen;
            bgen.writeDateTime(value);
        } else {
            gen.writeNumber(value.getTime());
        }
    }
}
