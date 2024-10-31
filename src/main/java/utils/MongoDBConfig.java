package utils;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;

public class MongoDBConfig {
    private static Datastore datastore;
    private static final String DATABASE_NAME = "mid";
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";

    static {
        try {
            MongoClient mongoClient = MongoClients.create(CONNECTION_STRING);
            datastore = Morphia.createDatastore(mongoClient, DATABASE_NAME);
            
            // The new way to ensure indexes for all mapped classes
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

    // You can add additional configuration methods here if needed
    public static void mapEntity(Class<?> entityClass) {
        datastore.getMapper().map(entityClass);
    }
}