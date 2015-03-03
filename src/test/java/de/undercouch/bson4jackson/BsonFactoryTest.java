package de.undercouch.bson4jackson;

import com.fasterxml.jackson.core.JsonFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests {@link BsonFactory}
 * @author Andy Coates
 */
public class BsonFactoryTest {
    private BsonFactory factory;

    @Before
    public void setUp() throws Exception {
        factory = new BsonFactory();
    }

    @Test
    public void shouldCreateNewInstanceOnCopy() throws Exception {
        final BsonFactory copy = factory.copy();

        assertNotSame(factory, copy);
    }

    @Test
    public void shouldCopyParserFeaturesOnCopy() throws Exception {
        final BsonParser.Feature feature = BsonParser.Feature.HONOR_DOCUMENT_LENGTH;
        factory.configure(feature, !factory.isEnabled(feature));

        final BsonFactory copy = factory.copy();

        assertEquals(factory.isEnabled(feature), copy.isEnabled(feature));
    }

    @Test
    public void shouldCopyGeneratorFeaturesOnCopy() throws Exception {
        final BsonGenerator.Feature feature = BsonGenerator.Feature.ENABLE_STREAMING;
        factory.configure(feature, !factory.isEnabled(feature));

        final BsonFactory copy = factory.copy();

        assertEquals(factory.isEnabled(feature), copy.isEnabled(feature));
    }

    @Test
    public void shouldCopySuperClass() throws Exception {
        final JsonFactory.Feature feature = JsonFactory.Feature.CANONICALIZE_FIELD_NAMES;
        factory.configure(feature, !factory.isEnabled(feature));

        final BsonFactory copy = factory.copy();

        assertEquals(factory.isEnabled(feature), copy.isEnabled(feature));
    }
}