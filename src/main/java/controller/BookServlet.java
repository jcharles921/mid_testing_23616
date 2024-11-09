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
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.*;
import org.bson.types.ObjectId;

@WebServlet("/books")
public class BookServlet extends HttpServlet {
    private Datastore datastore;

    @Override
    public void init() throws ServletException {
        datastore = MongoDBConfig.getDatastore();
    }

    // Create a new book
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        ObjectMapper mapper = new ObjectMapper();
        PrintWriter out = resp.getWriter();

        try {
            
            Map<String, String> requestBody = mapper.readValue(req.getReader(), Map.class);

            
            String role = requestBody.get("role");
            String shelfId = requestBody.get("shelfId");
            if (role == null || shelfId == null || role.isEmpty() || shelfId.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(mapper.writeValueAsString(Collections.singletonMap("error", "Missing required parameters")));
                return;
            }
            if (!hasPermission(role, "CREATE_BOOK")) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.write(mapper.writeValueAsString(Collections.singletonMap("error", "You do not have permission to manage books.")));
                return;
            }
            Shelf shelf = datastore.find(Shelf.class)
                    .filter(Filters.eq("_id", UUID.fromString(shelfId)))
                    .first();

            if (shelf == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write(mapper.writeValueAsString(Collections.singletonMap("error", "Shelf not found")));
                return;
            }

         
            Book book = new Book();
            book.setTitle(requestBody.get("title"));
            book.setEdition(Integer.parseInt(requestBody.getOrDefault("edition", "1")));
            book.setISBNCode(requestBody.get("ISBNCode"));
          
            try {
                String publicationYearStr = requestBody.get("publicationYear");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date publicationDate = dateFormat.parse(publicationYearStr);
                book.setPublicationYear(publicationDate);
            } catch (ParseException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(mapper.writeValueAsString(Collections.singletonMap("error", "Invalid publication year format")));
                return;
            }
            
            book.setPublisherName(requestBody.get("publisherName"));
            book.setBookStatus(Book.BookStatus.AVAILABLE);
            book.setShelf(shelf);

            // Update shelf stock
            shelf.setAvailableStock(shelf.getAvailableStock() + 1);

           
            datastore.save(book);
            datastore.save(shelf);

            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Book created successfully");
            response.put("bookId", book.getBookId() != null ? book.getBookId().toString() : "");
            response.put("shelfAvailableStock", shelf.getAvailableStock());

            // Respond with the created book and updated shelf stock
            resp.setStatus(HttpServletResponse.SC_CREATED);
            out.write(mapper.writeValueAsString(response));
            
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(mapper.writeValueAsString(Collections.singletonMap("error", "Invalid UUID format for shelf ID")));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(mapper.writeValueAsString(Collections.singletonMap("error", e.getMessage() != null ? e.getMessage() : "An error occurred")));
        }
    }




    // Update a book
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        ObjectMapper mapper = new ObjectMapper();

        try {
            Map<String, String> requestBody = mapper.readValue(req.getInputStream(), Map.class);
            String role = requestBody.get("role");
            String bookId = requestBody.get("bookId");

            // Check permission
            if (!hasPermission(role, "CREATE_BOOK")) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.write(mapper.writeValueAsString(Map.of("error", "You do not have permission to manage books.")));
                return;
            }

            Book book = datastore.find(Book.class).filter(Filters.eq("_id", UUID.fromString(bookId))).first();

            if (book == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write(mapper.writeValueAsString(Map.of("error", "Book not found.")));
                return;
            }

            book.setTitle(requestBody.get("title"));
            book.setEdition(Integer.parseInt(requestBody.get("edition")));
            book.setPublisherName(requestBody.get("publisherName"));
            datastore.save(book);

            resp.setStatus(HttpServletResponse.SC_OK);
            out.write(mapper.writeValueAsString(Map.of("message", "Book updated successfully")));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(mapper.writeValueAsString(Map.of("error", e.getMessage())));
        }
    }

 
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        ObjectMapper mapper = new ObjectMapper();

        try {
            Map<String, String> requestBody = mapper.readValue(req.getInputStream(), Map.class);
            String role = requestBody.get("role");
            String bookId = requestBody.get("bookId");

            if (!hasPermission(role, "DELETE_BOOK")) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.write(mapper.writeValueAsString(Map.of("error", "You do not have permission to delete books.")));
                return;
            }

            Book book = datastore.find(Book.class).filter(Filters.eq("_id", UUID.fromString(bookId))).first();

            if (book == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write(mapper.writeValueAsString(Map.of("error", "Book not found.")));
                return;
            }

            Shelf shelf = book.getShelf(); 

            if (shelf != null) {
                shelf.setAvailableStock(shelf.getAvailableStock() - 1); 
                datastore.save(shelf); 
            }

            datastore.delete(book); 

            resp.setStatus(HttpServletResponse.SC_OK);
            out.write(mapper.writeValueAsString(Map.of("message", "Book deleted successfully", "shelfAvailableStock", shelf != null ? shelf.getAvailableStock() : null)));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(mapper.writeValueAsString(Collections.singletonMap("error", "Invalid UUID format for book ID")));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(mapper.writeValueAsString(Map.of("error", e.getMessage())));
        }
    }


 // Get all books
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        ObjectMapper mapper = new ObjectMapper();

        try {
            List<Book> books = datastore.find(Book.class).iterator().toList();

            if (books.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write(mapper.writeValueAsString(books));
            } else {
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(mapper.writeValueAsString(books));
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(mapper.writeValueAsString(Map.of("error", e.getMessage())));
        }
    }


    private boolean hasPermission(String role, String action) {
        Permission permission = datastore.find(Permission.class).filter(Filters.eq("action", action)).first();
        if (permission != null) {
            return permission.getAllowedRoles().contains(Permission.RoleType.valueOf(role));
        }
        return false;
    }
}
