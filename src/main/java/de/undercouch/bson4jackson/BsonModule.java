package de.undercouch.bson4jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import de.undercouch.bson4jackson.deserializers.BsonDeserializers;
import de.undercouch.bson4jackson.serializers.BsonSerializers;

/**
 * Module that configures Jackson to be able to correctly handle all BSON types
 * @author James Roper
 * @since 1.3
 */
public class BsonModule extends Module {
    @Override
    public String getModuleName() {
        return "BsonModule";
    }

    @Override
    public Version version() {
        return new Version(2, 9, 2, "", "de.undercouch", "bson4jackson");
    }

    @Override
    public void setupModule(SetupContext context) {
        context.addSerializers(new BsonSerializers());
        context.addDeserializers(new BsonDeserializers());
    }
}
