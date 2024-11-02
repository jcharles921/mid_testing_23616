package model;

import dev.morphia.annotations.*;
import java.util.UUID;

@Entity("persons")
public abstract class Person {
    @Id
    private UUID personId; 
    private String firstName;
    private String lastName;
    private Gender gender;
    private String phoneNumber;

    public enum Gender {
        MALE, FEMALE
    }

    // Constructors, Getters, and Setters
    public Person() {
        this.personId = UUID.randomUUID(); 
    }

    public UUID getPersonId() {
        return personId;
    }

    public void setPersonId(UUID personId) {
        this.personId = personId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
