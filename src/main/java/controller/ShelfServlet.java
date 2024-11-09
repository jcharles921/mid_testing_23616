package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import utils.*;
import dev.morphia.Datastore;
import dev.morphia.query.filters.Filters;
import model.Shelf;
import model.Room;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

@WebServlet("/shelves")
public class ShelfServlet extends HttpServlet {
    private Datastore datastore;

    @Override
    public void init() throws ServletException {
        datastore = MongoDBConfig.getDatastore();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        ObjectMapper mapper = new ObjectMapper();
        PrintWriter out = resp.getWriter();

        try {
            List<Shelf> shelves = datastore.find(Shelf.class).iterator().toList();
            resp.setStatus(HttpServletResponse.SC_OK);
            out.write(mapper.writeValueAsString(shelves));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(mapper.writeValueAsString(Map.of("error", e.getMessage())));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        ObjectMapper mapper = new ObjectMapper();
        PrintWriter out = resp.getWriter();

        try {
            Shelf shelf = mapper.readValue(req.getReader(), Shelf.class);

            String roomId = req.getParameter("roomId");
            if (roomId != null) {
                Room room = datastore.find(Room.class).filter(Filters.eq("_id", UUID.fromString(roomId))).first();
                if (room != null) {
                    shelf.setRoom(room);
                } else {
                    throw new IllegalArgumentException("Room not found with provided roomId.");
                }
            }

            datastore.save(shelf);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            out.write(mapper.writeValueAsString(shelf));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(mapper.writeValueAsString(Map.of("error", e.getMessage())));
        }
    }


    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        ObjectMapper mapper = new ObjectMapper();
        PrintWriter out = resp.getWriter();

        try {
            UUID shelfId = UUID.fromString(req.getParameter("shelfId"));
            datastore.find(Shelf.class).filter(Filters.eq("_id", shelfId)).delete();

            resp.setStatus(HttpServletResponse.SC_OK);
            out.write(mapper.writeValueAsString(Map.of("message", "Shelf deleted successfully")));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(mapper.writeValueAsString(Map.of("error", e.getMessage())));
        }
    }
}