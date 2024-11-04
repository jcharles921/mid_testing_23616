package controller;

import dev.morphia.query.filters.Filters;
import model.Location;
import model.Person;
import model.User;
import utils.MongoDBConfig;
import utils.NotificationService;
import utils.PasswordUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.List;
import org.json.JSONObject; // Make sure to import the JSON library
import java.io.BufferedReader;

@WebServlet("/createUser")
public class UserCreationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        try {
            var datastore = MongoDBConfig.getDatastore();

            // Read the JSON body from the request
            StringBuilder jsonBody = new StringBuilder();
            String line;
            try (BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    jsonBody.append(line);
                }
            }

            // Parse the JSON object
            JSONObject jsonObject = new JSONObject(jsonBody.toString());
            String firstName = jsonObject.optString("first_name");
            String lastName = jsonObject.optString("last_name");
            String userName = jsonObject.optString("user_name");
            String password = jsonObject.optString("password");
            String genderString = jsonObject.optString("gender");
            String phoneNumber = "+250" + jsonObject.optString("phone_number");
            String roleString = jsonObject.optString("role");
            String villageName = jsonObject.optString("village");
            String expectedCellName = jsonObject.optString("cell");

            // Validate required fields
            if (firstName == null || firstName.trim().isEmpty() || lastName == null || lastName.trim().isEmpty()
                    || userName == null || userName.trim().isEmpty() || password == null || password.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"All fields are required.\"}");
                return;
            }

            // Check if username already exists
            User existingUser = datastore.find(User.class).filter(Filters.eq("userName", userName)).first();
            if (existingUser != null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Username already exists.\"}");
                return;
            }

            // Find matching village
            List<Location> villages = datastore.find(Location.class)
                    .filter(Filters.and(Filters.eq("locationName", villageName), Filters.eq("locationType", "Village")))
                    .iterator().toList();

            Location matchingVillage = null;
            for (Location village : villages) {
                Location cell = village.getParentLocation();
                if (cell != null && cell.getLocationName().equalsIgnoreCase(expectedCellName)) {
                    matchingVillage = village;
                    break;
                }
            }

            if (matchingVillage == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Invalid village or cell selection.\"}");
                return;
            }

            // Create new user
            User newUser = new User();
            newUser.setPersonId(UUID.randomUUID());
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);

            // Set gender
            try {
                Person.Gender gender = Person.Gender.valueOf(genderString.toUpperCase());
                newUser.setGender(gender);
            } catch (IllegalArgumentException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Invalid gender selection.\"}");
                return;
            }

            newUser.setPhoneNumber(phoneNumber);
            newUser.setUserName(userName);
            newUser.setPassword(PasswordUtils.hashPassword(password));

            // Set role
            try {
                User.RoleType role = User.RoleType.valueOf(roleString.toUpperCase());
                newUser.setRole(role);
            } catch (IllegalArgumentException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Invalid role selection.\"}");
                return;
            }

            newUser.setVillageId(matchingVillage.getLocationId());
            // Send confirmation SMS with location details
            NotificationService notificationService = new NotificationService();
            String locationMessage = String.format("Hello %s, your registration is successful! Your location: %s, %s.",
                    firstName,
                    matchingVillage.getLocationName(),
                    matchingVillage.getParentLocation().getLocationName());
            notificationService.sendSms(phoneNumber, locationMessage);
            // Save user
            datastore.save(newUser);

         

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write("{\"message\": \"Registration successful! Confirmation SMS sent.\"}");

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"An error occurred during registration. Please try again.\"}");
        }
    }
}
