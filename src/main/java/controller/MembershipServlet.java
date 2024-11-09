package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.morphia.Datastore;
import dev.morphia.query.filters.Filters;
import model.*;
import utils.MongoDBConfig;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import org.bson.types.ObjectId;

@WebServlet("/membership")
public class MembershipServlet extends HttpServlet {
	private Datastore datastore;

	@Override
	public void init() throws ServletException {
		datastore = MongoDBConfig.getDatastore();
	}

	// Handles creating membership
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		ObjectMapper mapper = new ObjectMapper();

		try {
			Map<String, String> requestBody = mapper.readValue(req.getInputStream(), Map.class);
			String userId = requestBody.get("userId");
			String membershipTypeId = requestBody.get("membershipTypeId");
			String role = requestBody.get("role");

			// Check if the user has permission to create a membership
			if (!hasPermission(role, "CREATE_MEMBERSHIP")) {
				resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
				out.write(mapper
						.writeValueAsString(Map.of("error", "You do not have permission to create memberships.")));
				return;
			}

			if (!isValidRole(role)) {
				resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
				out.write(mapper
						.writeValueAsString(Map.of("error", "Only students and teachers can create memberships.")));
				return;
			}

			User user = datastore.find(User.class).filter(Filters.eq("_id", new ObjectId(userId))).first();
			MembershipType membershipType = datastore.find(MembershipType.class)
					.filter(Filters.eq("_id", new ObjectId(membershipTypeId))).first();

			if (user == null || membershipType == null) {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.write(mapper.writeValueAsString(Map.of("error", "Invalid user or membership type.")));
				return;
			}

			// Create membership with default PENDING status
			Membership membership = new Membership();
			membership.setMembershipId(UUID.randomUUID());
			membership.setReader(user);
			membership.setMembershipType(membershipType);
			membership.setMembershipStatus(Membership.MembershipStatus.PENDING);
			membership.setRegistrationDate(new Date());
			membership.setExpiringTime(new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000)); // 1 year

			datastore.save(membership);

			resp.setStatus(HttpServletResponse.SC_CREATED);
			out.write(mapper
					.writeValueAsString(Map.of("message", "Membership created successfully and is pending validation.",
							"membershipId", membership.getMembershipId())));
		} catch (Exception e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			out.write(mapper.writeValueAsString(Map.of("error", e.getMessage())));
		}
	}

	// Handles validating a membership
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		ObjectMapper mapper = new ObjectMapper();

		try {
			Map<String, String> requestBody = mapper.readValue(req.getInputStream(), Map.class);

			String membershipId = requestBody.get("membershipId");
			String role = requestBody.get("role");

			// Check if the user has permission to validate memberships
			if (!hasPermission(role, "VALIDATE_MEMBERSHIP")) {
				resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
				out.write(mapper
						.writeValueAsString(Map.of("error", "You do not have permission to validate memberships.")));
				return;
			}

			Membership membership = datastore.find(Membership.class)
					.filter(Filters.eq("_id", new ObjectId(membershipId))).first();

			if (membership == null) {
				resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
				out.write(mapper.writeValueAsString(Map.of("error", "Membership not found.")));
				return;
			}

			if (membership.getMembershipStatus() == Membership.MembershipStatus.APPROVED) {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.write(mapper.writeValueAsString(Map.of("error", "Membership is already approved.")));
				return;
			}

			membership.setMembershipStatus(Membership.MembershipStatus.APPROVED);
			datastore.save(membership);

			resp.setStatus(HttpServletResponse.SC_OK);
			out.write(mapper.writeValueAsString(Map.of("message", "Membership validated successfully.")));
		} catch (Exception e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			out.write(mapper.writeValueAsString(Map.of("error", e.getMessage())));
		}
	}
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		ObjectMapper mapper = new ObjectMapper();

		try {
			String userId = req.getParameter("userId");

			// Validate that userId is a valid UUID format (since your model uses UUID)
			if (userId == null || !isValidUUID(userId)) {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.write(mapper.writeValueAsString(Map.of("error", "Invalid userId. It must be a valid UUID.")));
				return;
			}

			UUID uuidUserId = UUID.fromString(userId);

			User user = datastore.find(User.class).filter(Filters.eq("_id", uuidUserId)).first();

			if (user == null) {
				resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
				out.write(mapper.writeValueAsString(Map.of("error", "User not found.")));
				return;
			}
			List<Membership> membershipList = datastore.find(Membership.class).filter(Filters.eq("reader", user))
					.iterator().toList();

			resp.setStatus(HttpServletResponse.SC_OK);
			out.write(mapper.writeValueAsString(membershipList));
		} catch (Exception e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			System.out.println(e.getMessage());
			out.write(mapper.writeValueAsString(Map.of("error", e.getMessage())));
		}
	}
	private boolean isValidRole(String role) {
		return Permission.RoleType.TEACHER.name().equals(role) || Permission.RoleType.STUDENT.name().equals(role);
	}

	private boolean hasPermission(String role, String action) {
		Permission permission = datastore.find(Permission.class).filter(Filters.eq("action", action)).first();
		if (permission != null) {
			return permission.getAllowedRoles().contains(Permission.RoleType.valueOf(role));
		}
		return false;
	}

	private boolean isValidUUID(String userId) {
		try {
			UUID.fromString(userId);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
}
