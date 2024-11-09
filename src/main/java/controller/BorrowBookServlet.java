package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.morphia.Datastore;
import dev.morphia.query.filters.Filters;
import model.Book;
import model.Borrower;
import model.Membership;
import model.MembershipType;
import model.Shelf;
import model.User;
import utils.MongoDBConfig;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

@WebServlet("/borrowBook")
public class BorrowBookServlet extends HttpServlet {
	private Datastore datastore;

	@Override
	public void init() throws ServletException {
		datastore = MongoDBConfig.getDatastore();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("application/json");
		ObjectMapper mapper = new ObjectMapper();
		PrintWriter out = resp.getWriter();

		try {
			Map<String, String> requestBody = mapper.readValue(req.getInputStream(), Map.class);
			String bookIdStr = requestBody.get("bookId");
			if (bookIdStr == null) {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.write(mapper.writeValueAsString(Map.of("error", "Missing bookId")));
				return;
			}

			UUID bookId = UUID.fromString(bookIdStr);
			HttpSession session = req.getSession();
			UUID userId = (UUID) session.getAttribute("userId");
			if (userId == null) {
				resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				out.write(mapper.writeValueAsString(Map.of("error", "User not authenticated")));
				return;
			}

			User user = datastore.find(User.class).filter(Filters.eq("_id", userId)).first();
			if (user == null) {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.write(mapper.writeValueAsString(Map.of("error", "User not found")));
				return;
			}

			Membership membership = datastore.find(Membership.class).filter(Filters.eq("reader", user),
					Filters.eq("membershipStatus", Membership.MembershipStatus.APPROVED)).first();

			if (membership == null) {
				resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
				out.write(mapper.writeValueAsString(Map.of("error", "No active membership")));
				return;
			}

			MembershipType membershipType = membership.getMembershipType();
			int maxBooks = membershipType.getMaxBooks();
			long borrowedBooksCount = datastore.find(Borrower.class).filter(Filters.eq("reader", user)).count();

			if (borrowedBooksCount >= maxBooks) {
				resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
				out.write(mapper.writeValueAsString(Map.of("error", "Borrowing limit reached")));
				return;
			}

			Book book = datastore.find(Book.class).filter(Filters.eq("_id", bookId)).first();
			if (book == null) {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.write(mapper.writeValueAsString(Map.of("error", "Book not found")));
				return;
			}

			Shelf shelf = datastore.find(Shelf.class).filter(Filters.eq("_id", book.getShelf().getShelfId())).first();
			if (shelf == null || shelf.getAvailableStock() <= 0) {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.write(mapper.writeValueAsString(Map.of("error", "No available  bookS")));
				return;
			}

			// Calculate the due date based on membership type
			int borrowDays;
			switch (membershipType.getMembershipName().toLowerCase()) {
			case "gold":
				borrowDays = 14; // 2 weeks
				break;
			case "silver":
				borrowDays = 7; // 1 week
				break;
			case "striver":
				borrowDays = 4; // 4 days
				break;
			default:
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.write(mapper.writeValueAsString(Map.of("error", "Unknown membership type")));
				return;
			}

			Borrower borrower = new Borrower();
			borrower.setBook(book);
			borrower.setReader(user);
			borrower.setPickupDate(new java.util.Date());
			borrower.setDueDate(new java.util.Date(System.currentTimeMillis() + borrowDays * 24L * 60L * 60L * 1000L)); // 24 h times the number of days
																														
																														
			borrower.setFine(membershipType.getPrice());
			borrower.setLateChargeFees(0); // Initially, no late fees
			datastore.save(borrower);

			shelf.setBorrowedNumber(shelf.getBorrowedNumber() + 1);
			shelf.setAvailableStock(shelf.getAvailableStock() - 1);
			datastore.save(shelf);

			resp.setStatus(HttpServletResponse.SC_OK);
			out.write(mapper.writeValueAsString(Map.of("message", "Book borrowed successfully")));
		} catch (Exception e) {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.write(mapper.writeValueAsString(Map.of("error", e.getMessage())));
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		ObjectMapper mapper = new ObjectMapper();

		try {
		    HttpSession session = req.getSession();
		    String role = (String) session.getAttribute("role");
		    UUID userId = (UUID) session.getAttribute("userId");
			System.out.println("Role ===>"+role);
		    if (userId == null) {
		        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		        out.write(mapper.writeValueAsString(Map.of("error", "User not authenticated")));
		        return;
		    }

		    List<Borrower> borrowedBooks;

		    if (role != null && 
		       (role.equalsIgnoreCase("LIBRARIAN") || 
		        role.equalsIgnoreCase("DEAN") || 
		        role.equalsIgnoreCase("HOD") || 
		        role.equalsIgnoreCase("MANAGER"))) {
		        // Fetch all borrows for administrative roles
		    	System.out.println("For admininstrative ");
		        borrowedBooks = datastore.find(Borrower.class).iterator().toList();
		    } else if (role != null && 
		              (role.equalsIgnoreCase("STUDENT") || 
		               role.equalsIgnoreCase("TEACHER"))) {
		    	System.out.println("For student and teacher");
		        // Fetch only current user borrows
		        borrowedBooks = datastore.find(Borrower.class)
		                .filter(Filters.eq("readerId", userId))
		                .iterator()
		                .toList();
		    } else {
		        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
		        out.write(mapper.writeValueAsString(Map.of("error", "Invalid role")));
		        return;
		    }

		    // Late charge check logic (same as before)
		    Date today = new Date();
		    for (Borrower borrower : borrowedBooks) {
		        if (borrower.getDueDate() != null && today.after(borrower.getDueDate())) {
		            long daysLate = (today.getTime() - borrower.getDueDate().getTime()) / (1000 * 60 * 60 * 24);
		            borrower.setLateChargeFees((int) daysLate * 100);
		            datastore.save(borrower);
		        }
		    }

		    // Response logic
		    if (borrowedBooks.isEmpty()) {
		        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		        out.write(mapper.writeValueAsString(Map.of("message", "No borrowed books found")));
		    } else {
		        resp.setStatus(HttpServletResponse.SC_OK);
		        out.write(mapper.writeValueAsString(borrowedBooks));
		    }
		} catch (Exception e) {
		    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		    out.write(mapper.writeValueAsString(Map.of("error", e.getMessage())));
		}
	}

	
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	    resp.setContentType("application/json");
	    PrintWriter out = resp.getWriter();
	    ObjectMapper mapper = new ObjectMapper();

	    try {
	        HttpSession session = req.getSession();
	        String role = (String) session.getAttribute("role");

	        if (role == null || !role.equals("librarian")) {
	            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
	            out.write(mapper.writeValueAsString(Map.of("error", "Access denied: Librarian role required")));
	            return;
	        }

	        // Parse request body
	        Map<String, String> requestBody = mapper.readValue(req.getInputStream(), Map.class);
	        String borrowerIdStr = requestBody.get("borrowerId");

	        if (borrowerIdStr == null) {
	            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	            out.write(mapper.writeValueAsString(Map.of("error", "Missing borrowerId")));
	            return;
	        }

	        UUID borrowerId = UUID.fromString(borrowerIdStr);

	        // Find the borrower record
	        Borrower borrower = datastore.find(Borrower.class).filter(Filters.eq("_id", borrowerId)).first();
	        if (borrower == null) {
	            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
	            out.write(mapper.writeValueAsString(Map.of("error", "Borrower record not found")));
	            return;
	        }

	        // Mark as returned
	        Date returnDate = new Date();
	        borrower.setReturnDate(returnDate);
	        // Save the updated record
	        datastore.save(borrower);

	        // Update the shelf to reflect the returned book
	        Shelf shelf = datastore.find(Shelf.class).filter(Filters.eq("_id", borrower.getBook().getShelf().getShelfId())).first();
	        if (shelf != null) {
	            shelf.setAvailableStock(shelf.getAvailableStock() + 1);
	            shelf.setBorrowedNumber(shelf.getBorrowedNumber() - 1);
	            datastore.save(shelf);
	        }

	        // Success response
	        resp.setStatus(HttpServletResponse.SC_OK);
	        out.write(mapper.writeValueAsString(Map.of("message", "Book marked as returned successfully", "returnDate", returnDate)));
	    } catch (Exception e) {
	        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        out.write(mapper.writeValueAsString(Map.of("error", e.getMessage())));
	    }
	}


}
