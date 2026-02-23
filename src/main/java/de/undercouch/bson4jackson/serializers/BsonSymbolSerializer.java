package de.undercouch.bson4jackson.serializers;

import de.undercouch.bson4jackson.BsonGenerator;
import de.undercouch.bson4jackson.types.Symbol;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * Serializer for BSON symbols
 * @since 1.3
 */
public class BsonSymbolSerializer extends ValueSerializer<Symbol> {
    @Override
    public void serialize(Symbol value, JsonGenerator gen,
            SerializationContext ctxt) {
        if (value == null) {
            ctxt.defaultSerializeNullValue(gen);
        } else if (gen instanceof BsonGenerator) {
            BsonGenerator bgen = (BsonGenerator)gen;
            bgen.writeSymbol(value);
        } else {
            gen.writeString(value.getSymbol());
        }
    }
}
