package de.undercouch.bson4jackson;

import com.fasterxml.jackson.core.JsonFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

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
    @Category(value = RequiresJackson_v2_3.class)
    public void shouldCreateNewInstanceOnCopy() {
        BsonFactory copy = factory.copy();
        assertNotSame(factory, copy);
    }

    /**
     * {@link BsonFactory#copy()} should copy parser features
     */
    @Test
    @Category(value = RequiresJackson_v2_3.class)
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
    @Category(value = RequiresJackson_v2_3.class)
    public void shouldCopyGeneratorFeaturesOnCopy() {
        BsonGenerator.Feature feature = BsonGenerator.Feature.ENABLE_STREAMING;
        factory.configure(feature, !factory.isEnabled(feature));

        BsonFactory copy = factory.copy();

        assertEquals(factory.isEnabled(feature), copy.isEnabled(feature));
    }

    /**
     * {@link BsonFactory#copy()} should copy features of its superclass
     * {@link JsonFactory}
     */
    @Test
    @Category(value = RequiresJackson_v2_3.class)
    public void shouldCopySuperClass() {
        JsonFactory.Feature feature = JsonFactory.Feature.CANONICALIZE_FIELD_NAMES;
        factory.configure(feature, !factory.isEnabled(feature));

        BsonFactory copy = factory.copy();

        assertEquals(factory.isEnabled(feature), copy.isEnabled(feature));
    }
}
