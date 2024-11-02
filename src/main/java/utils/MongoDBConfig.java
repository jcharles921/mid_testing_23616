package utils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Location;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

public class MongoDBConfig {
	private static Datastore datastore;
	private static final String DATABASE_NAME = "auca_library_db";
	private static final String CONNECTION_STRING = "mongodb://localhost:27017";

	static {
		try {
			MongoClient mongoClient = MongoClients.create(CONNECTION_STRING);
			datastore = Morphia.createDatastore(mongoClient, DATABASE_NAME);

// Map the model package where your entities are located
			datastore.getMapper().mapPackage("model");

// Ensure indexes after mapping
			datastore.ensureIndexes();

			System.out.println("MongoDB connection established successfully");
		} catch (Exception e) {
			System.err.println("Error connecting to MongoDB: " + e.getMessage());
			throw new RuntimeException("Could not connect to MongoDB", e);
		}
	}

	public static Datastore getDatastore() {
		return datastore;
	}

// Method to map additional entity classes if needed
	public static void mapAdditionalEntity(Class<?> entityClass) {
		datastore.getMapper().map(entityClass);
		datastore.ensureIndexes();
	}

// Database seeding methods
	public static void initializeLocations(String filePath) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode root = objectMapper.readTree(new File(filePath)).get("data");

// Only seed if the locations collection is empty
		if (datastore.find(Location.class).count() == 0) {
			System.out.println("Starting location data initialization...");
			Map<UUID, Location> locationMap = new HashMap<>();

// First pass: Create all locations
			for (JsonNode provinceNode : root) {
				UUID provinceId = UUID.randomUUID();
				createLocation(provinceNode, null, provinceId, locationMap);
			}

// Second pass: Set parent references and save all locations
			for (Location location : locationMap.values()) {
				datastore.save(location);
			}

			System.out.println("Location data initialization completed successfully");
		} else {
			System.out.println("Location data already exists - skipping initialization");
		}
	}

	private static void createLocation(JsonNode node, Location parentLocation, UUID locationId,
			Map<UUID, Location> locationMap) {
		Location location = new Location();
		location.setLocationId(locationId);
		location.setLocationName(node.get("location_name").asText());
		location.setLocationType(node.get("location_type").asText());

// Set location code if it exists in the JSON
		JsonNode locationCodeNode = node.get("location_code");
		if (locationCodeNode != null) {
			location.setLocationCode(locationCodeNode.asText());
		}

		if (parentLocation != null) {
			location.setParentLocation(parentLocation);
		}

// Store location in map
		locationMap.put(locationId, location);

		JsonNode children = node.get("children");
		if (children != null) {
			for (JsonNode child : children) {
				createLocation(child, location, UUID.randomUUID(), locationMap);
			}
		}
	}
}