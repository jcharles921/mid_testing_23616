package model;

import dev.morphia.annotations.*;
import java.util.Date;
import java.util.UUID;

@Entity("memberships")
public class Membership {
    @Id
    private UUID membershipId;
    private String membershipCode;

   
    private MembershipStatus membershipStatus;
    private Date registrationDate;
    private Date expiringTime;

    @Reference
    private User reader; // Relationship with User

    
    @Reference
    private MembershipType membershipType; // Reference instead of Embedded

    public enum MembershipStatus {
        APPROVED, REJECTED, PENDING,EXPIRED
    }

    // Constructors, Getters, and Setters
    public Membership() {
        this.membershipId = UUID.randomUUID();
    }

    public UUID getMembershipId() {
        return membershipId;
    }

    public void setMembershipId(UUID membershipId) {
        this.membershipId = membershipId;
    }

    public String getMembershipCode() {
        return membershipCode;
    }

    public void setMembershipCode(String membershipCode) {
        this.membershipCode = membershipCode;
    }

    public MembershipStatus getMembershipStatus() {
        return membershipStatus;
    }

    public void setMembershipStatus(MembershipStatus membershipStatus) {
        this.membershipStatus = membershipStatus;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Date getExpiringTime() {
        return expiringTime;
    }

    public void setExpiringTime(Date expiringTime) {
        this.expiringTime = expiringTime;
    }

    public User getReader() {
        return reader;
    }

    public void setReader(User reader) {
        this.reader = reader;
    }

    public MembershipType getMembershipType() {
        return membershipType;
    }

    public void setMembershipType(MembershipType membershipType) {
        this.membershipType = membershipType;
    }
}
