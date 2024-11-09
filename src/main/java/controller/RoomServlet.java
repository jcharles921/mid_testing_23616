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
            Map<String, String> requestBody = mapper.readValue(req.getInputStream(), Map.class);
            String shelfIdStr = requestBody.get("shelfId");
            String roomIdStr = requestBody.get("roomId");
        

            if (shelfIdStr == null || roomIdStr == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(mapper.writeValueAsString(Map.of("error", "Missing shelfId or roomId")));
                return;
            }

            // Parse UUIDs for shelfId and roomId
            UUID shelfId = UUID.fromString(shelfIdStr);
            UUID roomId = UUID.fromString(roomIdStr);

            // Retrieve the Shelf and Room objects from the datastore
            Shelf shelf = datastore.find(Shelf.class).filter(Filters.eq("_id", shelfId)).first();
            Room room = datastore.find(Room.class).filter(Filters.eq("_id", roomId)).first();

            if (shelf == null) {
                throw new IllegalArgumentException("Shelf not found with provided shelfId.");
            }

            if (room == null) {
                throw new IllegalArgumentException("Room not found with provided roomId.");
            }

            // Assign the new room to the shelf
            shelf.setRoom(room);
            
            // Save the updated shelf object in the datastore
            datastore.save(shelf);

            // Return success response
            resp.setStatus(HttpServletResponse.SC_OK);
            out.write(mapper.writeValueAsString(Map.of("message", "Shelf updated with new room successfully")));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(mapper.writeValueAsString(Map.of("error", e.getMessage())));
        }
    }

}