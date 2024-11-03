package controller;

import dev.morphia.query.filters.Filters;
import model.User;
import utils.MongoDBConfig;
import utils.PasswordUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("userName");
        String password = request.getParameter("password");

        try {
            // Get the datastore instance from MongoDBConfig
            var datastore = MongoDBConfig.getDatastore();

            // Find the user by username
            User user = datastore.find(User.class)
                    .filter(Filters.eq("userName", username))
                    .first();
            System.out.println("The user Name "+username);
            System.out.println("The user found"+user.getFirstName());

            // Check if user exists and verify password
            if (user != null && PasswordUtils.verifyPassword(password, user.getPassword())) {
                // Start a session and set user details and role in session
                HttpSession session = request.getSession();
                session.setAttribute("userId", user.getPersonId());
                session.setAttribute("userName", user.getUserName());
                session.setAttribute("role", user.getRole().name());

                // Set response status to indicate successful login
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("Login successful. Welcome, " + user.getFirstName() + "!");
            } else {
                // Invalid credentials
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid username or password.");
            }
        } catch (Exception e) {
            // Log the error and send a server error response
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred while logging in.");
        }
    }
}
