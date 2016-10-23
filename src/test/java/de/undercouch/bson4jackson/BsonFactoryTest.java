// Copyright 2010-2016 Michel Kraemer
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package de.undercouch.bson4jackson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.fasterxml.jackson.core.JsonFactory;

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
