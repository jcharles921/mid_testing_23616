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

@WebServlet("/books")
public class BookServlet extends HttpServlet {
    private Datastore datastore;

    @Override
    public void init() throws ServletException {
        datastore = MongoDBConfig.getDatastore();
    }

    // Create a new book
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        ObjectMapper mapper = new ObjectMapper();

        try {
            Map<String, String> requestBody = mapper.readValue(req.getInputStream(), Map.class);
            String role = requestBody.get("role");
            // Check permission
            if (!hasPermission(role, "CREATE_BOOK")) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.write(mapper.writeValueAsString(Map.of("error", "You do not have permission to manage books.")));
                return;
            }

            Book book = new Book();
            book.setTitle(requestBody.get("title"));
            book.setEdition(Integer.parseInt(requestBody.get("edition")));
            book.setISBNCode(requestBody.get("ISBNCode"));
            book.setPublicationYear(new Date(requestBody.get("publicationYear")));
            book.setPublisherName(requestBody.get("publisherName"));
            book.setBookStatus(Book.BookStatus.AVAILABLE);

            datastore.save(book);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            out.write(mapper.writeValueAsString(Map.of("message", "Book created successfully", "bookId", book.getBookId())));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(mapper.writeValueAsString(Map.of("error", e.getMessage())));
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
            if (!hasPermission(role, "MANAGE_BOOKS")) {
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

    // Delete a book
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        ObjectMapper mapper = new ObjectMapper();

        try {
            Map<String, String> requestBody = mapper.readValue(req.getInputStream(), Map.class);
            String role = requestBody.get("role");
            String bookId = requestBody.get("bookId");

            // Check permission
            if (!hasPermission(role, "MANAGE_BOOKS")) {
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

            datastore.delete(book);
            resp.setStatus(HttpServletResponse.SC_OK);
            out.write(mapper.writeValueAsString(Map.of("message", "Book deleted successfully")));
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
