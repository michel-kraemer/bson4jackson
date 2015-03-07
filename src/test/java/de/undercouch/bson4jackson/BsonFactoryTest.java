// Copyright 2010-2011 Michel Kraemer
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

import com.fasterxml.jackson.core.JsonFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

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
    @Category(value = RequiresJackson_v2_3.class)
    public void shouldCreateNewInstanceOnCopy() throws Exception {
        final BsonFactory copy = factory.copy();

        assertNotSame(factory, copy);
    }

    @Test
    @Category(value = RequiresJackson_v2_3.class)
    public void shouldCopyParserFeaturesOnCopy() throws Exception {
        final BsonParser.Feature feature = BsonParser.Feature.HONOR_DOCUMENT_LENGTH;
        factory.configure(feature, !factory.isEnabled(feature));

        final BsonFactory copy = factory.copy();

        assertEquals(factory.isEnabled(feature), copy.isEnabled(feature));
    }

    @Test
    @Category(value = RequiresJackson_v2_3.class)
    public void shouldCopyGeneratorFeaturesOnCopy() throws Exception {
        final BsonGenerator.Feature feature = BsonGenerator.Feature.ENABLE_STREAMING;
        factory.configure(feature, !factory.isEnabled(feature));

        final BsonFactory copy = factory.copy();

        assertEquals(factory.isEnabled(feature), copy.isEnabled(feature));
    }

    @Test
    @Category(value = RequiresJackson_v2_3.class)
    public void shouldCopySuperClass() throws Exception {
        final JsonFactory.Feature feature = JsonFactory.Feature.CANONICALIZE_FIELD_NAMES;
        factory.configure(feature, !factory.isEnabled(feature));

        final BsonFactory copy = factory.copy();

        assertEquals(factory.isEnabled(feature), copy.isEnabled(feature));
    }
}