package de.undercouch.bson4jackson;

import org.junit.Before;
import org.junit.Test;
import tools.jackson.core.TokenStreamFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * Tests {@link BsonFactory}
 * @author Andy Coates
 */
public class BsonFactoryTest {
    private BsonFactory factory;

    /**
     * Set up the unit tests
     */
    @Before
    public void setUp() {
        factory = new BsonFactory();
    }

    /**
     * {@link BsonFactory#copy()} should create a new instance
     */
    @Test
    public void shouldCreateNewInstanceOnCopy() {
        BsonFactory copy = factory.copy();
        assertNotSame(factory, copy);
    }

    /**
     * {@link BsonFactory#copy()} should copy parser features
     */
    @Test
    public void shouldCopyParserFeaturesOnCopy() {
        BsonParser.Feature feature = BsonParser.Feature.HONOR_DOCUMENT_LENGTH;
        factory.configure(feature, !factory.isEnabled(feature));

        BsonFactory copy = factory.copy();

        assertEquals(factory.isEnabled(feature), copy.isEnabled(feature));
    }

    /**
     * {@link BsonFactory#copy()} should copy generator features
     */
    @Test
    public void shouldCopyGeneratorFeaturesOnCopy() {
        BsonGenerator.Feature feature = BsonGenerator.Feature.ENABLE_STREAMING;
        factory.configure(feature, !factory.isEnabled(feature));

        BsonFactory copy = factory.copy();

        assertEquals(factory.isEnabled(feature), copy.isEnabled(feature));
    }

    /**
     * {@link BsonFactory#copy()} should copy features of its superclass
     * {@link tools.jackson.core.json.JsonFactory}
     */
    @Test
    public void shouldCopySuperClass() {
        // In Jackson 3, TokenStreamFactory features are immutable after construction,
        // so we just verify the copy preserves the default feature state
        TokenStreamFactory.Feature feature = TokenStreamFactory.Feature.CANONICALIZE_PROPERTY_NAMES;

        BsonFactory copy = factory.copy();

        assertEquals(factory.isEnabled(feature), copy.isEnabled(feature));
    }
}
