package model;

import dev.morphia.annotations.*;
import java.util.UUID;
import java.util.List;

@Entity("permissions")
public class Permission {
	   @Id
	    private UUID permissionId;
	    
	    private String action; // Describes the action (e.g., CREATE_LOCATIONS, BORROW_BOOKS)
	    
	    @Indexed(options = @IndexOptions(unique = false))
	    private List<RoleType> allowedRoles; // Defines which roles can perform this action

	    public enum RoleType {
	        STUDENT,
	        MANAGER,
	        TEACHER,
	        DEAN,
	        HOD,
	        LIBRARIAN,
	        SYSTEM 
	    }
    // Constructors
	    public Permission(String action, List<RoleType> allowedRoles) {
	        this.permissionId = UUID.randomUUID();
	        this.action = action;
	        this.allowedRoles = allowedRoles;
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
