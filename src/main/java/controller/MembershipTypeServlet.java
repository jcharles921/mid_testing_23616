package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.morphia.Datastore;
import dev.morphia.query.filters.Filters;
import model.MembershipType;
import utils.MongoDBConfig;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@WebServlet("/membership/types")
public class MembershipTypeServlet extends HttpServlet {
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

			List<MembershipType> membershipTypes = datastore.find(MembershipType.class).iterator().toList();

			String jsonResponse = mapper.writeValueAsString(membershipTypes);
			resp.setStatus(HttpServletResponse.SC_OK);
			out.write(jsonResponse);
		} catch (Exception e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			out.write(
					mapper.writeValueAsString(Map.of("error", "Failed to fetch membership types: " + e.getMessage())));
		}
	}
}
