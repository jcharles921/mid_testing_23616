package model;

import dev.morphia.annotations.*;
import java.util.UUID;

@Entity("rooms")
public class Room {
    @Id
    private UUID roomId;
    private String roomCode;


    public Room() {
        this.roomId = UUID.randomUUID();
    }

    public UUID getRoomId() {
        return roomId;
    }

    public void setRoomId(UUID roomId) {
        this.roomId = roomId;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }
}
