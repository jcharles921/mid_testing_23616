package model;

import dev.morphia.annotations.*;
import java.util.Date;
import java.util.UUID;

@Entity("borrowers")
public class Borrower {
    @Id
    private UUID id;

    @Reference
    private Book book; // Relationship with Book

    @Reference
    private User reader; // Relationship with User

    private Date pickupDate;
    private Date dueDate;
    private Date returnDate;
    private int fine;
    private int lateChargeFees;

   
    public Borrower() {
        this.id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public User getReader() {
        return reader;
    }

    public void setReader(User reader) {
        this.reader = reader;
    }

    public Date getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(Date pickupDate) {
        this.pickupDate = pickupDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }

    public int getFine() {
        return fine;
    }

    public void setFine(int fine) {
        this.fine = fine;
    }

    public int getLateChargeFees() {
        return lateChargeFees;
    }

    public void setLateChargeFees(int lateChargeFees) {
        this.lateChargeFees = lateChargeFees;
    }
}
