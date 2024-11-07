package model;

import dev.morphia.annotations.*;
import java.util.UUID;
import java.util.Date;

@Entity("books")
public class Book {
    @Id
    private UUID bookId;
    private String title;
    private int edition;
    private String ISBNCode;
    private Date publicationYear;
    private String publisherName;

    @Reference
    private Shelf shelf; // Relationship with Shelf

    private BookStatus bookStatus;

    public enum BookStatus {
        BORROWED, RESERVED, AVAILABLE
    }

    // Constructors, Getters, and Setters
    public Book() {
        this.bookId = UUID.randomUUID();
    }

    public UUID getBookId() {
        return bookId;
    }

    public void setBookId(UUID bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getEdition() {
        return edition;
    }

    public void setEdition(int edition) {
        this.edition = edition;
    }

    public String getISBNCode() {
        return ISBNCode;
    }

    public void setISBNCode(String ISBNCode) {
        this.ISBNCode = ISBNCode;
    }

    public Date getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Date publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    public Shelf getShelf() {
        return shelf;
    }

    public void setShelf(Shelf shelf) {
        this.shelf = shelf;
    }

    public BookStatus getBookStatus() {
        return bookStatus;
    }

    public void setBookStatus(BookStatus bookStatus) {
        this.bookStatus = bookStatus;
    }
}
