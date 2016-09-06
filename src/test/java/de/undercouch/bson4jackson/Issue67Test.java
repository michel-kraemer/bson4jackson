package de.undercouch.bson4jackson;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Issue67Test {
	@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.EXISTING_PROPERTY,
		property = "type"
	)
	@JsonSubTypes({
		@JsonSubTypes.Type(name = "a", value = TypeAsPropertyWithUUIDA.class),
		@JsonSubTypes.Type(name = "b", value = TypeAsPropertyWithUUIDB.class)
	})
	@JsonPropertyOrder({"uuid", "type"})
	public static abstract class TypeAsPropertyWithUUIDBase {
		String type;
		UUID uuid;
		
		public void setType(String type) {
			this.type = type;
		}
		
		public String getType() {
			return type;
		}
		
		public void setUUID(UUID uuid) {
			this.uuid = uuid;
		}
		
		public UUID getUUID() {
			return uuid;
		}
	}
	
	public static class TypeAsPropertyWithUUIDA extends TypeAsPropertyWithUUIDBase {
		TypeAsPropertyWithUUIDA() {
			type = "a";
			uuid = UUID.randomUUID();
		}
	}
	
	public static class TypeAsPropertyWithUUIDB extends TypeAsPropertyWithUUIDBase {
		TypeAsPropertyWithUUIDB() {
			type = "b";
			uuid = UUID.randomUUID();
		}
	}
	
	@Test
	public void parseTypeAsExistingPropertyWithUUID() throws Exception {
		ObjectMapper mapper = new ObjectMapper(new BsonFactory())
				.registerModule(new BsonModule());
		TypeAsPropertyWithUUIDA a = new TypeAsPropertyWithUUIDA();
		byte[] bytes = mapper.writeValueAsBytes(a);
		TypeAsPropertyWithUUIDBase v = mapper.readValue(bytes,
				TypeAsPropertyWithUUIDBase.class);
		assertEquals("a", v.getType());
		assertEquals(TypeAsPropertyWithUUIDA.class, v.getClass());
		assertEquals(a.getUUID(), v.getUUID());
	}
}
