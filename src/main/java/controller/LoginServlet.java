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

        response.setContentType("text/plain");
        String username = request.getParameter("userName");
        String password = request.getParameter("password");

        try {
            var datastore = MongoDBConfig.getDatastore();
            User user = datastore.find(User.class)
                    .filter(Filters.eq("userName", username))
                    .first();

            if (user != null && PasswordUtils.verifyPassword(password, user.getPassword())) {
                HttpSession session = request.getSession();
                session.setAttribute("userId", user.getPersonId());
                session.setAttribute("userName", user.getUserName());
                session.setAttribute("role", user.getRole().name());
                session.setMaxInactiveInterval(300);

                String role = user.getRole().name();
                if ("LIBRARIAN".equals(role)) {
                    response.sendRedirect("librarianDashboard.jsp");
                } else if ("HOD".equals(role) || "DEAN".equals(role) || "MANAGER".equals(role)) {    
                    response.sendRedirect("adminDashboard.jsp");
                } else if ("TEACHER".equals(role)|| "STUDENT".equals(role)) {
                    response.sendRedirect("memberDashboard.jsp");
                } else {
                    // Handle any other role that is not mapped
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("Unauthorized role");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid username or password");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("An error occurred while processing your request");
        }
    }
}
