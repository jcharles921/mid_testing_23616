package model;

import dev.morphia.annotations.*;
import java.util.UUID;

@Entity("membership_types")
public class MembershipType {
    @Id
    private UUID membershipTypeId;
    private String membershipName;
    private int price;
    private int maxBooks;

  
    public MembershipType() {
        this.membershipTypeId = UUID.randomUUID();
    }

    // Custom constructor for easier seeding
    public MembershipType(UUID id, String membershipName, int price, int maxBooks) {
        this.membershipTypeId = id;
        this.membershipName = membershipName;
        this.price = price;
        this.maxBooks = maxBooks;
    }

    // Getters and Setters
    public UUID getMembershipTypeId() {
        return membershipTypeId;
    }

    public void setMembershipTypeId(UUID membershipTypeId) {
        this.membershipTypeId = membershipTypeId;
    }

    public String getMembershipName() {
        return membershipName;
    }

    public void setMembershipName(String membershipName) {
        this.membershipName = membershipName;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getMaxBooks() {
        return maxBooks;
    }

    public void setMaxBooks(int maxBooks) {
        this.maxBooks = maxBooks;
    }
}
