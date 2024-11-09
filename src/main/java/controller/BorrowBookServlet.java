package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.morphia.Datastore;
import dev.morphia.query.filters.Filters;
import model.Book;
import model.Borrower;
import model.Membership;
import model.Shelf;
import model.User;
import utils.MongoDBConfig;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.util.Map;
import java.util.UUID;

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


            Membership membership = datastore.find(Membership.class)
                    .filter(Filters.eq("reader.userId", userId), Filters.eq("membershipStatus", Membership.MembershipStatus.APPROVED))
                    .first();

            if (membership == null) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.write(mapper.writeValueAsString(Map.of("error", "No active membership")));
                return;
            }

            int maxBooks = membership.getMembershipType().getMaxBooks();
            long borrowedBooksCount = datastore.find(Borrower.class)
                    .filter(Filters.eq("reader.userId", userId))
                    .count();

            if (borrowedBooksCount >= maxBooks) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.write(mapper.writeValueAsString(Map.of("error", "Borrowing limit reached")));
                return;
            }

            // Retrieve the book and shelf
            Book book = datastore.find(Book.class).filter(Filters.eq("_id", bookId)).first();
            if (book == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(mapper.writeValueAsString(Map.of("error", "Book not found")));
                return;
            }

            Shelf shelf = datastore.find(Shelf.class).filter(Filters.eq("_id", book.getShelf().getShelfId())).first();
            if (shelf == null || shelf.getAvailableStock() <= 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(mapper.writeValueAsString(Map.of("error", "No available copies of the book")));
                return;
            }


            Borrower borrower = new Borrower();
            borrower.setBook(book);     
            borrower.setReader(user);       
            borrower.setPickupDate(new java.util.Date());
            borrower.setDueDate(new java.util.Date(System.currentTimeMillis() + 14L * 24 * 60 * 60 * 1000)); // 2 weeks due date
            borrower.setFine(0);
            borrower.setLateChargeFees(0);
            datastore.save(borrower);
            shelf.setBorrowedNumber(shelf.getBorrowedNumber() + 1);
            shelf.setAvailableStock(shelf.getAvailableStock() - 1);
            datastore.save(shelf);
            
            // Success response
            resp.setStatus(HttpServletResponse.SC_OK);
            out.write(mapper.writeValueAsString(Map.of("message", "Book borrowed successfully")));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(mapper.writeValueAsString(Map.of("error", e.getMessage())));
        }
    }
}
