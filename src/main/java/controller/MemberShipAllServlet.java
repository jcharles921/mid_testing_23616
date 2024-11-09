package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.morphia.Datastore;
import dev.morphia.query.filters.Filters;
import model.Membership;
import model.Permission;
import utils.MongoDBConfig;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@WebServlet("/membership/all")
public class MemberShipAllServlet extends HttpServlet {
    private Datastore datastore;

    @Override
    public void init() throws ServletException {
        datastore = MongoDBConfig.getDatastore();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        ObjectMapper mapper = new ObjectMapper();

        try {
            String role = req.getParameter("role");

            if (role == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(mapper.writeValueAsString(Map.of("error", "Role parameter is required.")));
                return;
            }

            // Check if the role is LIBRARIAN
            if (!Permission.RoleType.LIBRARIAN.name().equals(role)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.write(mapper.writeValueAsString(Map.of("error", "Only librarians can view all memberships.")));
                return;
            }

            // Retrieve all memberships
            List<Membership> membershipList = datastore.find(Membership.class).iterator().toList();

            resp.setStatus(HttpServletResponse.SC_OK);
            out.write(mapper.writeValueAsString(membershipList));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(mapper.writeValueAsString(Map.of("error", e.getMessage())));
        }
    }
}

