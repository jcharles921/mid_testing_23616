package listener;

import dev.morphia.Datastore;
import dev.morphia.query.Query;
import model.Permission;
import model.MembershipType;
import utils.MongoDBConfig;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@WebListener
public class AppContextListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
			// Initialize locations
			URL resource = getClass().getClassLoader().getResource("assets/rwandaLocations.json");
			if (resource != null) {
				String filePath = Paths.get(resource.toURI()).toString();
				MongoDBConfig.getDatastore();
				MongoDBConfig.initializeLocations(filePath);
			} else {
				throw new IOException("locations.json file not found in assets directory");
			}

			// Seed permissions
			seedPermissions();

			// Seed membership types
			seedMembershipTypes();

		} catch (Exception e) {
			System.err.println("Failed to initialize application: " + e.getMessage());
			throw new RuntimeException("Application initialization failed", e);
		}
	}

	private void seedPermissions() {
		Datastore datastore = MongoDBConfig.getDatastore();
		Query<Permission> permissionQuery = datastore.find(Permission.class);

		if (permissionQuery.count() > 0) {
			System.out.println("Permissions are already present.");
		} else {
			System.out.println("Seeding permissions...");
			List<Permission> permissions = Arrays.asList(
					new Permission("CREATE_LOCATIONS",
							Arrays.asList(Permission.RoleType.LIBRARIAN, Permission.RoleType.MANAGER)),
					new Permission("CREATE_ACCOUNT",
							Arrays.asList(Permission.RoleType.LIBRARIAN, Permission.RoleType.MANAGER,
									Permission.RoleType.STUDENT, Permission.RoleType.TEACHER, Permission.RoleType.DEAN,
									Permission.RoleType.HOD)),
					new Permission("BORROW_BOOKS",
							Arrays.asList(Permission.RoleType.STUDENT, Permission.RoleType.TEACHER)),
					new Permission("APPROVE_MEMBERSHIP", Arrays.asList(Permission.RoleType.LIBRARIAN)),
					new Permission("ASSIGN_BOOK_TO_SHELF", Arrays.asList(Permission.RoleType.LIBRARIAN)),
					new Permission("ASSIGN_SHELF_TO_ROOM", Arrays.asList(Permission.RoleType.LIBRARIAN)),
					new Permission("VIEW_ROOM_BOOK_COUNT",
							Arrays.asList(Permission.RoleType.LIBRARIAN, Permission.RoleType.MANAGER)),
					new Permission("CHARGE_LATE_FEES",
							Arrays.asList(Permission.RoleType.LIBRARIAN, Permission.RoleType.SYSTEM)),
					new Permission("CREATE_BOOK", Arrays.asList(Permission.RoleType.LIBRARIAN)),
					new Permission("UPDATE_BOOK", Arrays.asList(Permission.RoleType.LIBRARIAN)),
					new Permission("DELETE_BOOK", Arrays.asList(Permission.RoleType.LIBRARIAN)));

			datastore.save(permissions);
			System.out.println("Permissions seeded successfully.");
		}
	}

	private void seedMembershipTypes() {
		Datastore datastore = MongoDBConfig.getDatastore();
		Query<MembershipType> membershipTypeQuery = datastore.find(MembershipType.class);

		if (membershipTypeQuery.count() > 0) {
			System.out.println("Membership types are already present.");
		} else {
			System.out.println("Seeding membership types...");
			List<MembershipType> membershipTypes = Arrays.asList(
					new MembershipType(UUID.randomUUID(), "Gold", 50, 5),
					new MembershipType(UUID.randomUUID(), "Silver", 30, 3),
					new MembershipType(UUID.randomUUID(), "Striver", 10, 2)
			);

			datastore.save(membershipTypes);
			System.out.println("Membership types seeded successfully.");
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		
	}
}
