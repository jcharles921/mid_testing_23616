package controller;

import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.query.filters.Filters;
import model.Location;
import model.Person;
import model.User;
import utils.PasswordUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@WebServlet("/createUser")
public class UserCreationServlet extends HttpServlet {
    
    private Datastore datastore;

    @Override
    public void init() throws ServletException {
        datastore = Morphia.createDatastore(MongoClients.create(), "auca_library_db");
        datastore.getMapper().mapPackage("model");
        datastore.ensureIndexes();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            String firstName = request.getParameter("first_name");
            String lastName = request.getParameter("last_name");
            String genderString = request.getParameter("gender");
            String phoneNumber = request.getParameter("phone_number");
            String userName = request.getParameter("user_name");
            String password = request.getParameter("password");
            String roleString = request.getParameter("role");
            String villageName = request.getParameter("village");

            // Find village location by name using modern filter syntax
            Location village = datastore.find(Location.class)
                .filter(
                    Filters.and(
                        Filters.eq("locationName", villageName),
                        Filters.eq("locationType", "Village")
                    )
                )
                .first();

            if (village == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid village name.");
                return;
            }

            // Hash the password
            String hashedPassword = PasswordUtils.hashPassword(password);

            // Create and save the new user
            User newUser = new User();
            newUser.setPersonId(UUID.randomUUID());
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            
            try {
                Person.Gender gender = Person.Gender.valueOf(genderString.toUpperCase());
                newUser.setGender(gender);
            } catch (IllegalArgumentException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid gender value.");
                return;
            }

            newUser.setPhoneNumber(phoneNumber);
            newUser.setUserName(userName);
            newUser.setPassword(hashedPassword);
            
            try {
                User.RoleType role = User.RoleType.valueOf(roleString.toUpperCase());
                newUser.setRole(role);
            } catch (IllegalArgumentException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid role value.");
                return;
            }

            newUser.setVillageId(village.getLocationId());

            datastore.save(newUser);

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write("User created successfully.");
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error creating user.");
            e.printStackTrace();
        }
    }
}