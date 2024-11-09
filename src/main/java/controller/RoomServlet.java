package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import utils.*;
import dev.morphia.Datastore;
import dev.morphia.query.filters.Filters;
import model.Room;
import model.Shelf;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

@WebServlet("/rooms")
public class RoomServlet extends HttpServlet {
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
            List<Room> rooms = datastore.find(Room.class).iterator().toList();
            out.write(mapper.writeValueAsString(rooms));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(mapper.writeValueAsString(Map.of("error", e.getMessage())));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        ObjectMapper mapper = new ObjectMapper();
        PrintWriter out = resp.getWriter();

        try {
            Room room = mapper.readValue(req.getReader(), Room.class);
            datastore.save(room);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            out.write(mapper.writeValueAsString(room));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(mapper.writeValueAsString(Map.of("error", e.getMessage())));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        ObjectMapper mapper = new ObjectMapper();
        PrintWriter out = resp.getWriter();

        try {
            UUID shelfId = UUID.fromString(req.getParameter("shelfId"));
            UUID roomId = UUID.fromString(req.getParameter("roomId"));

            Shelf shelf = datastore.find(Shelf.class).filter(Filters.eq("_id", shelfId)).first();
            Room room = datastore.find(Room.class).filter(Filters.eq("_id", roomId)).first();

            if (shelf == null || room == null) {
                throw new Exception("Shelf or Room not found");
            }

            shelf.setRoom(room);
            datastore.save(shelf);

            resp.setStatus(HttpServletResponse.SC_OK);
            out.write(mapper.writeValueAsString(Map.of("message", "Shelf assigned to room successfully")));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(mapper.writeValueAsString(Map.of("error", e.getMessage())));
        }
    }
}