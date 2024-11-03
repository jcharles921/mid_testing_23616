package controller;

import dev.morphia.query.filters.Filters;
import model.Location;
import model.Person;
import model.User;
import utils.MongoDBConfig;
import utils.PasswordUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.List;


@WebServlet("/createUser")
public class UserCreationServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            var datastore = MongoDBConfig.getDatastore();

            // Retrieve form parameters
            String firstName = request.getParameter("first_name");
            String lastName = request.getParameter("last_name");
            String genderString = request.getParameter("gender");
            String phoneNumber = request.getParameter("phone_number");
            String userName = request.getParameter("user_name");
            String password = request.getParameter("password");
            String roleString = request.getParameter("role");
            String villageName = request.getParameter("village");
            String expectedCellName = request.getParameter("cell");  // Cell name from the request to validate

            // Find all village locations by name and filter by parent cell name
            List<Location> villages = datastore.find(Location.class)
                .filter(
                    Filters.and(
                        Filters.eq("locationName", villageName),
                        Filters.eq("locationType", "Village")
                    )
                )
                .iterator()
                .toList();

            Location matchingVillage = null;
            for (Location village : villages) {
                Location cell = village.getParentLocation();
                if (cell != null && cell.getLocationName().equalsIgnoreCase(expectedCellName)) {
                    matchingVillage = village;
                    break;
                }
            }

            if (matchingVillage == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid village or cell name.");
                return;
            }

            // Hash the password
            String hashedPassword = PasswordUtils.hashPassword(password);

            // Create and save the new user
            User newUser = new User();
            newUser.setPersonId(UUID.randomUUID());
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);

            // Set the gender
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

            if (roleString == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Role is required.");
                return;
            }

            try {
                User.RoleType role = User.RoleType.valueOf(roleString.toUpperCase());
                newUser.setRole(role);
            } catch (IllegalArgumentException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid role value.");
                return;
            }

            // Set the villageId to the matched village
            newUser.setVillageId(matchingVillage.getLocationId());

            // Save the user to the database
            datastore.save(newUser);

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write("User created successfully.");
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error creating user.");
            e.printStackTrace();
        }
    }
}
