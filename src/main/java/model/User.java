package model;

import dev.morphia.annotations.*;

import java.util.UUID;

@Entity("users")
public class User extends Person {
    private String password;
    private String userName;
    private RoleType role;

    @Property("village_id")
    private UUID villageId; 

    public enum RoleType {
        STUDENT, MANAGER, TEACHER, DEAN, HOD, LIBRARIAN
    }

    // Constructors
    public User() {
        super(); 
        this.setPersonId(UUID.randomUUID()); 
    }

    // Getters and Setters
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public RoleType getRole() {
        return role;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }

    public UUID getVillageId() {
        return villageId;
    }

    public void setVillageId(UUID uuid) {
        this.villageId = uuid;
    }
}
