package utils;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Location;
import org.bson.UuidRepresentation;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MongoDBConfig {
    private static Datastore datastore;
    private static final String DATABASE_NAME = "auca_library_db";
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";

    static {
        try {
            MongoClientSettings settings = MongoClientSettings.builder().uuidRepresentation(UuidRepresentation.STANDARD)
                    .applyConnectionString(new com.mongodb.ConnectionString(CONNECTION_STRING)).build();

            MongoClient mongoClient = MongoClients.create(settings);
            datastore = Morphia.createDatastore(mongoClient, DATABASE_NAME);
            datastore.getMapper().mapPackage("model");
            datastore.ensureIndexes();
            System.out.println("MongoDB connection established successfully");
        } catch (Exception e) {
            System.out.println("Error connecting to MongoDB: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Could not connect to MongoDB", e);
        }
    }

    public static Datastore getDatastore() {
        return datastore;
    }

    public static void initializeLocations(String filePath) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        File jsonFile = new File(filePath);
        if (!jsonFile.exists()) {
            throw new IOException("Location data file not found: " + filePath);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonFile);
        JsonNode dataNode = rootNode.get("data");

        if (dataNode == null || !dataNode.isArray()) {
            throw new IOException("Invalid JSON structure: 'data' field must be an array");
        }

        if (datastore.find(Location.class).count() == 0) {
            System.out.println("Starting location data initialization...");
            Map<UUID, Location> locationMap = new HashMap<>();
            int locationCount = 0;

            try {
                // Process top-level provinces
                for (JsonNode provinceContainer : dataNode) {
                    Iterator<String> provinceFields = provinceContainer.fieldNames();
                    while (provinceFields.hasNext()) {
                        String provinceName = provinceFields.next();
                        JsonNode districtsArray = provinceContainer.get(provinceName);

                        System.out.println("Processing province: " + provinceName);

                        // Create Province
                        Location province = createBaseLocation(provinceName, "Province", null);
                        locationMap.put(province.getLocationId(), province);
                        locationCount++;

                        // Process Districts
                        if (districtsArray.isArray()) {
                            for (JsonNode districtContainer : districtsArray) {
                                Iterator<String> districtFields = districtContainer.fieldNames();
                                while (districtFields.hasNext()) {
                                    String districtName = districtFields.next();
                                    JsonNode sectorsArray = districtContainer.get(districtName);

                                    System.out.println("Processing district: " + districtName);

                                    // Create District
                                    Location district = createBaseLocation(districtName, "District", province);
                                    locationMap.put(district.getLocationId(), district);
                                    locationCount++;

                                    // Process Sectors
                                    if (sectorsArray.isArray()) {
                                        for (JsonNode sectorContainer : sectorsArray) {
                                            Iterator<String> sectorFields = sectorContainer.fieldNames();
                                            while (sectorFields.hasNext()) {
                                                String sectorName = sectorFields.next();
                                                JsonNode cellsArray = sectorContainer.get(sectorName);

                                                System.out.println("Processing sector: " + sectorName);

                                                // Create Sector
                                                Location sector = createBaseLocation(sectorName, "Sector", district);
                                                locationMap.put(sector.getLocationId(), sector);
                                                locationCount++;

                                                // Process Cells
                                                if (cellsArray.isArray()) {
                                                    for (JsonNode cellContainer : cellsArray) {
                                                        Iterator<String> cellFields = cellContainer.fieldNames();
                                                        while (cellFields.hasNext()) {
                                                            String cellName = cellFields.next();
                                                            JsonNode villagesArray = cellContainer.get(cellName);

                                                            System.out.println("Processing cell: " + cellName);

                                                            // Create Cell
                                                            Location cell = createBaseLocation(cellName, "Cell", sector);
                                                            locationMap.put(cell.getLocationId(), cell);
                                                            locationCount++;

                                                            // Process Villages
                                                            if (villagesArray.isArray()) {
                                                                for (JsonNode villageNode : villagesArray) {
                                                                    if (villageNode.isTextual()) {
                                                                        String villageName = villageNode.asText().trim();
                                                                        if (!villageName.isEmpty()) {
                                                                            System.out.println("Processing village: " + villageName);

                                                                            Location village = createBaseLocation(villageName, "Village", cell);
                                                                            locationMap.put(village.getLocationId(), village);
                                                                            locationCount++;
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Batch save all locations
                System.out.println("Starting to save " + locationMap.size() + " locations...");
                int savedCount = 0;
                for (Location location : locationMap.values()) {
                    try {
                        datastore.save(location);
                        savedCount++;

                        if (savedCount % 100 == 0) {
                            System.out.println("Saved " + savedCount + " locations...");
                        }
                    } catch (Exception e) {
                        System.out.println("Error saving location " + location.getLocationName() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                System.out.println("Location data initialization completed. Processed " + locationCount
                        + " locations, saved " + savedCount + " successfully");
            } catch (Exception e) {
                System.out.println("Error during location initialization: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to initialize locations", e);
            }
        } else {
            System.out.println("Location data already exists - skipping initialization");
        }
    }

    private static Location createBaseLocation(String name, String type, Location parent) {
        Location location = new Location();
        location.setLocationId(UUID.randomUUID());
        location.setLocationName(name.trim().toLowerCase());
        location.setLocationType(type);
        location.setParentLocation(parent);
        return location;
    }
}