package model;

import dev.morphia.annotations.*;
import java.util.UUID;
import java.util.List;

@Entity("permissions")
public class Permission {
    @Id
    private UUID permissionId;
    
    private String action; 
    
    // Define the enum for roles
    public enum RoleType {
        STUDENT,
        MANAGER,
        TEACHER,
        DEAN,
        HOD,
        LIBRARIAN
    }

    // Store allowed roles as a List of RoleType (instead of using @Reference)
    private List<RoleType> allowedRoles; 

    // Constructors
    public Permission() {
        this.permissionId = UUID.randomUUID();
    }

    // Getters and Setters
    public UUID getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(UUID permissionId) {
        this.permissionId = permissionId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public List<RoleType> getAllowedRoles() {
        return allowedRoles;
    }

    public void setAllowedRoles(List<RoleType> allowedRoles) {
        this.allowedRoles = allowedRoles;
    }
}
