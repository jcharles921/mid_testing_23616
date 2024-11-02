package listener;

import utils.MongoDBConfig;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            // Load the file from the assets package using ClassLoader
            URL resource = getClass().getClassLoader().getResource("assets/rwandaLocations.json");
            if (resource != null) {
                String filePath = Paths.get(resource.toURI()).toString();
                MongoDBConfig.initializeLocations(filePath);
            } else {
                throw new IOException("locations.json file not found in assets directory");
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize location data: " + e.getMessage());
            throw new RuntimeException("Location data initialization failed", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Clean up resources if needed
    }
}
