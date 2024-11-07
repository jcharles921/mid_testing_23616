package model;

import dev.morphia.annotations.*;
import java.util.UUID;

@Entity("shelves")
public class Shelf {
    @Id
    private UUID shelfId;
    private String bookCategory;
    private int initialStock;
    private int borrowedNumber;
    private int availableStock;

    @Reference
    private Room room; // Relationship with Room

    // Constructors, Getters, and Setters
    public Shelf() {
        this.shelfId = UUID.randomUUID();
    }

    public UUID getShelfId() {
        return shelfId;
    }

    public void setShelfId(UUID shelfId) {
        this.shelfId = shelfId;
    }

    public String getBookCategory() {
        return bookCategory;
    }

    public void setBookCategory(String bookCategory) {
        this.bookCategory = bookCategory;
    }

    public int getInitialStock() {
        return initialStock;
    }

    public void setInitialStock(int initialStock) {
        this.initialStock = initialStock;
    }

    public int getBorrowedNumber() {
        return borrowedNumber;
    }

    public void setBorrowedNumber(int borrowedNumber) {
        this.borrowedNumber = borrowedNumber;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(int availableStock) {
        this.availableStock = availableStock;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}
