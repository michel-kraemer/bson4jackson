package de.undercouch.bson4jackson.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.undercouch.bson4jackson.BsonGenerator;
import de.undercouch.bson4jackson.types.Symbol;

import java.io.IOException;

/**
 * Serializer for BSON symbols
 * @since 1.3
 * @author James Roper
 * @author Michel Kraemer
 */
public class BsonSymbolSerializer extends JsonSerializer<Symbol> {
    @Override
    public void serialize(Symbol value, JsonGenerator gen,
            SerializerProvider serializerProvider) throws IOException {
        if (value == null) {
            serializerProvider.defaultSerializeNull(gen);
        } else if (gen instanceof BsonGenerator) {
            BsonGenerator bgen = (BsonGenerator)gen;
            bgen.writeSymbol(value);
        } else {
            gen.writeString(value.getSymbol());
        }
    }
}
