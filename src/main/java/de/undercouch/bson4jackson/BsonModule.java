package de.undercouch.bson4jackson;

import tools.jackson.core.Version;
import tools.jackson.databind.JacksonModule;
import de.undercouch.bson4jackson.deserializers.BsonDeserializers;
import de.undercouch.bson4jackson.serializers.BsonSerializers;

/**
 * Module that configures Jackson to be able to correctly handle all BSON types
 * @author James Roper
 * @since 1.3
 */
public class BsonModule extends JacksonModule {
    @Override
    public String getModuleName() {
        return "BsonModule";
    }

    @Override
    public Version version() {
        return new Version(3, 0, 0, "", "de.undercouch", "bson4jackson");
    }

    @Override
    public void setupModule(SetupContext context) {
        context.addSerializers(new BsonSerializers());
        context.addDeserializers(new BsonDeserializers());
    }
}
