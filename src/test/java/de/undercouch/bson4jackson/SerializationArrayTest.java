package de.undercouch.bson4jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;

public class SerializationArrayTest {
    private static final String file = "de/undercouch/bson4jackson/bigjson.json";
    private static final ObjectMapper bsonMapper = new ObjectMapper(new BsonFactory());
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    static {
        bsonMapper.registerModule(new BsonModule());
    }

    @Test
    public void testArray() throws Exception {
        final InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(file);
        final JsonNode jsonNode = jsonMapper.readTree(resourceAsStream);
        final byte[] bytes = bsonMapper.writeValueAsBytes(jsonNode);
        final JsonNode readTree = bsonMapper.readTree(bytes);
        Assert.assertEquals(jsonNode, readTree);
    }

}
